package admin

import (
	"context"
	"crypto/rand"
	"fmt"
	"net/http"
	"net/url"
	"strconv"
	"text/template"
	"time"

	"summer-school-2026/backend/internal/storage/postgres"

	"github.com/go-chi/chi/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

import "embed"

//go:embed templates/*
var templateFS embed.FS

type AdminBookingRow struct {
	ID             string
	SlotID         string
	ClientID       string
	ClientPhone    string
	EquipmentType  string
	Status         string
	CreatedAt      time.Time
	CancelledAt    *time.Time
	SlotMenu       string
	SlotDateTime   time.Time
	SlotAddress    string
	InstructorName string
	Price          float64
}

type Handler struct {
	db          *pgxpool.Pool
	slots       *postgres.SlotRepository
	bookings    *postgres.BookingRepository
	instructors *postgres.InstructorRepository
	templates   map[string]*template.Template
}

func NewHandler(db *pgxpool.Pool, slots *postgres.SlotRepository, bookings *postgres.BookingRepository, instructors *postgres.InstructorRepository) *Handler {
	funcMap := template.FuncMap{
		"formatTime": func(t time.Time) string {
			return t.Format("02.01.2006 15:04")
		},
		"formatDateTimeLocal": func(t time.Time) string {
			return t.Format("2006-01-02T15:04")
		},
		"statusBadgeClass": func(s string) string {
			switch s {
			case "Активен", "Активна":
				return "badge-active"
			case "Отменена", "Отменён", "ОтмененаКлиентом":
				return "badge-cancelled"
			case "Завершена", "Завершён":
				return "badge-completed"
			default:
				return ""
			}
		},
		"isActive": func(s string) bool {
			return s == "Активна"
		},
		"eq": func(a, b string) bool {
			return a == b
		},
	}

	h := &Handler{
		db:          db,
		slots:       slots,
		bookings:    bookings,
		instructors: instructors,
		templates:   make(map[string]*template.Template),
	}

	for _, name := range []string{"login", "verify", "dashboard", "slots", "slot_form", "bookings"} {
		h.templates[name] = template.Must(
			template.New("").Funcs(funcMap).ParseFS(templateFS,
				"templates/layout.html",
				"templates/"+name+".html",
			),
		)
	}

	return h
}

func (h *Handler) Register(r chi.Router) {
	r.Get("/admin/login", h.LoginPage)
	r.Post("/admin/login", h.LoginSubmit)
	r.Get("/admin/login/verify", h.LoginVerifyPage)
	r.Post("/admin/login/verify", h.LoginVerifySubmit)
	r.Get("/admin/logout", h.Logout)

	r.Group(func(r chi.Router) {
		r.Use(h.requireAdminMiddleware)
		r.Get("/admin/", h.Dashboard)
		r.Get("/admin/slots", h.SlotsList)
		r.Get("/admin/slots/new", h.SlotNew)
		r.Post("/admin/slots/new", h.SlotCreate)
		r.Get("/admin/slots/edit/{id}", h.SlotEdit)
		r.Post("/admin/slots/edit/{id}", h.SlotUpdate)
		r.Get("/admin/slots/delete/{id}", h.SlotDeleteConfirm)
		r.Post("/admin/slots/delete/{id}", h.SlotDelete)
		r.Get("/admin/bookings", h.BookingsList)
		r.Post("/admin/bookings/{id}/cancel", h.BookingCancel)
	})
}

func (h *Handler) requireAdminMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if !isAdmin(r) {
			http.Redirect(w, r, "/admin/login", http.StatusSeeOther)
			return
		}
		next.ServeHTTP(w, r)
	})
}

func (h *Handler) render(w http.ResponseWriter, name string, data map[string]any) {
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	if err := h.templates[name].ExecuteTemplate(w, "layout", data); err != nil {
		http.Error(w, "template error: "+err.Error(), http.StatusInternalServerError)
	}
}

func baseData(title string) map[string]any {
	return map[string]any{"Title": title, "IsLoggedIn": true}
}

// --- Login ---

func (h *Handler) LoginPage(w http.ResponseWriter, r *http.Request) {
	if isAdmin(r) {
		http.Redirect(w, r, "/admin/", http.StatusSeeOther)
		return
	}
	h.render(w, "login", map[string]any{"Title": "Вход", "IsLoggedIn": false})
}

func (h *Handler) LoginSubmit(w http.ResponseWriter, r *http.Request) {
	if err := r.ParseForm(); err != nil {
		h.render(w, "login", map[string]any{"Title": "Вход", "IsLoggedIn": false, "Error": "Ошибка при обработке формы"})
		return
	}
	phone := r.FormValue("phone")
	if phone != adminPhone {
		h.render(w, "login", map[string]any{"Title": "Вход", "IsLoggedIn": false, "Error": "Неверный номер телефона"})
		return
	}
	http.Redirect(w, r, "/admin/login/verify?phone="+url.QueryEscape(phone), http.StatusSeeOther)
}

