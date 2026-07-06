package httpapi

import (
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestRequestIDMiddleware(t *testing.T) {
	handler := requestIDMiddleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		respID := w.Header().Get("X-Request-Id")
		if respID == "" {
			t.Error("X-Request-Id header should be set on response")
		}
		w.WriteHeader(http.StatusOK)
	}))

	req := httptest.NewRequest(http.MethodGet, "/", nil)
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Errorf("Status = %d, want 200", rec.Code)
	}
}

func TestRequestIDMiddlewareExisting(t *testing.T) {
	handler := requestIDMiddleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		respID := w.Header().Get("X-Request-Id")
		if respID != "custom-id" {
			t.Errorf("X-Request-Id = %q, want custom-id", respID)
		}
		w.WriteHeader(http.StatusOK)
	}))

	req := httptest.NewRequest(http.MethodGet, "/", nil)
	req.Header.Set("X-Request-Id", "custom-id")
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
}

func TestJSONContentTypeMiddleware(t *testing.T) {
	handler := jsonContentTypeMiddleware(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		ct := w.Header().Get("Content-Type")
		if ct != "application/json" {
			t.Errorf("Content-Type = %q, want application/json", ct)
		}
		w.WriteHeader(http.StatusOK)
	}))

	req := httptest.NewRequest(http.MethodGet, "/", nil)
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
}
