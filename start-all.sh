#!/bin/bash

# Start all services script

# Colors
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Docker Compose
echo -e "${GREEN}Starting Docker services...${NC}"
docker-compose up -d

# Wait for services to be ready
echo -e "${YELLOW}Waiting for services to be ready...${NC}"
sleep 10

echo -e "${GREEN}All services are running!${NC}"
echo "PostgreSQL: localhost:5432"
echo "Redis: localhost:6379"
echo "MinIO: localhost:9000 (Console: localhost:9001)"
echo "Backend: localhost:8080"
echo "Frontend: localhost:5173"