func (h *Handler) LoginVerifyPage(w http.ResponseWriter, r *http.Request) {
	if isAdmin(r) {
		http.Redirect(w, r, "/admin/", http.StatusSeeOther)
		return
	}
	phone := r.URL.Query().Get("phone")
	h.render(w, "verify", map[string]any{
		"Title":      "Подтверждение",
		"IsLoggedIn": false,
		"Phone":      phone,
		"Message":    "Dev-код: 123456",
	})
}

func (h *Handler) LoginVerifySubmit(w http.ResponseWriter, r *http.Request) {
	if err := r.ParseForm(); err != nil {
		h.render(w, "verify", map[string]any{"Title": "Подтверждение", "IsLoggedIn": false, "Error": "Ошибка при обработке формы"})
		return
	}
	phone := r.FormValue("phone")
	code := r.FormValue("code")
	if phone != adminPhone || code != "123456" {
		h.render(w, "verify", map[string]any{
			"Title":      "Подтверждение",
			"IsLoggedIn": false,
			"Phone":      phone,
			"Error":      "Неверный код подтверждения",
		})
		return
	}
	setAdminCookie(w)
	http.Redirect(w, r, "/admin/", http.StatusSeeOther)
}

func (h *Handler) Logout(w http.ResponseWriter, r *http.Request) {
	clearAdminCookie(w)
	http.Redirect(w, r, "/admin/login", http.StatusSeeOther)
}

// --- Dashboard ---

func (h *Handler) Dashboard(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	var totalSlots, totalBookings, totalClients int
	h.db.QueryRow(ctx, `SELECT count(*) FROM slots WHERE status = 'Активен'`).Scan(&totalSlots)
	h.db.QueryRow(ctx, `SELECT count(*) FROM bookings WHERE status = 'Активна'`).Scan(&totalBookings)
	h.db.QueryRow(ctx, `SELECT count(*) FROM clients WHERE deleted_at IS NULL`).Scan(&totalClients)

	recent, _ := h.listAdminBookings(ctx, 10, 0)

	h.render(w, "dashboard", map[string]any{
		"Title":          "Дашборд",
		"IsLoggedIn":     true,
		"TotalSlots":     totalSlots,
		"TotalBookings":  totalBookings,
		"TotalClients":   totalClients,
		"RecentBookings": recent,
	})
}

// --- Slots ---

func (h *Handler) SlotsList(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	list, err := h.slots.List(ctx, postgres.SlotFilters{Limit: 200, Offset: 0})
	if err != nil {
		h.render(w, "slots", map[string]any{
			"Title":      "Слоты",
			"IsLoggedIn": true,
			"Error":      "Ошибка загрузки слотов: " + err.Error(),
			"Slots":      postgres.SlotList{},
		})
		return
	}
	h.render(w, "slots", map[string]any{
		"Title":      "Слоты",
		"IsLoggedIn": true,
		"Slots":      list,
	})
}

func (h *Handler) SlotNew(w http.ResponseWriter, r *http.Request) {
	instructors, err := h.instructors.ListInstructors(r.Context())
	if err != nil {
		h.render(w, "slot_form", map[string]any{
			"Title":       "Новый слот",
			"IsLoggedIn":  true,
			"Error":       "Ошибка загрузки инструкторов: " + err.Error(),
			"IsEdit":      false,
			"Instructors": []postgres.Instructor{},
		})
		return
	}
	h.render(w, "slot_form", map[string]any{
		"Title":       "Новый слот",
		"IsLoggedIn":  true,
		"IsEdit":      false,
		"Instructors": instructors,
	})
}

