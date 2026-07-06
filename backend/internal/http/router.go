package httpapi

import (
	"log/slog"
	"net/http"

	"github.com/go-chi/chi/v5"
)

type HealthResponse struct {
	Status string `json:"status"`
}

type RouteRegistrar interface {
	Register(r chi.Router)
}

func NewRouter(logger *slog.Logger, registrars ...RouteRegistrar) http.Handler {
	if logger == nil {
		logger = slog.Default()
	}

	router := chi.NewRouter()
	router.Use(requestIDMiddleware)
	router.Use(recoverMiddleware(logger))
	router.Use(accessLogMiddleware(logger))
	router.Use(jsonContentTypeMiddleware)
	router.NotFound(func(w http.ResponseWriter, r *http.Request) {
		WriteError(w, http.StatusNotFound, CodeNotFound, "Запрашиваемый ресурс не найден.", nil)
	})
	router.MethodNotAllowed(func(w http.ResponseWriter, r *http.Request) {
		WriteError(w, http.StatusNotFound, CodeNotFound, "Запрашиваемый ресурс не найден.", nil)
	})
	router.Get("/healthz", healthHandler)
	router.Get("/readyz", healthHandler)

	for _, reg := range registrars {
		reg.Register(router)
	}

	return router
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	writeJSON(w, http.StatusOK, HealthResponse{Status: "ok"})
}
