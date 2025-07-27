#!/bin/bash
# First-time setup script for Finance Monkey

# Terminal colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if .env file exists, if not, create from example
if [ ! -f ".env" ]; then
  echo -e "${YELLOW}Creating .env file from example...${NC}"
  cp .env.example .env
  echo -e "${GREEN}.env file created. Please edit it with your actual credentials.${NC}"
else
  echo -e "${GREEN}.env file already exists.${NC}"
fi

# Create Google credentials directory if it doesn't exist
CREDS_DIR="./backend/src/main/resources/credentials"
if [ ! -d "$CREDS_DIR" ]; then
  echo -e "${YELLOW}Creating credentials directory...${NC}"
  mkdir -p "$CREDS_DIR"
fi

# Check if credentials.json exists
CREDS_FILE="$CREDS_DIR/credentials.json"
if [ ! -f "$CREDS_FILE" ]; then
  echo -e "${YELLOW}Google credentials file not found.${NC}"
  
  echo -e "${BLUE}Would you like to:${NC}"
  echo "1. Create a placeholder credentials file (for testing)"
  echo "2. Skip (you'll need to add it manually later)"
  read -p "Enter choice [1-2]: " choice
  
  if [ "$choice" == "1" ]; then
    echo '{
  "installed": {
    "client_id": "YOUR_CLIENT_ID",
    "project_id": "finance-monkey",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_secret": "YOUR_CLIENT_SECRET",
    "redirect_uris": ["urn:ietf:wg:oauth:2.0:oob", "http://localhost"]
  }
}' > "$CREDS_FILE"
    echo -e "${GREEN}Placeholder credentials file created.${NC}"
    echo -e "${YELLOW}Please replace with your actual Google API credentials before using Drive features.${NC}"
  else
    echo -e "${YELLOW}Skipping credentials file creation.${NC}"
    echo -e "${YELLOW}You will need to manually add credentials.json before using Drive features.${NC}"
  fi
else
  echo -e "${GREEN}Google credentials file found.${NC}"
fi

echo -e "\n${BLUE}===================================================${NC}"
echo -e "${BLUE}    Ready to run Finance Monkey application!${NC}"
echo -e "${BLUE}===================================================${NC}"
echo -e "\nRun the following command to start the application:"
echo -e "${GREEN}./run-local.sh${NC}"
