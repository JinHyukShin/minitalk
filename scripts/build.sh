#!/bin/bash
# Build the application
cd "$(dirname "$0")/.."
./gradlew clean build -x test
echo "Build complete."