func (h *Handler) SlotCreate(w http.ResponseWriter, r *http.Request) {
	instructors, _ := h.instructors.ListInstructors(r.Context())
	if err := r.ParseForm(); err != nil {
		h.render(w, "slot_form", map[string]any{
			"Title": "Новый слот", "IsLoggedIn": true, "Error": "Ошибка обработки формы",
			"IsEdit": false, "Instructors": instructors,
		})
		return
	}

	dateTime, err := time.Parse("2006-01-02T15:04", r.FormValue("dateTime"))
	if err != nil {
		h.render(w, "slot_form", map[string]any{
			"Title": "Новый слот", "IsLoggedIn": true, "Error": "Неверный формат даты/времени",
			"IsEdit": false, "Instructors": instructors,
		})
		return
	}
	capacity, err := strconv.Atoi(r.FormValue("capacity"))
	if err != nil || capacity < 1 {
		h.render(w, "slot_form", map[string]any{
			"Title": "Новый слот", "IsLoggedIn": true, "Error": "Неверная вместимость",
			"IsEdit": false, "Instructors": instructors,
		})
		return
	}
	price, err := strconv.ParseFloat(r.FormValue("price"), 64)
	if err != nil || price < 0 {
		h.render(w, "slot_form", map[string]any{
			"Title": "Новый слот", "IsLoggedIn": true, "Error": "Неверная цена",
			"IsEdit": false, "Instructors": instructors,
		})
		return
	}

	slot := postgres.Slot{
		ID:           newUUID(),
		Menu:         r.FormValue("menu"),
		Difficulty:   r.FormValue("difficulty"),
		PhotoUrls:    []string{},
		InstructorID: r.FormValue("instructorId"),
		DateTime:     dateTime,
		Capacity:     capacity,
		Price:        price,
		Address:      r.FormValue("address"),
		Status:       r.FormValue("status"),
		BookedCount:  0,
	}

	if err := h.slots.Create(r.Context(), slot); err != nil {
		h.render(w, "slot_form", map[string]any{
			"Title": "Новый слот", "IsLoggedIn": true, "Error": "Ошибка создания слота: " + err.Error(),
			"IsEdit": false, "Instructors": instructors,
		})
		return
	}

	http.Redirect(w, r, "/admin/slots", http.StatusSeeOther)
}

func (h *Handler) SlotEdit(w http.ResponseWriter, r *http.Request) {
	slotID := chi.URLParam(r, "id")
	slot, found, err := h.slots.GetByID(r.Context(), slotID)
	if err != nil || !found {
		h.render(w, "slot_form", map[string]any{
			"Title":      "Редактировать слот",
			"IsLoggedIn": true,
			"Error":      "Слот не найден",
			"IsEdit":     false,
		})
		return
	}
	instructors, _ := h.instructors.ListInstructors(r.Context())
	h.render(w, "slot_form", map[string]any{
		"Title":       "Редактировать слот",
		"IsLoggedIn":  true,
		"IsEdit":      true,
		"Slot":        slot,
		"Instructors": instructors,
	})
}

func (h *Handler) SlotUpdate(w http.ResponseWriter, r *http.Request) {
	slotID := chi.URLParam(r, "id")
	_, found, err := h.slots.GetByID(r.Context(), slotID)
	if err != nil || !found {
		h.render(w, "slot_form", map[string]any{"Title": "Редактировать слот", "IsLoggedIn": true, "IsEdit": false, "Error": "Слот не найден"})
		return
	}

	instructors, _ := h.instructors.ListInstructors(r.Context())

	if err := r.ParseForm(); err != nil {
		h.render(w, "slot_form", map[string]any{"Title": "Редактировать слот", "IsLoggedIn": true, "IsEdit": true, "Instructors": instructors, "Error": "Ошибка обработки формы"})
		return
	}

	dateTime, err := time.Parse("2006-01-02T15:04", r.FormValue("dateTime"))
	if err != nil {
		h.render(w, "slot_form", map[string]any{"Title": "Редактировать слот", "IsLoggedIn": true, "IsEdit": true, "Instructors": instructors, "Error": "Неверный формат даты/времени"})
		return
	}
	capacity, err := strconv.Atoi(r.FormValue("capacity"))
	if err != nil || capacity < 1 {
		h.render(w, "slot_form", map[string]any{"Title": "Редактировать слот", "IsLoggedIn": true, "IsEdit": true, "Instructors": instructors, "Error": "Неверная вместимость"})
		return
	}
	price, err := strconv.ParseFloat(r.FormValue("price"), 64)
	if err != nil || price < 0 {
		h.render(w, "slot_form", map[string]any{"Title": "Редактировать слот", "IsLoggedIn": true, "IsEdit": true, "Instructors": instructors, "Error": "Неверная цена"})
		return
	}

	slot := postgres.Slot{
		ID:           slotID,
		Menu:         r.FormValue("menu"),
		Difficulty:   r.FormValue("difficulty"),
		PhotoUrls:    []string{},
		InstructorID: r.FormValue("instructorId"),
		DateTime:     dateTime,
		Capacity:     capacity,
		Price:        price,
		Address:      r.FormValue("address"),
		Status:       r.FormValue("status"),
	}

	if err := h.slots.Update(r.Context(), slot); err != nil {
		h.render(w, "slot_form", map[string]any{"Title": "Редактировать слот", "IsLoggedIn": true, "IsEdit": true, "Instructors": instructors, "Slot": slot, "Error": "Ошибка обновления слота: " + err.Error()})
		return
	}

	http.Redirect(w, r, "/admin/slots", http.StatusSeeOther)
}

