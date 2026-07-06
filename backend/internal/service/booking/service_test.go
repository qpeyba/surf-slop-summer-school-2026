package booking_test

import (
	"context"
	"errors"
	"testing"
	"time"

	"summer-school-2026/backend/internal/service/booking"
)

type mockRepo struct {
	client         booking.Client
	clientOk       bool
	clientErr      error
	createBooking  booking.Booking
	createErr      error
	listBookings   booking.BookingList
	listErr        error
	getBooking     booking.Booking
	getErr         error
	cancelBooking  booking.Booking
	cancelErr      error
	transferOld    booking.Booking
	transferNew    booking.Booking
	transferErr    error
	reviewResult   booking.ReviewResult
	reviewErr      error
}

func (m *mockRepo) ClientBySessionTokenHash(ctx context.Context, tokenHash string) (booking.Client, bool, error) {
	return m.client, m.clientOk, m.clientErr
}

func (m *mockRepo) Create(ctx context.Context, clientID string, command booking.CreateCommand, requestHash string, now time.Time) (booking.Booking, error) {
	return m.createBooking, m.createErr
}

func (m *mockRepo) List(ctx context.Context, clientID string, command booking.ListCommand) (booking.BookingList, error) {
	return m.listBookings, m.listErr
}

func (m *mockRepo) Get(ctx context.Context, clientID, bookingID string) (booking.Booking, error) {
	return m.getBooking, m.getErr
}

func (m *mockRepo) Cancel(ctx context.Context, clientID, bookingID string, now time.Time) (booking.Booking, error) {
	return m.cancelBooking, m.cancelErr
}

func (m *mockRepo) Transfer(ctx context.Context, clientID, bookingID, newSlotID string, now time.Time) (booking.Booking, booking.Booking, error) {
	return m.transferOld, m.transferNew, m.transferErr
}

func (m *mockRepo) UpsertReview(ctx context.Context, clientID, bookingID string, rating int, text *string) (booking.ReviewResult, error) {
	return m.reviewResult, m.reviewErr
}

func TestCreateBookingUnauthorized(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := booking.NewService(repo)

	_, err := svc.Create(context.Background(), booking.CreateCommand{
		Token:         "invalid-token",
		SlotID:        "slot-1",
		EquipmentType: "Своя",
	})
	if !errors.Is(err, booking.ErrUnauthorized) {
		t.Errorf("Expected ErrUnauthorized, got %v", err)
	}
}

func TestCreateBookingInvalidEquipment(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := booking.NewService(repo)

	_, err := svc.Create(context.Background(), booking.CreateCommand{
		Token:         "token",
		SlotID:        "slot-1",
		EquipmentType: "Непонятно",
	})
	if !errors.Is(err, booking.ErrInvalidRequest) {
		t.Errorf("Expected ErrInvalidRequest, got %v", err)
	}
}

func TestCreateBookingEmptySlotID(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := booking.NewService(repo)

	_, err := svc.Create(context.Background(), booking.CreateCommand{
		Token:         "token",
		SlotID:        "",
		EquipmentType: "Своя",
	})
	if !errors.Is(err, booking.ErrInvalidRequest) {
		t.Errorf("Expected ErrInvalidRequest, got %v", err)
	}
}

func TestCreateBookingSuccess(t *testing.T) {
	expected := booking.Booking{
		ID:            "booking-1",
		SlotID:        "slot-1",
		EquipmentType: "Своя",
		Status:        "Активна",
	}
	repo := &mockRepo{
		client:        booking.Client{ID: "client-1"},
		clientOk:      true,
		createBooking: expected,
	}
	svc := booking.NewService(repo)

	result, err := svc.Create(context.Background(), booking.CreateCommand{
		Token:         "valid-token",
		SlotID:        "slot-1",
		EquipmentType: "Своя",
	})
	if err != nil {
		t.Fatalf("Create() unexpected error: %v", err)
	}
	if result.ID != expected.ID {
		t.Errorf("Booking ID = %q, want %q", result.ID, expected.ID)
	}
	if result.Status != "Активна" {
		t.Errorf("Status = %q, want Активна", result.Status)
	}
}

func TestListBookingsInvalidPagination(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := booking.NewService(repo)

	_, err := svc.List(context.Background(), booking.ListCommand{
		Token:  "token",
		Limit:  0,
		Offset: 0,
	})
	if !errors.Is(err, booking.ErrInvalidRequest) {
		t.Errorf("Expected ErrInvalidRequest for limit=0, got %v", err)
	}

	_, err = svc.List(context.Background(), booking.ListCommand{
		Token:  "token",
		Limit:  200,
		Offset: 0,
	})
	if !errors.Is(err, booking.ErrInvalidRequest) {
		t.Errorf("Expected ErrInvalidRequest for limit>100, got %v", err)
	}
}

