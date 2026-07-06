package httpapi

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestWriteError(t *testing.T) {
	rec := httptest.NewRecorder()
	WriteError(rec, http.StatusNotFound, CodeNotFound, "Запрашиваемый ресурс не найден.", nil)

	if rec.Code != http.StatusNotFound {
		t.Errorf("Status = %d, want 404", rec.Code)
	}
	body := rec.Body.String()
	if body == "" {
		t.Fatal("Response body should not be empty")
	}
	if !strings.Contains(body, "not_found") {
		t.Errorf("Body should contain error code 'not_found', got: %s", body)
	}
}

func TestWriteJSON(t *testing.T) {
	rec := httptest.NewRecorder()
	WriteJSON(rec, http.StatusOK, map[string]string{"status": "ok"})

	if rec.Code != http.StatusOK {
		t.Errorf("Status = %d, want 200", rec.Code)
	}
	body := rec.Body.String()
	if body == "" {
		t.Fatal("Response body should not be empty")
	}
}

func TestDecodeJSONValid(t *testing.T) {
	body := `{"slotId": "some-uuid", "equipmentType": "Прокат"}`
	r := httptest.NewRequest(http.MethodPost, "/", strings.NewReader(body))
	r.Header.Set("Content-Type", "application/json")

	var req struct {
		SlotID        string `json:"slotId"`
		EquipmentType string `json:"equipmentType"`
	}
	err := DecodeJSON(r, &req)
	if err != nil {
		t.Fatalf("DecodeJSON() unexpected error: %v", err)
	}
	if req.SlotID != "some-uuid" {
		t.Errorf("SlotID = %q, want some-uuid", req.SlotID)
	}
	if req.EquipmentType != "Прокат" {
		t.Errorf("EquipmentType = %q, want Прокат", req.EquipmentType)
	}
}

func TestDecodeJSONEmptyBody(t *testing.T) {
	r := httptest.NewRequest(http.MethodPost, "/", nil)
	r.Header.Set("Content-Type", "application/json")

	var req struct {
		Name string `json:"name"`
	}
	err := DecodeJSON(r, &req)
	if err == nil {
		t.Fatal("DecodeJSON() should fail for empty body")
	}
}

func TestDecodeJSONMalformed(t *testing.T) {
	r := httptest.NewRequest(http.MethodPost, "/", strings.NewReader("not-json"))
	r.Header.Set("Content-Type", "application/json")

	var req struct {
		Name string `json:"name"`
	}
	err := DecodeJSON(r, &req)
	if err == nil {
		t.Fatal("DecodeJSON() should fail for malformed JSON")
	}
}

func TestDecodeJSONUnknownFields(t *testing.T) {
	r := httptest.NewRequest(http.MethodPost, "/", strings.NewReader(`{"name": "test", "extra": "field"}`))
	r.Header.Set("Content-Type", "application/json")

	var req struct {
		Name string `json:"name"`
	}
	err := DecodeJSON(r, &req)
	if err == nil {
		t.Fatal("DecodeJSON() should fail for unknown fields")
	}
}

func TestErrorCodesNotEmpty(t *testing.T) {
	codes := []string{
		CodeBadRequest, CodeUnauthorized, CodeForbidden, CodeNotFound,
		CodeSlotFull, CodeDoubleBooking, CodeSlotCancelled, CodeSlotStarted,
		CodeAlreadyCancelled, CodeBookingNotActive, CodeBookingNotFound,
		CodeBookingNotCompleted, CodeInvalidRating, CodeInvalidCode,
		CodeIdempotencyConflict, CodeTooManyRequests, CodeInternalError,
	}
	for _, code := range codes {
		if code == "" {
			t.Errorf("Error code should not be empty")
		}
	}
	if len(codes) != 17 {
		t.Errorf("Expected 17 error codes, got %d", len(codes))
	}
}
