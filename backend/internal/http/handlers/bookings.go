package handlers

import (
	"errors"
	"net/http"
	"time"

	httpapi "summer-school-2026/backend/internal/http"
	"summer-school-2026/backend/internal/service/booking"

	"github.com/go-chi/chi/v5"
)

type BookingHandler struct {
	service *booking.Service
}

func NewBookingHandler(service *booking.Service) *BookingHandler {
	return &BookingHandler{service: service}
}

func (h *BookingHandler) Register(r chi.Router) {
	r.Post("/bookings", h.CreateBooking)
	r.Get("/bookings", h.ListBookings)
	r.Get("/bookings/{bookingId}", h.GetBooking)
	r.Post("/bookings/{bookingId}/cancel", h.CancelBooking)
	r.Post("/bookings/{bookingId}/transfer", h.TransferBooking)
	r.Put("/bookings/{bookingId}/review", h.UpsertReview)
}

// --- CreateBooking ---

type CreateBookingRequest struct {
	SlotID        string `json:"slotId"`
	EquipmentType string `json:"equipmentType"`
}

type BookingResponse struct {
	Booking BookingItem `json:"booking"`
}

func (h *BookingHandler) CreateBooking(w http.ResponseWriter, r *http.Request) {
	token, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	var req CreateBookingRequest
	if err := httpapi.DecodeJSON(r, &req); err != nil {
		httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверные параметры запроса.", nil)
		return
	}

	idempotencyKey := r.Header.Get("Idempotency-Key")

	created, err := h.service.Create(r.Context(), booking.CreateCommand{
		Token:          token,
		IdempotencyKey: idempotencyKey,
		SlotID:         req.SlotID,
		EquipmentType:  req.EquipmentType,
	})
	if err != nil {
		writeBookingError(w, err)
		return
	}
	httpapi.WriteJSON(w, http.StatusCreated, BookingResponse{Booking: bookingToItem(created)})
}

// --- ListBookings ---

type BookingPage struct {
	Items []BookingItem `json:"items"`
	Total int           `json:"total"`
}

func (h *BookingHandler) ListBookings(w http.ResponseWriter, r *http.Request) {
	token, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}

	q := r.URL.Query()
	limit, offset := pagination(q.Get("limit"), q.Get("offset"))

	var status *string
	if s := q.Get("status"); s != "" {
		status = &s
	}

	list, err := h.service.List(r.Context(), booking.ListCommand{Token: token, Status: status, Limit: limit, Offset: offset})
	if err != nil {
		writeBookingError(w, err)
		return
	}

	items := make([]BookingItem, 0, len(list.Items))
	for _, item := range list.Items {
		items = append(items, bookingToItem(item))
	}
	httpapi.WriteJSON(w, http.StatusOK, BookingPage{Items: items, Total: list.Total})
}

// --- GetBooking ---

func (h *BookingHandler) GetBooking(w http.ResponseWriter, r *http.Request) {
	token, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	bookingID := chi.URLParam(r, "bookingId")
	found, err := h.service.Get(r.Context(), token, bookingID)
	if err != nil {
		writeBookingError(w, err)
		return
	}
	httpapi.WriteJSON(w, http.StatusOK, BookingResponse{Booking: bookingToItem(found)})
}

// --- CancelBooking ---

func (h *BookingHandler) CancelBooking(w http.ResponseWriter, r *http.Request) {
	token, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	bookingID := chi.URLParam(r, "bookingId")
	cancelled, err := h.service.Cancel(r.Context(), token, bookingID)
	if err != nil {
		writeBookingError(w, err)
		return
	}
	httpapi.WriteJSON(w, http.StatusOK, BookingResponse{Booking: bookingToItem(cancelled)})
}

// --- TransferBooking ---

type TransferRequest struct {
	NewSlotID string `json:"newSlotId"`
}

type TransferResponse struct {
	OldBooking BookingItem `json:"oldBooking"`
	NewBooking BookingItem `json:"newBooking"`
}

func (h *BookingHandler) TransferBooking(w http.ResponseWriter, r *http.Request) {
	token, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	bookingID := chi.URLParam(r, "bookingId")
	var req TransferRequest
	if err := httpapi.DecodeJSON(r, &req); err != nil {
		httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверные параметры запроса.", nil)
		return
	}

	oldB, newB, err := h.service.Transfer(r.Context(), booking.TransferCommand{
		Token:     token,
		BookingID: bookingID,
		NewSlotID: req.NewSlotID,
	})
	if err != nil {
		writeBookingError(w, err)
		return
	}
	httpapi.WriteJSON(w, http.StatusOK, TransferResponse{
		OldBooking: bookingToItem(oldB),
		NewBooking: bookingToItem(newB),
	})
}

// --- UpsertReview ---

type ReviewRequest struct {
	Rating int     `json:"rating"`
	Text   *string `json:"text,omitempty"`
}

type ReviewResponse struct {
	Review           booking.Review `json:"review"`
	InstructorRating float64        `json:"instructorRating"`
}

