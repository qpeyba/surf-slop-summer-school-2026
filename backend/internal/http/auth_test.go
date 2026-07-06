package httpapi

import (
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestBearerTokenValid(t *testing.T) {
	r := httptest.NewRequest(http.MethodGet, "/", nil)
	r.Header.Set("Authorization", "Bearer test-token-123")

	token, err := BearerToken(r)
	if err != nil {
		t.Fatalf("BearerToken() unexpected error: %v", err)
	}
	if token != "test-token-123" {
		t.Errorf("BearerToken() = %q, want test-token-123", token)
	}
}

func TestBearerTokenMissingHeader(t *testing.T) {
	r := httptest.NewRequest(http.MethodGet, "/", nil)

	_, err := BearerToken(r)
	if err == nil {
		t.Fatal("BearerToken() should fail for missing Authorization header")
	}
}

func TestBearerTokenNotBearer(t *testing.T) {
	r := httptest.NewRequest(http.MethodGet, "/", nil)
	r.Header.Set("Authorization", "Basic YWxhZGRpbjpvcGVuc2VzYW1l")

	_, err := BearerToken(r)
	if err == nil {
		t.Fatal("BearerToken() should fail for non-Bearer Authorization header")
	}
}

func TestBearerTokenEmptyToken(t *testing.T) {
	r := httptest.NewRequest(http.MethodGet, "/", nil)
	r.Header.Set("Authorization", "Bearer ")

	_, err := BearerToken(r)
	if err == nil {
		t.Fatal("BearerToken() should fail for empty token")
	}
}

func TestBearerTokenBearerOnly(t *testing.T) {
	r := httptest.NewRequest(http.MethodGet, "/", nil)
	r.Header.Set("Authorization", "Bearer")

	_, err := BearerToken(r)
	if err == nil {
		t.Fatal("BearerToken() should fail for 'Bearer' without token")
	}
}