func (h *Handler) SlotDeleteConfirm(w http.ResponseWriter, r *http.Request) {
	slotID := chi.URLParam(r, "id")
	slot, found, err := h.slots.GetByID(r.Context(), slotID)
	if err != nil || !found {
		h.render(w, "slots", map[string]any{"Title": "Слоты", "IsLoggedIn": true, "Slots": postgres.SlotList{}, "Error": "Слот не найден"})
		return
	}
	list, _ := h.slots.List(r.Context(), postgres.SlotFilters{Limit: 200, Offset: 0})
	h.render(w, "slots", map[string]any{
		"Title":      "Слоты",
		"IsLoggedIn": true,
		"Slots":      list,
		"DeleteSlot": &slot,
	})
}

func (h *Handler) SlotDelete(w http.ResponseWriter, r *http.Request) {
	slotID := chi.URLParam(r, "id")
	if err := h.slots.Delete(r.Context(), slotID); err != nil {
		h.render(w, "slots", map[string]any{"Title": "Слоты", "IsLoggedIn": true, "Slots": postgres.SlotList{}, "Error": "Ошибка удаления слота: " + err.Error()})
		return
	}
	http.Redirect(w, r, "/admin/slots", http.StatusSeeOther)
}

// --- Bookings ---

func (h *Handler) BookingsList(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	bookings, err := h.listAdminBookings(ctx, 200, 0)
	if err != nil {
		h.render(w, "bookings", map[string]any{
			"Title":      "Брони",
			"IsLoggedIn": true,
			"Error":      "Ошибка загрузки броней: " + err.Error(),
			"Bookings":   []AdminBookingRow{},
		})
		return
	}
	h.render(w, "bookings", map[string]any{
		"Title":      "Брони",
		"IsLoggedIn": true,
		"Bookings":   bookings,
	})
}

func (h *Handler) BookingCancel(w http.ResponseWriter, r *http.Request) {
	bookingID := chi.URLParam(r, "id")
	if err := r.ParseForm(); err != nil {
		http.Redirect(w, r, "/admin/bookings", http.StatusSeeOther)
		return
	}
	reason := r.FormValue("reason")
	if reason == "" {
		reason = "Отменена администратором"
	}
	if err := h.bookings.CancelByAdmin(r.Context(), bookingID, reason); err != nil {
		bookings, _ := h.listAdminBookings(r.Context(), 200, 0)
		h.render(w, "bookings", map[string]any{
			"Title":      "Брони",
			"IsLoggedIn": true,
			"Bookings":   bookings,
			"Error":      "Ошибка отмены брони: " + err.Error(),
		})
		return
	}
	http.Redirect(w, r, "/admin/bookings", http.StatusSeeOther)
}

// --- Helpers ---

func (h *Handler) listAdminBookings(ctx context.Context, limit, offset int) ([]AdminBookingRow, error) {
	rows, err := h.db.Query(ctx, `
SELECT b.id::text, b.slot_id::text, b.client_id::text, COALESCE(c.phone, ''),
       b.equipment_type, b.status, b.created_at, b.cancelled_at,
       s.menu, s.start_at, s.address, i.name, s.price
FROM bookings b
JOIN slots s ON s.id = b.slot_id
JOIN clients c ON c.id = b.client_id
JOIN instructors i ON i.id = s.instructor_id
ORDER BY b.created_at DESC
LIMIT $1 OFFSET $2`, limit, offset)
	if err != nil {
		return nil, fmt.Errorf("query admin bookings: %w", err)
	}
	defer rows.Close()

	var result []AdminBookingRow
	for rows.Next() {
		var r AdminBookingRow
		if err := rows.Scan(
			&r.ID, &r.SlotID, &r.ClientID, &r.ClientPhone,
			&r.EquipmentType, &r.Status, &r.CreatedAt, &r.CancelledAt,
			&r.SlotMenu, &r.SlotDateTime, &r.SlotAddress,
			&r.InstructorName, &r.Price,
		); err != nil {
			return nil, fmt.Errorf("scan admin booking: %w", err)
		}
		result = append(result, r)
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate admin bookings: %w", err)
	}
	return result, nil
}

func newUUID() string {
	b := make([]byte, 16)
	_, _ = rand.Read(b)
	b[6] = (b[6] & 0x0f) | 0x40
	b[8] = (b[8] & 0x3f) | 0x80
	return fmt.Sprintf("%08x-%04x-%04x-%04x-%012x", b[0:4], b[4:6], b[6:8], b[8:10], b[10:16])
}
