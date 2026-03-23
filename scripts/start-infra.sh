#!/bin/bash
# Start infrastructure services (PostgreSQL, MongoDB, Redis, MinIO)
cd "$(dirname "$0")/../docker"
docker compose up -d postgres mongodb redis minio
echo "Infrastructure services started."
echo "PostgreSQL: localhost:5432"
echo "MongoDB:    localhost:27017"
echo "Redis:      localhost:6379"
echo "MinIO:      localhost:9000 (API), localhost:9001 (Console)"
