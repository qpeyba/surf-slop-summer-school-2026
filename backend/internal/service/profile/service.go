package profile

import (
	"context"
	"errors"
	"log/slog"

	"summer-school-2026/backend/internal/service/auth"
)

var (
	ErrUnauthorized = errors.New("unauthorized")
	ErrInvalidName  = errors.New("invalid name")
)

type Client struct {
	ID            string
	Phone         string
	Allergies     []string
	LoyaltyPoints int
	LoyaltyStatus *string
	OwnEquipment  bool
}

type Repository interface {
	ClientBySessionTokenHash(ctx context.Context, tokenHash string) (Client, bool, error)
	UpdateClient(ctx context.Context, clientID string, allergies []string, ownEquipment *bool) (Client, error)
}

type Service struct {
	repo   Repository
	logger *slog.Logger
}

func NewService(repo Repository, logger *slog.Logger) *Service {
	if logger == nil {
		logger = slog.Default()
	}
	return &Service{repo: repo, logger: logger}
}

func (s *Service) Current(ctx context.Context, token string) (Client, error) {
	if token == "" {
		return Client{}, ErrUnauthorized
	}
	client, ok, err := s.repo.ClientBySessionTokenHash(ctx, auth.HashToken(token))
	if err != nil {
		return Client{}, err
	}
	if !ok {
		return Client{}, ErrUnauthorized
	}
	return client, nil
}

func (s *Service) Update(ctx context.Context, token string, allergies []string, ownEquipment *bool) (Client, error) {
	client, err := s.Current(ctx, token)
	if err != nil {
		return Client{}, err
	}
	return s.repo.UpdateClient(ctx, client.ID, allergies, ownEquipment)
}
