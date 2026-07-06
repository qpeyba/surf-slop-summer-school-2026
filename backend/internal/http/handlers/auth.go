package handlers

import (
	"errors"
	"net/http"

	httpapi "summer-school-2026/backend/internal/http"
	"summer-school-2026/backend/internal/service/auth"

	"github.com/go-chi/chi/v5"
)

type AuthHandler struct {
	service *auth.Service
}

func NewAuthHandler(service *auth.Service) *AuthHandler {
	return &AuthHandler{service: service}
}

func (h *AuthHandler) Register(r chi.Router) {
	r.Post("/auth/otp/request", h.RequestOtp)
	r.Post("/auth/otp/verify", h.VerifyOtp)
}

type OtpRequest struct {
	Phone string `json:"phone"`
}

type OtpResponse struct {
	Message string `json:"message"`
}

func (h *AuthHandler) RequestOtp(w http.ResponseWriter, r *http.Request) {
	var req OtpRequest
	if err := httpapi.DecodeJSON(r, &req); err != nil {
		httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверные параметры запроса.", nil)
		return
	}

	result, err := h.service.RequestCode(r.Context(), req.Phone)
	if err != nil {
		writeAuthError(w, err)
		return
	}

	httpapi.WriteJSON(w, http.StatusAccepted, OtpResponse{
		Message: "Код отправлен. (dev: " + result.Code + ")",
	})
}

type OtpVerifyRequest struct {
	Phone string `json:"phone"`
	Code  string `json:"code"`
}

type TokenResponse struct {
	AccessToken string `json:"accessToken"`
	TokenType   string `json:"tokenType"`
	ExpiresIn   int    `json:"expiresIn"`
}

func (h *AuthHandler) VerifyOtp(w http.ResponseWriter, r *http.Request) {
	var req OtpVerifyRequest
	if err := httpapi.DecodeJSON(r, &req); err != nil {
		httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверные параметры запроса.", nil)
		return
	}

	result, err := h.service.VerifyCode(r.Context(), req.Phone, req.Code)
	if err != nil {
		writeAuthError(w, err)
		return
	}

	httpapi.WriteJSON(w, http.StatusOK, TokenResponse{
		AccessToken: result.Token,
		TokenType:   "bearer",
		ExpiresIn:   86400,
	})
}

func writeAuthError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, auth.ErrInvalidPhone):
		httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверный формат телефона.", nil)
	case errors.Is(err, auth.ErrInvalidCode):
		httpapi.WriteError(w, http.StatusConflict, httpapi.CodeInvalidCode, "Неверный код подтверждения.", nil)
	case errors.Is(err, auth.ErrTooManyRequests):
		httpapi.WriteError(w, http.StatusTooManyRequests, httpapi.CodeTooManyRequests, "Слишком много запросов. Повторите позже.", nil)
	default:
		httpapi.WriteError(w, http.StatusInternalServerError, httpapi.CodeInternalError, "Что-то пошло не так. Попробуйте ещё раз позже.", nil)
	}
}
