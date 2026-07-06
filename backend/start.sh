#!/bin/bash
export DATABASE_URL="postgres://chef:chef@localhost:5432/chef_table?sslmode=disable"
export HTTP_ADDR=":8080"
export SHUTDOWN_TIMEOUT="10"
exec go run ./cmd/api
