package httpapi

import (
	"log/slog"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestRouterHealthz(t *testing.T) {
	logger := slog.Default()
	router := NewRouter(logger, nil)
	srv := httptest.NewServer(router)
	defer srv.Close()

	resp, err := http.Get(srv.URL + "/healthz")
	if err != nil {
		t.Fatalf("GET /healthz failed: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Errorf("GET /healthz status = %d, want 200", resp.StatusCode)
	}
}

func TestRouterReadyz(t *testing.T) {
	logger := slog.Default()
	router := NewRouter(logger, nil)
	srv := httptest.NewServer(router)
	defer srv.Close()

	resp, err := http.Get(srv.URL + "/readyz")
	if err != nil {
		t.Fatalf("GET /readyz failed: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Errorf("GET /readyz status = %d, want 200", resp.StatusCode)
	}
}

func TestRouterNotFound(t *testing.T) {
	logger := slog.Default()
	router := NewRouter(logger, nil)
	srv := httptest.NewServer(router)
	defer srv.Close()

	resp, err := http.Get(srv.URL + "/nonexistent")
	if err != nil {
		t.Fatalf("GET /nonexistent failed: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusNotFound {
		t.Errorf("GET /nonexistent status = %d, want 404", resp.StatusCode)
	}
}

func TestRouterMethodNotAllowed(t *testing.T) {
	logger := slog.Default()
	router := NewRouter(logger, nil)
	srv := httptest.NewServer(router)
	defer srv.Close()

	resp, err := http.Post(srv.URL+"/healthz", "application/json", nil)
	if err != nil {
		t.Fatalf("POST /healthz failed: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusMethodNotAllowed {
		t.Errorf("POST /healthz status = %d, want 405 (BUG-001 fix)", resp.StatusCode)
	}
}

func TestRouterWithNilLogger(t *testing.T) {
	router := NewRouter(nil, nil)
	if router == nil {
		t.Fatal("NewRouter(nil, nil) should not return nil")
	}
}
