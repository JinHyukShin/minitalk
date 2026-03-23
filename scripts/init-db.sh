#!/bin/bash
# Initialize the database
cd "$(dirname "$0")/.."
PGPASSWORD=minitalk123 psql -h localhost -U minitalk -d minitalk -f src/main/resources/database/migration/V1__init.sql
echo "Database initialized."
