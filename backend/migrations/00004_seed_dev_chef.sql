-- +goose Up
-- Seed-данные для кулинарных мастер-классов «Шеф-стол»

INSERT INTO instructors (id, name, status, rating, specialization)
VALUES
    ('33333333-3333-3333-3333-333333333333', 'Анна Росси', 'Постоянный', 4.8, 'Итальянская кухня'),
    ('44444444-4444-4444-4444-444444444444', 'Дмитрий Кузнецов', 'Постоянный', 4.5, 'Японская кухня'),
    ('77777777-7777-7777-7777-777777777777', 'Елена Воронина', 'Приглашённый', 4.9, 'Французская выпечка')
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name, status = EXCLUDED.status, rating = EXCLUDED.rating, specialization = EXCLUDED.specialization;

INSERT INTO slots (
    id, instructor_id, start_at, menu, difficulty, photo_urls, capacity, booked_count, price, address, status
)
VALUES
    (
        '55555555-5555-5555-5555-555555555555',
        '33333333-3333-3333-3333-333333333333',
        '2026-07-06 10:00:00+03',
        'Итальянская паста',
        'Для новичков',
        ARRAY['https://example.com/photos/pasta1.jpg', 'https://example.com/photos/pasta2.jpg'],
        12, 0, 3500.00,
        'Лофт на территории завода, ул. Примерная 10',
        'Активен'
    ),
    (
        '66666666-6666-6666-6666-666666666666',
        '44444444-4444-4444-4444-444444444444',
        '2026-07-07 12:00:00+03',
        'Японские роллы',
        'Для опытных',
        ARRAY['https://example.com/photos/rolls1.jpg'],
        8, 0, 4200.00,
        'Лофт на территории завода, ул. Примерная 10',
        'Активен'
    ),
    (
        '88888888-8888-8888-8888-888888888888',
        '77777777-7777-7777-7777-777777777777',
        '2026-07-08 16:00:00+03',
        'Французские круассаны',
        'Для новичков',
        ARRAY['https://example.com/photos/croissant1.jpg', 'https://example.com/photos/croissant2.jpg'],
        10, 0, 2800.00,
        'Лофт на территории завода, ул. Примерная 10',
        'Активен'
    )
ON CONFLICT (id) DO NOTHING;

-- +goose Down
DELETE FROM slots WHERE id IN ('55555555-5555-5555-5555-555555555555', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888');
DELETE FROM instructors WHERE id IN ('77777777-7777-7777-7777-777777777777');
