package handlers

import (
	"net/http"

	httpapi "summer-school-2026/backend/internal/http"
	"summer-school-2026/backend/internal/storage/postgres"

	"github.com/go-chi/chi/v5"
)

type InstructorHandler struct {
	repo *postgres.InstructorRepository
}

func NewInstructorHandler(repo *postgres.InstructorRepository) *InstructorHandler {
	return &InstructorHandler{repo: repo}
}

func (h *InstructorHandler) Register(r chi.Router) {
	r.Get("/instructors/{instructorId}", h.GetInstructor)
}

func (h *InstructorHandler) GetInstructor(w http.ResponseWriter, r *http.Request) {
	id := chi.URLParam(r, "instructorId")
	instructor, found, err := h.repo.GetByID(r.Context(), id)
	if err != nil {
		httpapi.WriteError(w, http.StatusInternalServerError, httpapi.CodeInternalError, "Что-то пошло не так.", nil)
		return
	}
	if !found {
		httpapi.WriteError(w, http.StatusNotFound, httpapi.CodeNotFound, "Шеф не найден.", nil)
		return
	}
	httpapi.WriteJSON(w, http.StatusOK, InstructorItem{
		ID:             instructor.ID,
		Name:           instructor.Name,
		Status:         instructor.Status,
		Rating:         instructor.Rating,
		Specialization: instructor.Specialization,
	})
}
