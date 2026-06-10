#!/bin/bash

# ============================================================
# Anime TV - Build & Install Script
# ============================================================
# Usage:
#   ./install.sh          - Build and install via adb
#   ./install.sh wifi     - Connect via WiFi then install
#   ./install.sh build    - Only build, no install
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  Anime TV - Build & Install${NC}"
echo -e "${GREEN}============================================================${NC}"
echo ""

# Step 1: Build
echo -e "${YELLOW}▶ Building Debug APK...${NC}"
./gradlew assembleDebug --no-daemon -q
cp "$APK_PATH" "./anime-tv-debug.apk"
APK_SIZE=$(du -h "./anime-tv-debug.apk" | cut -f1)
echo -e "${GREEN}✅ Build successful! (${APK_SIZE})${NC}"
echo ""

# Check mode
MODE="${1:-install}"

if [ "$MODE" = "build" ]; then
    echo -e "${GREEN}APK: ./anime-tv-debug.apk${NC}"
    exit 0
fi

# Step 2: Check adb connection
echo -e "${YELLOW}▶ Checking device connection...${NC}"

if [ "$MODE" = "wifi" ]; then
    echo -e "  Enter device IP (e.g. 192.168.1.100): "
    read -r DEVICE_IP
    if [ -z "$DEVICE_IP" ]; then
        echo -e "${RED}❌ No IP provided${NC}"
        exit 1
    fi
    echo -e "  Connecting to ${DEVICE_IP}:5555..."
    adb connect "${DEVICE_IP}:5555" 2>/dev/null || true
    sleep 2
fi

# Check if device is connected
DEVICE_COUNT=$(adb devices 2>/dev/null | grep -c "device$" || echo "0")

if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}❌ No device connected!${NC}"
    echo ""
    echo -e "  Options:"
    echo -e "  1. Connect USB and enable USB debugging"
    echo -e "  2. Run: ${YELLOW}./install.sh wifi${NC} to connect via WiFi"
    echo -e "  3. Manual install: copy ${YELLOW}anime-tv-debug.apk${NC} to phone"
    echo ""
    echo -e "  To enable WiFi debugging on phone:"
    echo -e "    Settings > Developer Options > Wireless debugging > Pair"
    echo -e "    Then: adb pair <ip>:<port>"
    echo -e "    Then: ./install.sh wifi"
    exit 1
fi

echo -e "${GREEN}  Device found!${NC}"
echo ""

# Step 3: Uninstall old version (ignore error if not installed)
echo -e "${YELLOW}▶ Uninstalling old version...${NC}"
adb uninstall com.animebr.app 2>/dev/null || echo -e "  (not previously installed)"

# Step 4: Install
echo -e "${YELLOW}▶ Installing APK...${NC}"
adb install -r "./anime-tv-debug.apk"

echo ""
echo -e "${GREEN}✅ Installed successfully!${NC}"
echo ""

# Step 5: Launch app
echo -e "${YELLOW}▶ Launching app...${NC}"
adb shell am start -n com.animebr.app/.ui.MainActivity
echo -e "${GREEN}✅ App launched!${NC}"
echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  Done! Check your device.${NC}"
echo -e "${GREEN}============================================================${NC}"
