#!/bin/bash

# Stop all services script

# Colors
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Docker Compose
echo -e "${GREEN}Stopping all services...${NC}"
docker-compose down

echo -e "${GREEN}All services stopped.${NC}"