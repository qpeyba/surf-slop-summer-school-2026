package auth_test

import (
	"context"
	"errors"
	"testing"
	"time"

	"summer-school-2026/backend/internal/service/auth"
)

type mockRepo struct {
	latestOTP           auth.OTP
	latestOTPOk         bool
	latestOTPErr        error
	createOTPErr        error
	findClient          auth.Client
	findClientOk        bool
	findClientErr       error
	createClient        auth.Client
	createClientErr     error
	createSessionErr    error
	revokeSessionErr    error
	consumeOTPErr       error
	incrementAttemptsErr error
}

func (m *mockRepo) LatestOTP(ctx context.Context, phone, purpose string) (auth.OTP, bool, error) {
	return m.latestOTP, m.latestOTPOk, m.latestOTPErr
}

func (m *mockRepo) CreateOTP(ctx context.Context, phone, purpose, codeHash string, expiresAt time.Time) error {
	return m.createOTPErr
}

func (m *mockRepo) ConsumeOTP(ctx context.Context, id string, now time.Time) error {
	return m.consumeOTPErr
}

func (m *mockRepo) IncrementOTPAttempts(ctx context.Context, id string) error {
	return m.incrementAttemptsErr
}

func (m *mockRepo) FindClientByPhone(ctx context.Context, phone string) (auth.Client, bool, error) {
	return m.findClient, m.findClientOk, m.findClientErr
}

func (m *mockRepo) CreateClient(ctx context.Context, phone string, now time.Time) (auth.Client, error) {
	return m.createClient, m.createClientErr
}

func (m *mockRepo) CreateSession(ctx context.Context, clientID, tokenHash string, expiresAt time.Time) error {
	return m.createSessionErr
}

func (m *mockRepo) RevokeSession(ctx context.Context, tokenHash string, now time.Time) error {
	return m.revokeSessionErr
}

func TestRequestCodeInvalidPhone(t *testing.T) {
	svc := auth.NewService(&mockRepo{}, nil)

	_, err := svc.RequestCode(context.Background(), "not-a-phone")
	if !errors.Is(err, auth.ErrInvalidPhone) {
		t.Errorf("Expected ErrInvalidPhone, got %v", err)
	}
}

func TestRequestCodeValidPhone(t *testing.T) {
	repo := &mockRepo{}
	svc := auth.NewService(repo, nil)

	result, err := svc.RequestCode(context.Background(), "+79001234567")
	if err != nil {
		t.Fatalf("RequestCode() unexpected error: %v", err)
	}
	if result.TTLSeconds != 300 {
		t.Errorf("TTLSeconds = %d, want 300 (5 min)", result.TTLSeconds)
	}
	if result.ResendAfterSeconds != 60 {
		t.Errorf("ResendAfterSeconds = %d, want 60", result.ResendAfterSeconds)
	}
	if len(result.Code) != 6 {
		t.Errorf("Code length = %d, want 6", len(result.Code))
	}
}

func TestRequestCodePhoneVariants(t *testing.T) {
	validPhones := []string{"+79001234567", "+1234567890", "+8613800138000"}
	repo := &mockRepo{}
	svc := auth.NewService(repo, nil)

	for _, phone := range validPhones {
		result, err := svc.RequestCode(context.Background(), phone)
		if err != nil {
			t.Errorf("RequestCode(%q) unexpected error: %v", phone, err)
			continue
		}
		if len(result.Code) != 6 {
			t.Errorf("Code length for %q = %d, want 6", phone, len(result.Code))
		}
	}

	invalidPhones := []string{"", "not-a-phone", "12345", "+0", "89001234567", "+ 123", "+"}
	for _, phone := range invalidPhones {
		_, err := svc.RequestCode(context.Background(), phone)
		if !errors.Is(err, auth.ErrInvalidPhone) {
			t.Errorf("RequestCode(%q): expected ErrInvalidPhone, got %v", phone, err)
		}
	}
}

