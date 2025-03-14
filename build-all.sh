#!/bin/bash

# Build all services script

# Colors
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Build Backend
echo -e "${GREEN}Building backend service...${NC}"
cd backend
if ./mvnw clean package -DskipTests; then
    echo -e "${GREEN}Backend built successfully.${NC}"
else
    echo -e "${RED}Failed to build backend!${NC}"
    exit 1
fi
cd ..

# Build Frontend
echo -e "${GREEN}Building frontend service...${NC}"
cd frontend
if npm ci && npm run build; then
    echo -e "${GREEN}Frontend built successfully.${NC}"
else
    echo -e "${RED}Failed to build frontend!${NC}"
    exit 1
fi
cd ..

# Build Docker images
echo -e "${GREEN}Building Docker images...${NC}"
if docker-compose build; then
    echo -e "${GREEN}Docker images built successfully.${NC}"
else
    echo -e "${RED}Failed to build Docker images!${NC}"
    exit 1
fi

echo -e "${GREEN}All services built successfully!${NC}"
echo -e "${YELLOW}You can now start the services with:${NC} ./start-all.sh"