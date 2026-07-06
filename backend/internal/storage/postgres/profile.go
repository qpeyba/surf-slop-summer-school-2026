package postgres

import (
	"context"
	"errors"
	"fmt"

	"summer-school-2026/backend/internal/service/profile"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type ProfileRepository struct {
	db *pgxpool.Pool
}

func NewProfileRepository(db *pgxpool.Pool) *ProfileRepository {
	return &ProfileRepository{db: db}
}

func (r *ProfileRepository) ClientBySessionTokenHash(ctx context.Context, tokenHash string) (profile.Client, bool, error) {
	var client profile.Client
	err := r.db.QueryRow(ctx, `
SELECT c.id::text, c.phone, c.allergies, c.loyalty_points, c.loyalty_status, c.own_equipment
FROM auth_sessions s
JOIN clients c ON c.id = s.client_id
WHERE s.token_hash = $1
  AND s.revoked_at IS NULL
  AND s.expires_at > now()
  AND c.deleted_at IS NULL`, tokenHash).Scan(
		&client.ID,
		&client.Phone,
		&client.Allergies,
		&client.LoyaltyPoints,
		&client.LoyaltyStatus,
		&client.OwnEquipment,
	)
	if errors.Is(err, pgx.ErrNoRows) {
		return profile.Client{}, false, nil
	}
	if err != nil {
		return profile.Client{}, false, fmt.Errorf("query client by session: %w", err)
	}
	return client, true, nil
}

func (r *ProfileRepository) UpdateClient(ctx context.Context, clientID string, allergies []string, ownEquipment *bool) (profile.Client, error) {
	var client profile.Client
	err := r.db.QueryRow(ctx, `
UPDATE clients
SET allergies = COALESCE($2, allergies),
    own_equipment = COALESCE($3, own_equipment)
WHERE id = $1 AND deleted_at IS NULL
RETURNING id::text, phone, allergies, loyalty_points, loyalty_status, own_equipment`,
		clientID, allergies, ownEquipment,
	).Scan(
		&client.ID,
		&client.Phone,
		&client.Allergies,
		&client.LoyaltyPoints,
		&client.LoyaltyStatus,
		&client.OwnEquipment,
	)
	if err != nil {
		return profile.Client{}, fmt.Errorf("update client: %w", err)
	}
	return client, nil
}
