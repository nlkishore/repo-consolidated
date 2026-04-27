#!/bin/bash
# ========================================================================
# UOB Turbine7 Portal MM - Automated Database Setup Script (Bash)
# ========================================================================
# This script automates the complete database setup process:
#   1. Creates database and user
#   2. Creates all tables
#   3. Loads test data
#   4. Verifies setup
# ========================================================================
# Usage:
#   chmod +x setup-database.sh
#   ./setup-database.sh
# ========================================================================
# Requirements:
#   - MySQL 8.1 installed and running
#   - MySQL root access
#   - Bash shell
# ========================================================================

set -e  # Exit on error

# Configuration
DATABASE_NAME="kishore"
DATABASE_USER="kishore"
DATABASE_PASSWORD="Kish1381@"
MYSQL_CMD="mysql"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================"
echo -e "UOB Turbine7 Portal MM - Database Setup"
echo -e "========================================${NC}"
echo ""

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if MySQL is available
echo -e "${YELLOW}Checking MySQL installation...${NC}"
if ! command -v $MYSQL_CMD &> /dev/null; then
    echo -e "${RED}✗ ERROR: MySQL not found in PATH${NC}"
    echo -e "${RED}  Please install MySQL or add it to your PATH${NC}"
    exit 1
fi

MYSQL_VERSION=$($MYSQL_CMD --version)
echo -e "${GREEN}✓ MySQL found: $MYSQL_VERSION${NC}"

# Get MySQL root password
echo ""
read -sp "Enter MySQL root password: " ROOT_PASSWORD
echo ""

# Function to execute SQL script
execute_sql_script() {
    local script_file=$1
    local user=$2
    local password=$3
    local database=$4
    
    if [ ! -f "$script_file" ]; then
        echo -e "${RED}✗ ERROR: Script file not found: $script_file${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Executing: $script_file...${NC}"
    
    if [ -z "$database" ]; then
        $MYSQL_CMD -u "$user" -p"$password" < "$script_file" 2>&1
    else
        $MYSQL_CMD -u "$user" -p"$password" "$database" < "$script_file" 2>&1
    fi
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Success: $script_file${NC}"
        return 0
    else
        echo -e "${RED}✗ ERROR: Failed to execute $script_file${NC}"
        return 1
    fi
}

# Step 1: Create Database and User
echo ""
echo -e "${CYAN}Step 1: Creating database and user...${NC}"
if execute_sql_script "01-create-database-and-user.sql" "root" "$ROOT_PASSWORD" ""; then
    echo -e "${GREEN}✓ Database and user created successfully${NC}"
else
    echo -e "${RED}✗ Failed to create database and user${NC}"
    exit 1
fi

# Step 2: Create Tables
echo ""
echo -e "${CYAN}Step 2: Creating tables...${NC}"
if execute_sql_script "02-create-tables.sql" "$DATABASE_USER" "$DATABASE_PASSWORD" "$DATABASE_NAME"; then
    echo -e "${GREEN}✓ Tables created successfully${NC}"
else
    echo -e "${RED}✗ Failed to create tables${NC}"
    exit 1
fi

# Step 3: Load Test Data
echo ""
echo -e "${CYAN}Step 3: Loading test data...${NC}"
if execute_sql_script "03-load-test-data.sql" "$DATABASE_USER" "$DATABASE_PASSWORD" "$DATABASE_NAME"; then
    echo -e "${GREEN}✓ Test data loaded successfully${NC}"
else
    echo -e "${RED}✗ Failed to load test data${NC}"
    exit 1
fi

# Step 4: Verify Setup
echo ""
echo -e "${CYAN}Step 4: Verifying setup...${NC}"
if execute_sql_script "04-verify-setup.sql" "$DATABASE_USER" "$DATABASE_PASSWORD" "$DATABASE_NAME"; then
    echo -e "${GREEN}✓ Setup verification completed${NC}"
else
    echo -e "${YELLOW}⚠ Warning: Verification script had issues (check output above)${NC}"
fi

# Summary
echo ""
echo -e "${CYAN}========================================"
echo -e "Database Setup Complete!${GREEN}"
echo -e "${CYAN}========================================${NC}"
echo ""
echo -e "${YELLOW}Database Configuration:${NC}"
echo -e "  Database: $DATABASE_NAME"
echo -e "  User: $DATABASE_USER"
echo -e "  Password: $DATABASE_PASSWORD"
echo ""
echo -e "${YELLOW}Test User Credentials:${NC}"
echo -e "  Username: admin       Password: password123"
echo -e "  Username: manager1   Password: password123"
echo -e "  Username: user1      Password: password123"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo -e "  1. Verify database connection in application"
echo -e "  2. Start Tomcat server"
echo -e "  3. Access application at http://localhost:8081/uob-t7-portal-mm-tomcat"
echo ""
