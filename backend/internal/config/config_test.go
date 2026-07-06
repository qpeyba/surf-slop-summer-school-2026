package config

import (
	"os"
	"testing"
	"time"
)

func TestLoadDefaults(t *testing.T) {
	os.Unsetenv("HTTP_ADDR")
	os.Unsetenv("DATABASE_URL")
	os.Unsetenv("SHUTDOWN_TIMEOUT")

	cfg, err := Load()
	if err != nil {
		t.Fatalf("Load() unexpected error: %v", err)
	}
	if cfg.HTTPAddr != ":8080" {
		t.Errorf("HTTPAddr = %q, want :8080", cfg.HTTPAddr)
	}
	if cfg.DatabaseURL != "postgres://volna:volna@localhost:5432/volna?sslmode=disable" {
		t.Errorf("DatabaseURL = %q, want default fallback", cfg.DatabaseURL)
	}
	if cfg.ShutdownTimeout != 10*time.Second {
		t.Errorf("ShutdownTimeout = %v, want 10s", cfg.ShutdownTimeout)
	}
}

func TestLoadFromEnv(t *testing.T) {
	os.Setenv("HTTP_ADDR", ":9090")
	os.Setenv("DATABASE_URL", "postgres://chef:chef@localhost:5432/chef_table?sslmode=disable")
	os.Setenv("SHUTDOWN_TIMEOUT", "30")
	defer func() {
		os.Unsetenv("HTTP_ADDR")
		os.Unsetenv("DATABASE_URL")
		os.Unsetenv("SHUTDOWN_TIMEOUT")
	}()

	cfg, err := Load()
	if err != nil {
		t.Fatalf("Load() unexpected error: %v", err)
	}
	if cfg.HTTPAddr != ":9090" {
		t.Errorf("HTTPAddr = %q, want :9090", cfg.HTTPAddr)
	}
	if cfg.DatabaseURL != "postgres://chef:chef@localhost:5432/chef_table?sslmode=disable" {
		t.Errorf("DatabaseURL = %q, want chef_table URL", cfg.DatabaseURL)
	}
	if cfg.ShutdownTimeout != 30*time.Second {
		t.Errorf("ShutdownTimeout = %v, want 30s", cfg.ShutdownTimeout)
	}
}

func TestLoadInvalidShutdownTimeout(t *testing.T) {
	os.Setenv("SHUTDOWN_TIMEOUT", "0")
	defer os.Unsetenv("SHUTDOWN_TIMEOUT")

	_, err := Load()
	if err == nil {
		t.Fatal("Load() should fail for SHUTDOWN_TIMEOUT=0")
	}
}

func TestLoadNegativeShutdownTimeout(t *testing.T) {
	os.Setenv("SHUTDOWN_TIMEOUT", "-5")
	defer os.Unsetenv("SHUTDOWN_TIMEOUT")

	_, err := Load()
	if err == nil {
		t.Fatal("Load() should fail for negative SHUTDOWN_TIMEOUT")
	}
}

func TestLoadNonNumericShutdownTimeout(t *testing.T) {
	os.Setenv("SHUTDOWN_TIMEOUT", "abc")
	defer os.Unsetenv("SHUTDOWN_TIMEOUT")

	_, err := Load()
	if err == nil {
		t.Fatal("Load() should fail for non-numeric SHUTDOWN_TIMEOUT")
	}
}

func TestLoadEmptyHttpAddr(t *testing.T) {
	os.Setenv("HTTP_ADDR", "")
	os.Unsetenv("DATABASE_URL")
	os.Unsetenv("SHUTDOWN_TIMEOUT")
	defer os.Unsetenv("HTTP_ADDR")

	cfg, err := Load()
	if err != nil {
		t.Fatalf("Load() unexpected error: %v", err)
	}
	if cfg.HTTPAddr != ":8080" {
		t.Errorf("HTTPAddr = %q, want :8080 fallback", cfg.HTTPAddr)
	}
}
