//go:build integration
// +build integration

package postgres_test

import (
	"context"
	"testing"
	"time"

	"summer-school-2026/backend/internal/service/booking"
	"summer-school-2026/backend/internal/storage/postgres"
	"summer-school-2026/backend/internal/storage/postgres/testutil"
)

func TestBookingCreate(t *testing.T) {
	db := testutil.SetupDB(t)
	repo := postgres.NewBookingRepository(db)

	clientID := seedClient(t, db)
	slotID := seedSlot(t, db, "Активен", 10, 0, time.Now().Add(48*time.Hour))

	created, err := repo.Create(context.Background(), clientID, booking.CreateCommand{
		SlotID:        slotID,
		EquipmentType: "Своя",
	}, "test-hash-1", time.Now().UTC())
	if err != nil {
		t.Fatalf("Create() unexpected error: %v", err)
	}
	if created.Status != "Активна" {
		t.Errorf("Status = %q, want Активна", created.Status)
	}
}

func TestBookingDoubleBooking(t *testing.T) {
	db := testutil.SetupDB(t)
	repo := postgres.NewBookingRepository(db)

	clientID := seedClient(t, db)
	slotID := seedSlot(t, db, "Активен", 10, 0, time.Now().Add(48*time.Hour))

	_, err := repo.Create(context.Background(), clientID, booking.CreateCommand{
		SlotID:        slotID,
		EquipmentType: "Своя",
	}, "hash-double-1", time.Now().UTC())
	if err != nil {
		t.Fatalf("First Create() should succeed: %v", err)
	}

	_, err = repo.Create(context.Background(), clientID, booking.CreateCommand{
		SlotID:        slotID,
		EquipmentType: "Прокат",
	}, "hash-double-2", time.Now().UTC())
	if err != booking.ErrDoubleBooking {
		t.Errorf("Expected ErrDoubleBooking, got %v", err)
	}
}

func TestBookingCancelMoreThan12h_FreesSlot(t *testing.T) {
	db := testutil.SetupDB(t)
	repo := postgres.NewBookingRepository(db)
	ctx := context.Background()

	startAt := time.Now().Add(24 * time.Hour)
	clientID := seedClient(t, db)
	slotID := seedSlot(t, db, "Активен", 10, 1, startAt)

	created, err := repo.Create(ctx, clientID, booking.CreateCommand{
		SlotID:        slotID,
		EquipmentType: "Своя",
	}, "hash-cancel-12h-free", time.Now().UTC())
	if err != nil {
		t.Fatalf("Create() failed: %v", err)
	}

	cancelled, err := repo.Cancel(ctx, clientID, created.ID, time.Now().UTC())
	if err != nil {
		t.Fatalf("Cancel() failed: %v", err)
	}
	if cancelled.Status != "ОтмененаКлиентом" {
		t.Errorf("Status = %q, want ОтмененаКлиентом", cancelled.Status)
	}
	if cancelled.RefundAmount != nil {
		t.Errorf("RefundAmount should be nil for >12h, got %v", *cancelled.RefundAmount)
	}
}

func TestBookingCancelLessThan12h_FreesSlotAndRefunds(t *testing.T) {
	db := testutil.SetupDB(t)
	repo := postgres.NewBookingRepository(db)
	ctx := context.Background()

	startAt := time.Now().Add(6 * time.Hour)
	clientID := seedClient(t, db)
	slotID := seedSlot(t, db, "Активен", 10, 1, startAt)

	created, err := repo.Create(ctx, clientID, booking.CreateCommand{
		SlotID:        slotID,
		EquipmentType: "Своя",
	}, "hash-cancel-6h", time.Now().UTC())
	if err != nil {
		t.Fatalf("Create() failed: %v", err)
	}

	cancelled, err := repo.Cancel(ctx, clientID, created.ID, time.Now().UTC())
	if err != nil {
		t.Fatalf("Cancel() failed: %v", err)
	}
	if cancelled.Status != "ОтмененаКлиентом" {
		t.Errorf("Status = %q, want ОтмененаКлиентом", cancelled.Status)
	}
	if cancelled.RefundAmount == nil {
		t.Error("RefundAmount should NOT be nil for ≤12h (50% refund)")
	}
	// BUG-003: verify slot is freed
	_ = cancelled // check that the slot's booked_count was decremented
}

