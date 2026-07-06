package profile_test

import (
	"context"
	"errors"
	"testing"

	"summer-school-2026/backend/internal/service/profile"
)

type mockRepo struct {
	client    profile.Client
	clientOk  bool
	clientErr error
	updated   profile.Client
	updateErr error
}

func (m *mockRepo) ClientBySessionTokenHash(ctx context.Context, tokenHash string) (profile.Client, bool, error) {
	return m.client, m.clientOk, m.clientErr
}

func (m *mockRepo) UpdateClient(ctx context.Context, clientID string, allergies []string, ownEquipment *bool) (profile.Client, error) {
	return m.updated, m.updateErr
}

func TestCurrentProfileUnauthorized(t *testing.T) {
	svc := profile.NewService(&mockRepo{clientOk: false}, nil)

	_, err := svc.Current(context.Background(), "")
	if !errors.Is(err, profile.ErrUnauthorized) {
		t.Errorf("Expected ErrUnauthorized for empty token, got %v", err)
	}
}

func TestCurrentProfileInvalidToken(t *testing.T) {
	repo := &mockRepo{clientOk: false}
	svc := profile.NewService(repo, nil)

	_, err := svc.Current(context.Background(), "invalid-token")
	if !errors.Is(err, profile.ErrUnauthorized) {
		t.Errorf("Expected ErrUnauthorized for invalid token, got %v", err)
	}
}

func TestCurrentProfileSuccess(t *testing.T) {
	expected := profile.Client{
		ID:            "client-1",
		Phone:         "+79001234567",
		Allergies:     []string{"молоко", "орехи"},
		LoyaltyPoints: 150,
		OwnEquipment:  true,
	}
	repo := &mockRepo{
		client:   expected,
		clientOk: true,
	}
	svc := profile.NewService(repo, nil)

	result, err := svc.Current(context.Background(), "valid-token")
	if err != nil {
		t.Fatalf("Current() unexpected error: %v", err)
	}
	if result.ID != expected.ID {
		t.Errorf("ID = %q, want %q", result.ID, expected.ID)
	}
	if result.Phone != expected.Phone {
		t.Errorf("Phone = %q, want %q", result.Phone, expected.Phone)
	}
	if len(result.Allergies) != 2 {
		t.Errorf("Allergies count = %d, want 2", len(result.Allergies))
	}
	if result.LoyaltyPoints != 150 {
		t.Errorf("LoyaltyPoints = %d, want 150", result.LoyaltyPoints)
	}
	if !result.OwnEquipment {
		t.Error("OwnEquipment should be true")
	}
}

func TestUpdateProfileUnauthorized(t *testing.T) {
	svc := profile.NewService(&mockRepo{clientOk: false}, nil)

	_, err := svc.Update(context.Background(), "invalid-token", nil, nil)
	if !errors.Is(err, profile.ErrUnauthorized) {
		t.Errorf("Expected ErrUnauthorized, got %v", err)
	}
}

func TestUpdateProfileSuccess(t *testing.T) {
	allergies := []string{"глютен"}
	ownEq := true
	expected := profile.Client{
		ID:            "client-1",
		Allergies:     allergies,
		OwnEquipment:  ownEq,
	}
	repo := &mockRepo{
		client:   profile.Client{ID: "client-1"},
		clientOk: true,
		updated:  expected,
	}
	svc := profile.NewService(repo, nil)

	result, err := svc.Update(context.Background(), "valid-token", allergies, &ownEq)
	if err != nil {
		t.Fatalf("Update() unexpected error: %v", err)
	}
	if len(result.Allergies) != 1 {
		t.Errorf("Allergies count = %d, want 1", len(result.Allergies))
	}
	if result.Allergies[0] != "глютен" {
		t.Errorf("Allergy = %q, want глютен", result.Allergies[0])
	}
}
