package booking

import (
	"context"
	"crypto/sha256"
	"encoding/base64"
	"errors"
	"fmt"
	"time"

	"summer-school-2026/backend/internal/service/auth"
)

var (
	ErrUnauthorized        = errors.New("unauthorized")
	ErrInvalidRequest      = errors.New("invalid booking request")
	ErrSlotFull            = errors.New("slot full")
	ErrDoubleBooking       = errors.New("double booking")
	ErrSlotCancelled       = errors.New("slot cancelled")
	ErrSlotStarted         = errors.New("slot started")
	ErrIdempotencyConflict = errors.New("idempotency conflict")
	ErrNotFound            = errors.New("booking not found")
	ErrForbidden           = errors.New("booking forbidden")
	ErrAlreadyCancelled    = errors.New("booking already cancelled")
	ErrNotCompleted        = errors.New("booking not completed")
	ErrInvalidRating       = errors.New("invalid rating")
)

type Client struct {
	ID string
}

type Booking struct {
	ID            string
	SlotID        string
	ClientID      string
	EquipmentType string
	Status        string
	RefundAmount  *float64
	ReviewRating  *int
	ReviewText    *string
	CreatedAt     time.Time
	CancelledAt   *time.Time
	Slot          BookingSlot
}

type BookingSlot struct {
	ID                       string
	DateTime                 time.Time
	Menu                     string
	PhotoUrls                []string
	Difficulty               string
	Capacity                 int
	BookedCount              int
	Price                    float64
	Address                  string
	Status                   string
	InstructorID             string
	InstructorName           string
	InstructorStatus         string
	InstructorRating         float64
	InstructorSpecialization *string
}

type CreateCommand struct {
	Token          string
	IdempotencyKey string
	SlotID         string
	EquipmentType  string
}

type ListCommand struct {
	Token  string
	Status *string
	Limit  int
	Offset int
}

type TransferCommand struct {
	Token     string
	BookingID string
	NewSlotID string
}

type UpsertReviewCommand struct {
	Token     string
	BookingID string
	Rating    int
	Text      *string
}

type Review struct {
	Rating int     `json:"rating"`
	Text   *string `json:"text,omitempty"`
}

type ReviewResult struct {
	Review           Review
	InstructorRating float64
}

type BookingList struct {
	Items []Booking
	Total int
}

type Repository interface {
	ClientBySessionTokenHash(ctx context.Context, tokenHash string) (Client, bool, error)
	Create(ctx context.Context, clientID string, command CreateCommand, requestHash string, now time.Time) (Booking, error)
	List(ctx context.Context, clientID string, command ListCommand) (BookingList, error)
	Get(ctx context.Context, clientID, bookingID string) (Booking, error)
	Cancel(ctx context.Context, clientID, bookingID string, now time.Time) (Booking, error)
	Transfer(ctx context.Context, clientID, bookingID, newSlotID string, now time.Time) (Booking, Booking, error)
	UpsertReview(ctx context.Context, clientID, bookingID string, rating int, text *string) (ReviewResult, error)
}

type Service struct {
	repo Repository
	now  func() time.Time
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo, now: time.Now}
}

func (s *Service) Create(ctx context.Context, command CreateCommand) (Booking, error) {
	if command.SlotID == "" || command.EquipmentType == "" {
		return Booking{}, ErrInvalidRequest
	}
	if command.EquipmentType != "Своя" && command.EquipmentType != "Прокат" {
		return Booking{}, ErrInvalidRequest
	}

	client, err := s.currentClient(ctx, command.Token)
	if err != nil {
		return Booking{}, err
	}

	return s.repo.Create(ctx, client.ID, command, requestHash(command), s.now().UTC())
}

func (s *Service) List(ctx context.Context, command ListCommand) (BookingList, error) {
	if command.Limit < 1 || command.Limit > 100 || command.Offset < 0 {
		return BookingList{}, ErrInvalidRequest
	}
	client, err := s.currentClient(ctx, command.Token)
	if err != nil {
		return BookingList{}, err
	}
	return s.repo.List(ctx, client.ID, command)
}

func (s *Service) Get(ctx context.Context, token, bookingID string) (Booking, error) {
	if bookingID == "" {
		return Booking{}, ErrNotFound
	}
	client, err := s.currentClient(ctx, token)
	if err != nil {
		return Booking{}, err
	}
	return s.repo.Get(ctx, client.ID, bookingID)
}

func (s *Service) Cancel(ctx context.Context, token, bookingID string) (Booking, error) {
	if bookingID == "" {
		return Booking{}, ErrNotFound
	}
	client, err := s.currentClient(ctx, token)
	if err != nil {
		return Booking{}, err
	}
	return s.repo.Cancel(ctx, client.ID, bookingID, s.now().UTC())
}

func (s *Service) Transfer(ctx context.Context, command TransferCommand) (Booking, Booking, error) {
	if command.BookingID == "" || command.NewSlotID == "" {
		return Booking{}, Booking{}, ErrNotFound
	}
	client, err := s.currentClient(ctx, command.Token)
	if err != nil {
		return Booking{}, Booking{}, err
	}
	return s.repo.Transfer(ctx, client.ID, command.BookingID, command.NewSlotID, s.now().UTC())
}

func (s *Service) UpsertReview(ctx context.Context, command UpsertReviewCommand) (ReviewResult, error) {
	if command.Rating < 1 || command.Rating > 5 {
		return ReviewResult{}, ErrInvalidRating
	}
	client, err := s.currentClient(ctx, command.Token)
	if err != nil {
		return ReviewResult{}, err
	}
	return s.repo.UpsertReview(ctx, client.ID, command.BookingID, command.Rating, command.Text)
}

func (s *Service) currentClient(ctx context.Context, token string) (Client, error) {
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

func requestHash(command CreateCommand) string {
	sum := sha256.Sum256([]byte(fmt.Sprintf("%s|%s", command.SlotID, command.EquipmentType)))
	return base64.RawStdEncoding.EncodeToString(sum[:])
}

func CancellationStatus(now, startAt time.Time) (refund bool, ok bool) {
	if !now.Before(startAt) {
		return false, false
	}
	remaining := startAt.Sub(now)
	if remaining > 12*time.Hour {
		return false, true
	}
	return true, true
}
