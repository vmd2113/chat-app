#!/bin/bash

# Start Docker containers
docker-compose up -d postgres redis minio

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 5

echo "Environment is ready!"
echo "PostgreSQL: localhost:5432"
echo "Redis: localhost:6379"
echo "MinIO: localhost:9000 (Console: localhost:9001)"
echo "MinIO credentials: minioadmin / minioadmin"