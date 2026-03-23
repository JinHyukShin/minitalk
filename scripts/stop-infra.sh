#!/bin/bash
# Stop infrastructure services
cd "$(dirname "$0")/../docker"
docker compose down
echo "Infrastructure services stopped."
