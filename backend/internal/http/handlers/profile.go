package handlers

import (
	"errors"
	"net/http"

	httpapi "summer-school-2026/backend/internal/http"
	"summer-school-2026/backend/internal/service/profile"

	"github.com/go-chi/chi/v5"
)

type ProfileHandler struct {
	service *profile.Service
}

func NewProfileHandler(service *profile.Service) *ProfileHandler {
	return &ProfileHandler{service: service}
}

func (h *ProfileHandler) Register(r chi.Router) {
	r.Get("/profile", h.GetProfile)
	r.Patch("/profile", h.UpdateProfile)
}

type ProfileResponse struct {
	ID            string   `json:"id"`
	Phone         string   `json:"phone"`
	Allergies     []string `json:"allergies,omitempty"`
	LoyaltyPoints int      `json:"loyaltyPoints"`
	LoyaltyStatus *string  `json:"loyaltyStatus,omitempty"`
	OwnEquipment  bool     `json:"ownEquipment"`
}

func (h *ProfileHandler) GetProfile(w http.ResponseWriter, r *http.Request) {
	token, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	client, err := h.service.Current(r.Context(), token)
	if err != nil {
		writeProfileError(w, err)
		return
	}
	httpapi.WriteJSON(w, http.StatusOK, profileToResponse(client))
}

type UpdateProfileRequest struct {
	Allergies    []string `json:"allergies,omitempty"`
	OwnEquipment *bool    `json:"ownEquipment,omitempty"`
}

func (h *ProfileHandler) UpdateProfile(w http.ResponseWriter, r *http.Request) {
	token, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	var req UpdateProfileRequest
	if err := httpapi.DecodeJSON(r, &req); err != nil {
		httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверные параметры запроса.", nil)
		return
	}

	client, err := h.service.Update(r.Context(), token, req.Allergies, req.OwnEquipment)
	if err != nil {
		writeProfileError(w, err)
		return
	}
	httpapi.WriteJSON(w, http.StatusOK, profileToResponse(client))
}

func profileToResponse(c profile.Client) ProfileResponse {
	return ProfileResponse{
		ID:            c.ID,
		Phone:         c.Phone,
		Allergies:     c.Allergies,
		LoyaltyPoints: c.LoyaltyPoints,
		LoyaltyStatus: c.LoyaltyStatus,
		OwnEquipment:  c.OwnEquipment,
	}
}

func writeProfileError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, profile.ErrUnauthorized):
		httpapi.WriteError(w, http.StatusUnauthorized, httpapi.CodeUnauthorized, "Требуется авторизация.", nil)
	default:
		httpapi.WriteError(w, http.StatusInternalServerError, httpapi.CodeInternalError, "Что-то пошло не так. Попробуйте ещё раз позже.", nil)
	}
}
