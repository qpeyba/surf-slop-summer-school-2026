-- +goose Up
INSERT INTO clients (id, phone, created_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '+79999999999', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO auth_sessions (id, client_id, token_hash, expires_at)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'eKvzZbtWVd3AhsaLNfAuQrWJqa+rtYaEvnryWdlrSSo',
    '2099-12-31 23:59:59+00'
)
ON CONFLICT (id) DO NOTHING;

-- +goose Down
DELETE FROM auth_sessions WHERE id = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';
DELETE FROM clients WHERE id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
