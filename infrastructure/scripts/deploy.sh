#!/bin/bash
# Create database if it doesn't exist
psql -h localhost -U postgres -c "CREATE DATABASE taskmanagement" || true

# Run migrations with Flyway
./gradlew flywayMigrate