func TestVerifyCodeInvalidCodeFormat(t *testing.T) {
	svc := auth.NewService(&mockRepo{}, nil)

	// Non-numeric code
	_, err := svc.VerifyCode(context.Background(), "+79001234567", "abc")
	if !errors.Is(err, auth.ErrInvalidCode) {
		t.Errorf("Expected ErrInvalidCode, got %v", err)
	}

	// Too short
	_, err = svc.VerifyCode(context.Background(), "+79001234567", "123")
	if !errors.Is(err, auth.ErrInvalidCode) {
		t.Errorf("Expected ErrInvalidCode for short code, got %v", err)
	}

	// Too long
	_, err = svc.VerifyCode(context.Background(), "+79001234567", "1234567")
	if !errors.Is(err, auth.ErrInvalidCode) {
		t.Errorf("Expected ErrInvalidCode for long code, got %v", err)
	}

	// Invalid phone with valid code format
	_, err = svc.VerifyCode(context.Background(), "bad-phone", "123456")
	if !errors.Is(err, auth.ErrInvalidCode) {
		t.Errorf("Expected ErrInvalidCode for bad phone, got %v", err)
	}
}

func TestVerifyCodeNoOTP(t *testing.T) {
	repo := &mockRepo{latestOTPOk: false}
	svc := auth.NewService(repo, nil)

	_, err := svc.VerifyCode(context.Background(), "+79001234567", "123456")
	if !errors.Is(err, auth.ErrInvalidCode) {
		t.Errorf("Expected ErrInvalidCode, got %v", err)
	}
}

func TestVerifyCodeAlreadyConsumed(t *testing.T) {
	consumedAt := time.Now().Add(-1 * time.Hour)
	repo := &mockRepo{
		latestOTP: auth.OTP{
			ID:         "otp-1",
			CodeHash:   auth.HashOTP("+79001234567", "login", "123456"),
			CreatedAt:  time.Now().Add(-2 * time.Minute),
			ExpiresAt:  time.Now().Add(3 * time.Minute),
			ConsumedAt: &consumedAt,
		},
		latestOTPOk: true,
	}
	svc := auth.NewService(repo, nil)

	_, err := svc.VerifyCode(context.Background(), "+79001234567", "123456")
	if !errors.Is(err, auth.ErrInvalidCode) {
		t.Errorf("Expected ErrInvalidCode for consumed OTP, got %v", err)
	}
}

func TestHashTokenDeterministic(t *testing.T) {
	token := "test-token"
	hash1 := auth.HashToken(token)
	hash2 := auth.HashToken(token)
	if hash1 != hash2 {
		t.Error("HashToken() should be deterministic")
	}
}

func TestHashOTPDeterministic(t *testing.T) {
	hash1 := auth.HashOTP("+79001234567", "login", "123456")
	hash2 := auth.HashOTP("+79001234567", "login", "123456")
	if hash1 != hash2 {
		t.Error("HashOTP() should be deterministic")
	}
}

func TestHashOTPDifferentInputs(t *testing.T) {
	hash1 := auth.HashOTP("+79001234567", "login", "123456")
	hash2 := auth.HashOTP("+79000000000", "login", "123456")
	if hash1 == hash2 {
		t.Error("HashOTP() should produce different hashes for different inputs")
	}
}

func TestLogoutEmptyToken(t *testing.T) {
	svc := auth.NewService(&mockRepo{}, nil)

	err := svc.Logout(context.Background(), "")
	if !errors.Is(err, auth.ErrInvalidSession) {
		t.Errorf("Expected ErrInvalidSession, got %v", err)
	}
}

func TestLogoutSuccess(t *testing.T) {
	svc := auth.NewService(&mockRepo{}, nil)

	err := svc.Logout(context.Background(), "valid-token")
	if err != nil {
		t.Fatalf("Logout() unexpected error: %v", err)
	}
}
