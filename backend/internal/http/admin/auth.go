package admin

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"net/http"
	"strings"
	"time"
)

const (
	adminSecret = "admin-dev-secret-key-2026"
	adminPhone  = "+79999999999"
)

func signPhone(phone string) string {
	mac := hmac.New(sha256.New, []byte(adminSecret))
	mac.Write([]byte(phone))
	return base64.RawStdEncoding.EncodeToString(mac.Sum(nil))
}

func setAdminCookie(w http.ResponseWriter) {
	sig := signPhone(adminPhone)
	value := adminPhone + "." + sig
	http.SetCookie(w, &http.Cookie{
		Name:     "admin_session",
		Value:    value,
		Expires:  time.Now().Add(24 * time.Hour),
		HttpOnly: true,
		Path:     "/",
		SameSite: http.SameSiteLaxMode,
	})
}

func clearAdminCookie(w http.ResponseWriter) {
	http.SetCookie(w, &http.Cookie{
		Name:     "admin_session",
		Value:    "",
		Expires:  time.Unix(0, 0),
		HttpOnly: true,
		Path:     "/",
	})
}

func isAdmin(r *http.Request) bool {
	cookie, err := r.Cookie("admin_session")
	if err != nil {
		return false
	}
	parts := strings.SplitN(cookie.Value, ".", 2)
	if len(parts) != 2 {
		return false
	}
	phone, sig := parts[0], parts[1]
	if phone != adminPhone {
		return false
	}
	expected := signPhone(phone)
	return hmac.Equal([]byte(sig), []byte(expected))
}

func requireAdmin(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if !isAdmin(r) {
			http.Redirect(w, r, "/admin/login", http.StatusSeeOther)
			return
		}
		next(w, r)
	}
}
