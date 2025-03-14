#!/bin/bash

# Database management script

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Default values
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="chatapp_db"
DB_USER="postgres"
DB_PASSWORD="postgres"

# Usage function
function usage {
    echo -e "${YELLOW}Usage:${NC} $0 [options] command"
    echo -e "${YELLOW}Commands:${NC}"
    echo "  create      Create database"
    echo "  drop        Drop database"
    echo "  reset       Drop and recreate database"
    echo "  backup      Backup database to file"
    echo "  restore     Restore database from file"
    echo -e "${YELLOW}Options:${NC}"
    echo "  -h, --host      Database host (default: $DB_HOST)"
    echo "  -p, --port      Database port (default: $DB_PORT)"
    echo "  -d, --dbname    Database name (default: $DB_NAME)"
    echo "  -u, --user      Database user (default: $DB_USER)"
    echo "  -w, --password  Database password (default: $DB_PASSWORD)"
    echo "  -f, --file      Backup/restore filename (default: backup_YYYY-MM-DD.sql)"
    echo "  --help          Show this help message"
    exit 1
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        create|drop|reset|backup|restore)
            COMMAND="$1"
            shift
            ;;
        -h|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -p|--port)
            DB_PORT="$2"
            shift 2
            ;;
        -d|--dbname)
            DB_NAME="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -w|--password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        -f|--file)
            BACKUP_FILE="$2"
            shift 2
            ;;
        --help)
            usage
            ;;
        *)
            echo -e "${RED}Error:${NC} Unknown option $1"
            usage
            ;;
    esac
done

# Set default backup filename if not specified
if [ -z "$BACKUP_FILE" ]; then
    BACKUP_FILE="backup_$(date +%Y-%m-%d).sql"
fi

# Set PGPASSWORD environment variable
export PGPASSWORD="$DB_PASSWORD"

# Execute command
case "$COMMAND" in
    create)
        echo -e "${GREEN}Creating database $DB_NAME...${NC}"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "CREATE DATABASE $DB_NAME;"
        echo -e "${GREEN}Database created successfully.${NC}"
        ;;
    drop)
        echo -e "${YELLOW}Warning:${NC} This will drop database $DB_NAME. All data will be lost."
        read -p "Are you sure? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${GREEN}Dropping database $DB_NAME...${NC}"
            psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "DROP DATABASE IF EXISTS $DB_NAME;"
            echo -e "${GREEN}Database dropped successfully.${NC}"
        fi
        ;;
    reset)
        echo -e "${YELLOW}Warning:${NC} This will reset database $DB_NAME. All data will be lost."
        read -p "Are you sure? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${GREEN}Resetting database $DB_NAME...${NC}"
            psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "DROP DATABASE IF EXISTS $DB_NAME;"
            psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -c "CREATE DATABASE $DB_NAME;"
            echo -e "${GREEN}Database reset successfully.${NC}"
        fi
        ;;
    backup)
        echo -e "${GREEN}Backing up database $DB_NAME to $BACKUP_FILE...${NC}"
        pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -F p "$DB_NAME" > "$BACKUP_FILE"
        echo -e "${GREEN}Database backed up successfully.${NC}"
        ;;
    restore)
        echo -e "${YELLOW}Warning:${NC} This will overwrite data in database $DB_NAME with data from $BACKUP_FILE."
        read -p "Are you sure? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${GREEN}Restoring database $DB_NAME from $BACKUP_FILE...${NC}"
            psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$BACKUP_FILE"
            echo -e "${GREEN}Database restored successfully.${NC}"
        fi
        ;;
    *)
        echo -e "${RED}Error:${NC} No command specified"
        usage
        ;;
esac

# Cleanup
unset PGPASSWORD