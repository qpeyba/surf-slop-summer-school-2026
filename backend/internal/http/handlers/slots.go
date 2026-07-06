package handlers

import (
	"net/http"
	"time"

	httpapi "summer-school-2026/backend/internal/http"
	"summer-school-2026/backend/internal/storage/postgres"

	"github.com/go-chi/chi/v5"
)

type SlotHandler struct {
	repo *postgres.SlotRepository
}

func NewSlotHandler(repo *postgres.SlotRepository) *SlotHandler {
	return &SlotHandler{repo: repo}
}

func (h *SlotHandler) Register(r chi.Router) {
	r.Get("/slots", h.ListSlots)
	r.Get("/slots/{slotId}", h.GetSlot)
}

func (h *SlotHandler) ListSlots(w http.ResponseWriter, r *http.Request) {
	_, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	q := r.URL.Query()
	limit, offset := pagination(q.Get("limit"), q.Get("offset"))
	filters := postgres.SlotFilters{Limit: limit, Offset: offset}

	if from := q.Get("from"); from != "" {
		t, err := time.Parse("2006-01-02", from)
		if err != nil {
			httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверный формат даты from.", nil)
			return
		}
		filters.DateFrom = &t
	}
	if to := q.Get("to"); to != "" {
		t, err := time.Parse("2006-01-02", to)
		if err != nil {
			httpapi.WriteError(w, http.StatusBadRequest, httpapi.CodeBadRequest, "Неверный формат даты to.", nil)
			return
		}
		filters.DateTo = &t
	}

	list, err := h.repo.List(r.Context(), filters)
	if err != nil {
		httpapi.WriteError(w, http.StatusInternalServerError, httpapi.CodeInternalError, "Что-то пошло не так. Попробуйте ещё раз позже.", nil)
		return
	}

	items := make([]SlotItem, 0, len(list.Items))
	for _, slot := range list.Items {
		items = append(items, slotToItem(slot))
	}
	httpapi.WriteJSON(w, http.StatusOK, SlotPage{Items: items, Total: list.Total})
}

func (h *SlotHandler) GetSlot(w http.ResponseWriter, r *http.Request) {
	_, ok := bearerOrUnauthorized(w, r)
	if !ok {
		return
	}
	slotID := chi.URLParam(r, "slotId")
	slot, found, err := h.repo.GetByID(r.Context(), slotID)
	if err != nil {
		httpapi.WriteError(w, http.StatusInternalServerError, httpapi.CodeInternalError, "Что-то пошло не так. Попробуйте ещё раз позже.", nil)
		return
	}
	if !found {
		httpapi.WriteError(w, http.StatusNotFound, httpapi.CodeNotFound, "Класс не найден.", nil)
		return
	}
	httpapi.WriteJSON(w, http.StatusOK, slotToItem(slot))
}

// --- slot DTOs ---

type SlotItem struct {
	ID           string         `json:"id"`
	DateTime     string         `json:"dateTime"`
	Menu         string         `json:"menu"`
	PhotoUrls    []string       `json:"photoUrls,omitempty"`
	Difficulty   string         `json:"difficulty"`
	InstructorID string         `json:"instructorId"`
	Instructor   InstructorItem `json:"instructor"`
	Capacity     int            `json:"capacity"`
	BookedCount  int            `json:"bookedCount"`
	Price        float64        `json:"price"`
	Address      string         `json:"address"`
	Status       string         `json:"status"`
}

type InstructorItem struct {
	ID             string  `json:"id"`
	Name           string  `json:"name"`
	Status         string  `json:"status"`
	Rating         float64 `json:"rating"`
	Specialization *string `json:"specialization,omitempty"`
}

type SlotPage struct {
	Items []SlotItem `json:"items"`
	Total int        `json:"total"`
}

func slotToItem(s postgres.Slot) SlotItem {
	return SlotItem{
		ID:           s.ID,
		DateTime:     s.DateTime.Format(time.RFC3339),
		Menu:         s.Menu,
		PhotoUrls:    s.PhotoUrls,
		Difficulty:   s.Difficulty,
		InstructorID: s.InstructorID,
		Instructor: InstructorItem{
			ID:             s.InstructorID,
			Name:           s.InstructorName,
			Status:         s.InstructorStatus,
			Rating:         s.InstructorRating,
			Specialization: s.InstructorSpecialization,
		},
		Capacity:    s.Capacity,
		BookedCount: s.BookedCount,
		Price:       s.Price,
		Address:     s.Address,
		Status:      s.Status,
	}
}

func pagination(limitStr, offsetStr string) (int, int) {
	limit := 20
	offset := 0
	if limitStr != "" {
		if v := atoi(limitStr); v > 0 && v <= 100 {
			limit = v
		}
	}
	if offsetStr != "" {
		if v := atoi(offsetStr); v >= 0 {
			offset = v
		}
	}
	return limit, offset
}

func atoi(s string) int {
	var n int
	for _, c := range s {
		if c < '0' || c > '9' {
			return 0
		}
		n = n*10 + int(c-'0')
	}
	return n
}