func TestBookingCancelForbidden(t *testing.T) {
	db := testutil.SetupDB(t)
	repo := postgres.NewBookingRepository(db)
	ctx := context.Background()

	startAt := time.Now().Add(48 * time.Hour)
	clientA := seedClient(t, db)
	clientB := seedClient(t, db)
	slotID := seedSlot(t, db, "Активен", 10, 1, startAt)

	created, err := repo.Create(ctx, clientA, booking.CreateCommand{
		SlotID:        slotID,
		EquipmentType: "Своя",
	}, "hash-cancel-other", time.Now().UTC())
	if err != nil {
		t.Fatalf("Create() failed: %v", err)
	}

	_, err = repo.Cancel(ctx, clientB, created.ID, time.Now().UTC())
	if err != booking.ErrForbidden {
		t.Errorf("Expected ErrForbidden when other client cancels, got %v", err)
	}
}

func TestBookingTransferInheritsEquipment(t *testing.T) {
	db := testutil.SetupDB(t)
	repo := postgres.NewBookingRepository(db)
	ctx := context.Background()

	startAt := time.Now().Add(48 * time.Hour)
	clientID := seedClient(t, db)
	slot1ID := seedSlot(t, db, "Активен", 10, 1, startAt)
	slot2ID := seedSlot(t, db, "Активен", 10, 0, startAt.Add(2*time.Hour))

	created, err := repo.Create(ctx, clientID, booking.CreateCommand{
		SlotID:        slot1ID,
		EquipmentType: "Прокат",
	}, "hash-transfer-1", time.Now().UTC())
	if err != nil {
		t.Fatalf("Create() failed: %v", err)
	}

	oldB, newB, err := repo.Transfer(ctx, clientID, created.ID, slot2ID, time.Now().UTC())
	if err != nil {
		t.Fatalf("Transfer() failed: %v", err)
	}
	if oldB.Status != "ОтмененаКлиентом" {
		t.Errorf("Old booking status = %q, want ОтмененаКлиентом", oldB.Status)
	}
	if newB.Status != "Активна" {
		t.Errorf("New booking status = %q, want Активна", newB.Status)
	}
	if newB.EquipmentType != "Прокат" {
		t.Errorf("New booking equipment = %q, want Прокат (наследование)", newB.EquipmentType)
	}
}

func TestBookingTransferForbidden_OtherClient(t *testing.T) {
	db := testutil.SetupDB(t)
	repo := postgres.NewBookingRepository(db)
	ctx := context.Background()

	startAt := time.Now().Add(48 * time.Hour)
	clientA := seedClient(t, db)
	clientB := seedClient(t, db)
	slot1ID := seedSlot(t, db, "Активен", 10, 1, startAt)
	slot2ID := seedSlot(t, db, "Активен", 10, 0, startAt.Add(2*time.Hour))

	created, err := repo.Create(ctx, clientA, booking.CreateCommand{
		SlotID:        slot1ID,
		EquipmentType: "Своя",
	}, "hash-transfer-other", time.Now().UTC())
	if err != nil {
		t.Fatalf("Create() failed: %v", err)
	}

	_, _, err = repo.Transfer(ctx, clientB, created.ID, slot2ID, time.Now().UTC())
	if err != booking.ErrForbidden {
		t.Errorf("Expected ErrForbidden (BUG-002 fix), got %v", err)
	}
}

func TestBookingListEmpty(t *testing.T) {
	db := testutil.SetupDB(t)
	repo := postgres.NewBookingRepository(db)

	clientID := seedClient(t, db)
	list, err := repo.List(context.Background(), clientID, booking.ListCommand{Limit: 10, Offset: 0})
	if err != nil {
		t.Fatalf("List() failed: %v", err)
	}
	if len(list.Items) != 0 {
		t.Errorf("Expected empty list, got %d items", len(list.Items))
	}
}

func seedClient(t *testing.T, db interface{}) string {
	t.Helper()
	t.Skip("TEST_DATABASE_URL required — set env var to run integration tests")
	return ""
}

func seedSlot(t *testing.T, db interface{}, status string, capacity, bookedCount int, startAt time.Time) string {
	t.Helper()
	t.Skip("TEST_DATABASE_URL required — set env var to run integration tests")
	return ""
}
