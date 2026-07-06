-- +goose Up
-- Трансформация схемы от SUP-сёрфинга «Волна» к кулинарным мастер-классам «Шеф-стол»

-- 1. clients: добавляем аллергии, экипировку, лояльность
ALTER TABLE clients
    ADD COLUMN allergies text[] NOT NULL DEFAULT '{}',
    ADD COLUMN own_equipment boolean NOT NULL DEFAULT false,
    ADD COLUMN loyalty_points integer NOT NULL DEFAULT 0,
    ADD COLUMN loyalty_status text;

-- 2. instructors: добавляем статус, рейтинг, специализацию
ALTER TABLE instructors
    ADD COLUMN status text NOT NULL DEFAULT 'Постоянный',
    ADD COLUMN rating numeric(2,1) NOT NULL DEFAULT 5.0,
    ADD COLUMN specialization text;

ALTER TABLE instructors
    ADD CONSTRAINT instructors_status_chk CHECK (status IN ('Постоянный', 'Приглашённый')),
    ADD CONSTRAINT instructors_rating_chk CHECK (rating BETWEEN 1.0 AND 5.0);

-- 3. slots: заменяем route_id и SUP-поля на поля кулинарного класса
-- Сначала удаляем FK и старые колонки
ALTER TABLE slots
    DROP CONSTRAINT slots_route_id_fkey,
    DROP CONSTRAINT slots_seats_chk,
    DROP CONSTRAINT slots_rental_boards_chk,
    DROP CONSTRAINT slots_price_chk,
    DROP CONSTRAINT slots_meeting_point_lat_chk,
    DROP CONSTRAINT slots_meeting_point_lng_chk;

ALTER TABLE slots
    DROP COLUMN route_id,
    DROP COLUMN total_seats,
    DROP COLUMN free_seats,
    DROP COLUMN rental_boards_total,
    DROP COLUMN free_rental_boards,
    DROP COLUMN rental_price,
    DROP COLUMN meeting_point,
    DROP COLUMN meeting_point_lat,
    DROP COLUMN meeting_point_lng;

-- Добавляем поля кулинарного класса
ALTER TABLE slots
    ADD COLUMN menu text NOT NULL DEFAULT '',
    ADD COLUMN difficulty text NOT NULL DEFAULT 'Для новичков',
    ADD COLUMN photo_urls text[] NOT NULL DEFAULT '{}',
    ADD COLUMN capacity integer NOT NULL DEFAULT 10,
    ADD COLUMN booked_count integer NOT NULL DEFAULT 0,
    ADD COLUMN price numeric(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN address text NOT NULL DEFAULT '';

ALTER TABLE slots
    ADD CONSTRAINT slots_difficulty_chk CHECK (difficulty IN ('Для новичков', 'Для опытных')),
    ADD CONSTRAINT slots_capacity_chk CHECK (capacity > 0 AND capacity <= 12),
    ADD CONSTRAINT slots_booked_chk CHECK (booked_count >= 0 AND booked_count <= capacity),
    ADD CONSTRAINT slots_price_chk CHECK (price >= 0);

-- Обновляем статусы
ALTER TABLE slots DROP CONSTRAINT slots_status_chk;
ALTER TABLE slots
    ADD CONSTRAINT slots_status_chk CHECK (status IN ('Активен', 'Отменён студией'));
UPDATE slots SET status = 'Активен' WHERE status = 'scheduled';
UPDATE slots SET status = 'Отменён студией' WHERE status = 'cancelled';

-- 4. bookings: заменяем seats/rental на equipmentType, добавляем отзывы и возврат
ALTER TABLE bookings
    DROP CONSTRAINT bookings_seats_chk,
    DROP CONSTRAINT bookings_rental_chk,
    DROP CONSTRAINT bookings_cancelled_at_chk;

ALTER TABLE bookings
    DROP COLUMN seats_count,
    DROP COLUMN rental_count,
    ADD COLUMN equipment_type text NOT NULL DEFAULT 'Прокат',
    ADD COLUMN refund_amount numeric(10,2),
    ADD COLUMN review_rating integer,
    ADD COLUMN review_text text;

ALTER TABLE bookings
    ADD CONSTRAINT bookings_equipment_chk CHECK (equipment_type IN ('Своя', 'Прокат')),
    ADD CONSTRAINT bookings_rating_chk CHECK (review_rating IS NULL OR review_rating BETWEEN 1 AND 5);

-- Обновляем статусы броней
ALTER TABLE bookings DROP CONSTRAINT bookings_status_chk;
ALTER TABLE bookings
    ADD CONSTRAINT bookings_status_chk CHECK (status IN ('Активна', 'ОтмененаКлиентом', 'ОтмененаСтудией', 'Завершена', 'КлиентНеПришёл'));
UPDATE bookings SET status = 'Активна' WHERE status = 'active';
UPDATE bookings SET status = 'ОтмененаКлиентом' WHERE status = 'cancelled';
UPDATE bookings SET status = 'ОтмененаКлиентом' WHERE status = 'late_cancel';

-- Обновляем уникальный индекс активных броней
DROP INDEX IF EXISTS bookings_active_client_slot_uidx;
CREATE UNIQUE INDEX bookings_active_client_slot_uidx ON bookings (client_id, slot_id) WHERE status = 'Активна';

-- 5. Удаляем таблицу routes (больше не нужна)
DROP TABLE IF EXISTS routes CASCADE;

-- Удаляем старые индексы по слотам
DROP INDEX IF EXISTS slots_route_id_idx;

-- +goose Down
-- Восстановление SUP-схемы не поддерживается (миграция необратима).
-- Для отката используйте резервную копию БД.
