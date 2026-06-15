#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"

echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  Anime TV - Release Build${NC}"
echo -e "${GREEN}============================================================${NC}"

# Check signing
if [ ! -f "keystore.properties" ]; then
    echo -e "${RED}❌ keystore.properties not found!${NC}"
    echo "  Create keystore.properties with:"
    echo "    storeFile=release-keystore.jks"
    echo "    storePassword=xxx"
    echo "    keyAlias=xxx"
    echo "    keyPassword=xxx"
    exit 1
fi

if [ ! -f "release-keystore.jks" ] && [ ! -f "anime_br.jks" ]; then
    echo -e "${RED}❌ Keystore file not found!${NC}"
    echo "  Generate with:"
    echo "    keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my_alias"
    exit 1
fi

# Read version
VERSION_NAME=$(grep 'versionName' app/build.gradle.kts | head -1 | grep -oE '"[^"]+"' | tr -d '"')
VERSION_CODE=$(grep 'versionCode' app/build.gradle.kts | head -1 | grep -oE '[0-9]+')
echo -e "  Version: ${YELLOW}${VERSION_NAME}${NC} (code: ${VERSION_CODE})"
echo ""

# Clean
echo -e "${YELLOW}▶ Cleaning...${NC}"
./gradlew clean --no-daemon -q

# Build AAB
echo -e "${YELLOW}▶ Building Release AAB...${NC}"
./gradlew bundleRelease --no-daemon -q

# Build APK
echo -e "${YELLOW}▶ Building Release APK...${NC}"
./gradlew assembleRelease --no-daemon -q

# Output directory
OUTPUT_DIR="release_output/v${VERSION_NAME}"
mkdir -p "$OUTPUT_DIR"

# Copy artifacts
AAB="app/build/outputs/bundle/release/app-release.aab"
APK="app/build/outputs/apk/release/app-release.apk"

if [ -f "$AAB" ]; then
    cp "$AAB" "$OUTPUT_DIR/AnimeBR-v${VERSION_NAME}.aab"
    echo -e "  ${GREEN}✓ AAB: $OUTPUT_DIR/AnimeBR-v${VERSION_NAME}.aab ($(du -h "$AAB" | cut -f1))${NC}"
fi

if [ -f "$APK" ]; then
    cp "$APK" "$OUTPUT_DIR/AnimeBR-v${VERSION_NAME}.apk"
    echo -e "  ${GREEN}✓ APK: $OUTPUT_DIR/AnimeBR-v${VERSION_NAME}.apk ($(du -h "$APK" | cut -f1))${NC}"
fi

echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  ✅ Release build complete!${NC}"
echo -e "${GREEN}  Output: ${OUTPUT_DIR}/${NC}"
echo -e "${GREEN}============================================================${NC}"