func (h *BookingHandler) UpsertReview(w http.ResponseWriter, r *http.Request) {
	token, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	bookingID := chi.URLParam(r, "bookingId")
	var req ReviewRequest
	if err := httpapi.DecodeJSON(r, &req); err != nil {
		httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверные параметры запроса.", nil)
		return
	}

	result, err := h.service.UpsertReview(r.Context(), booking.UpsertReviewCommand{
		Token:     token,
		BookingID: bookingID,
		Rating:    req.Rating,
		Text:      req.Text,
	})
	if err != nil {
		writeBookingError(w, err)
		return
	}
	httpapi.WriteJSON(w, http.StatusOK, ReviewResponse{
		Review:           result.Review,
		InstructorRating: result.InstructorRating,
	})
}

// --- DTOs ---

type BookingItem struct {
	ID            string     `json:"id"`
	SlotID        string     `json:"slotId"`
	ClientID      string     `json:"clientId"`
	EquipmentType string     `json:"equipmentType"`
	Status        string     `json:"status"`
	RefundAmount  *float64   `json:"refundAmount,omitempty"`
	ReviewRating  *int       `json:"reviewRating,omitempty"`
	ReviewText    *string    `json:"reviewText,omitempty"`
	CreatedAt     string     `json:"createdAt"`
	CancelledAt   *string    `json:"cancelledAt,omitempty"`
}

func bookingToItem(b booking.Booking) BookingItem {
	item := BookingItem{
		ID:            b.ID,
		SlotID:        b.SlotID,
		ClientID:      b.ClientID,
		EquipmentType: b.EquipmentType,
		Status:        b.Status,
		RefundAmount:  b.RefundAmount,
		ReviewRating:  b.ReviewRating,
		ReviewText:    b.ReviewText,
		CreatedAt:     b.CreatedAt.Format(time.RFC3339),
	}
	if b.CancelledAt != nil {
		s := b.CancelledAt.Format(time.RFC3339)
		item.CancelledAt = &s
	}
	return item
}

func writeBookingError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, booking.ErrUnauthorized):
		httpapi.WriteError(w, http.StatusUnauthorized, httpapi.CodeUnauthorized, "Требуется авторизация.", nil)
	case errors.Is(err, booking.ErrInvalidRequest):
		httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверные параметры запроса.", nil)
	case errors.Is(err, booking.ErrDoubleBooking):
		httpapi.WriteError(w, http.StatusConflict, httpapi.CodeDoubleBooking, "Вы уже записаны на выбранный класс.", nil)
	case errors.Is(err, booking.ErrIdempotencyConflict):
		httpapi.WriteError(w, http.StatusConflict, httpapi.CodeIdempotencyConflict, "Ключ идемпотентности уже использован для другого запроса.", nil)
	case errors.Is(err, booking.ErrAlreadyCancelled):
		httpapi.WriteError(w, http.StatusConflict, httpapi.CodeAlreadyCancelled, "Бронь уже отменена или завершена.", nil)
	case errors.Is(err, booking.ErrForbidden):
		httpapi.WriteError(w, http.StatusForbidden, httpapi.CodeForbidden, "Доступ запрещён.", nil)
	case errors.Is(err, booking.ErrNotFound):
		httpapi.WriteError(w, http.StatusNotFound, httpapi.CodeNotFound, "Бронь не найдена.", nil)
	case errors.Is(err, booking.ErrSlotFull):
		httpapi.WriteError(w, http.StatusConflict, httpapi.CodeSlotFull, "На этом классе больше нет свободных мест.", nil)
	case errors.Is(err, booking.ErrSlotCancelled):
		httpapi.WriteError(w, http.StatusGone, httpapi.CodeSlotCancelled, "Класс отменён студией.", nil)
	case errors.Is(err, booking.ErrSlotStarted):
		httpapi.WriteError(w, http.StatusGone, httpapi.CodeSlotStarted, "Запись закрыта. До начала осталось менее 10 минут.", nil)
	case errors.Is(err, booking.ErrNotCompleted):
		httpapi.WriteError(w, http.StatusForbidden, "booking_not_completed", "Оценить шефа можно только после завершения класса.", nil)
	case errors.Is(err, booking.ErrInvalidRating):
		httpapi.WriteError(w, http.StatusBadRequest, "invalid_rating", "Рейтинг должен быть от 1 до 5.", nil)
	default:
		httpapi.WriteError(w, http.StatusInternalServerError, httpapi.CodeInternalError, "Что-то пошло не так. Попробуйте ещё раз позже.", nil)
	}
}

// --- shared ---

func bearerOrUnauthorized(w http.ResponseWriter, r *http.Request) (string, bool) {
	token, err := httpapi.BearerToken(r)
	if err != nil {
		httpapi.WriteError(w, http.StatusUnauthorized, httpapi.CodeUnauthorized, "Требуется авторизация.", nil)
		return "", false
	}
	return token, true
}