func TestGetBookingEmptyID(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := booking.NewService(repo)

	_, err := svc.Get(context.Background(), "token", "")
	if !errors.Is(err, booking.ErrNotFound) {
		t.Errorf("Expected ErrNotFound, got %v", err)
	}
}

func TestGetBookingSuccess(t *testing.T) {
	expected := booking.Booking{
		ID:     "booking-1",
		SlotID: "slot-1",
		Status: "Активна",
	}
	repo := &mockRepo{
		client:    booking.Client{ID: "client-1"},
		clientOk:  true,
		getBooking: expected,
	}
	svc := booking.NewService(repo)

	result, err := svc.Get(context.Background(), "valid-token", "booking-1")
	if err != nil {
		t.Fatalf("Get() unexpected error: %v", err)
	}
	if result.ID != expected.ID {
		t.Errorf("Booking ID = %q, want %q", result.ID, expected.ID)
	}
}

func TestCancelBookingEmptyID(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := booking.NewService(repo)

	_, err := svc.Cancel(context.Background(), "token", "")
	if !errors.Is(err, booking.ErrNotFound) {
		t.Errorf("Expected ErrNotFound, got %v", err)
	}
}

func TestTransferBookingEmptyID(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := booking.NewService(repo)

	_, _, err := svc.Transfer(context.Background(), booking.TransferCommand{
		Token:     "token",
		BookingID: "",
		NewSlotID: "slot-1",
	})
	if !errors.Is(err, booking.ErrNotFound) {
		t.Errorf("Expected ErrNotFound, got %v", err)
	}
}

func TestUpsertReviewInvalidRating(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := booking.NewService(repo)

	// Test rating < 1
	_, err := svc.UpsertReview(context.Background(), booking.UpsertReviewCommand{
		Token:     "token",
		BookingID: "booking-1",
		Rating:    0,
	})
	if !errors.Is(err, booking.ErrInvalidRating) {
		t.Errorf("Expected ErrInvalidRating for 0, got %v", err)
	}

	// Test rating > 5
	_, err = svc.UpsertReview(context.Background(), booking.UpsertReviewCommand{
		Token:     "token",
		BookingID: "booking-1",
		Rating:    6,
	})
	if !errors.Is(err, booking.ErrInvalidRating) {
		t.Errorf("Expected ErrInvalidRating for 6, got %v", err)
	}
}

func TestUpsertReviewSuccess(t *testing.T) {
	text := "Отличный шеф!"
	expected := booking.ReviewResult{
		Review:           booking.Review{Rating: 5, Text: &text},
		InstructorRating: 4.8,
	}
	repo := &mockRepo{
		client:       booking.Client{ID: "client-1"},
		clientOk:     true,
		reviewResult: expected,
	}
	svc := booking.NewService(repo)

	result, err := svc.UpsertReview(context.Background(), booking.UpsertReviewCommand{
		Token:     "valid-token",
		BookingID: "booking-1",
		Rating:    5,
		Text:      &text,
	})
	if err != nil {
		t.Fatalf("UpsertReview() unexpected error: %v", err)
	}
	if result.InstructorRating != 4.8 {
		t.Errorf("InstructorRating = %v, want 4.8", result.InstructorRating)
	}
	if result.Review.Rating != 5 {
		t.Errorf("Review.Rating = %d, want 5", result.Review.Rating)
	}
}

func TestCancellationStatus(t *testing.T) {
	now := time.Date(2026, 7, 6, 12, 0, 0, 0, time.UTC)

	// More than 12 hours away — no refund, cancellable
	startAt := now.Add(24 * time.Hour)
	refund, ok := booking.CancellationStatus(now, startAt)
	if refund {
		t.Error("CancellationStatus: should be free (no refund) >12h")
	}
	if !ok {
		t.Error("CancellationStatus: should be cancellable >12h")
	}

	// Less than 12 hours — refund 50%, cancellable
	startAt = now.Add(6 * time.Hour)
	refund, ok = booking.CancellationStatus(now, startAt)
	if !refund {
		t.Error("CancellationStatus: should have refund ≤12h")
	}
	if !ok {
		t.Error("CancellationStatus: should be cancellable ≤12h")
	}

	// Already started — not cancellable
	startAt = now.Add(-1 * time.Hour)
	refund, ok = booking.CancellationStatus(now, startAt)
	if ok {
		t.Error("CancellationStatus: should NOT be cancellable after start")
	}
	if refund {
		t.Error("CancellationStatus: should NOT have refund after start")
	}
}
