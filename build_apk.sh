#!/bin/bash

# ============================================================
# Anime TV (anime-br) Build Script
# ============================================================
# Usage:
#   ./build_apk.sh          - Build debug APK
#   ./build_apk.sh release  - Build release APK (needs signing config)
#   ./build_apk.sh bundle   - Build release AAB (for Google Play)
#   ./build_apk.sh clean    - Clean build cache
# ============================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Project root
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# Java home (use JDK 17)
if [ -d "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
elif [ -n "$JAVA_HOME" ]; then
    echo -e "${YELLOW}Using system JAVA_HOME: $JAVA_HOME${NC}"
else
    echo -e "${RED}Error: JAVA_HOME not set. Please install JDK 17.${NC}"
    exit 1
fi

echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  Anime TV - Build Script${NC}"
echo -e "${GREEN}============================================================${NC}"
echo -e "  JAVA_HOME: $JAVA_HOME"
echo -e "  Project:   $PROJECT_DIR"
echo ""

# Build type
BUILD_TYPE="${1:-debug}"

case "$BUILD_TYPE" in
    debug)
        echo -e "${YELLOW}▶ Building Debug APK...${NC}"
        ./gradlew assembleDebug --no-daemon

        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
        if [ -f "$APK_PATH" ]; then
            APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
            echo ""
            echo -e "${GREEN}✅ Build successful!${NC}"
            echo -e "   APK: $APK_PATH"
            echo -e "   Size: $APK_SIZE"

            # Copy to project root for convenience
            cp "$APK_PATH" "./anime-tv-debug.apk"
            echo -e "   Copied to: ./anime-tv-debug.apk"
        else
            echo -e "${RED}❌ Build failed - APK not found${NC}"
            exit 1
        fi
        ;;

    release)
        echo -e "${YELLOW}▶ Building Release APK...${NC}"
        echo ""

        # Check for keystore
        KEYSTORE_PATH="keystore/release.jks"
        if [ ! -f "$KEYSTORE_PATH" ]; then
            echo -e "${YELLOW}⚠️  No keystore found at $KEYSTORE_PATH${NC}"
            echo -e "   Creating a debug-signed release build instead."
            echo -e "   To sign properly, create keystore/release.jks and set:"
            echo -e "     KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD"
            echo ""
            ./gradlew assembleRelease --no-daemon

            APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
        else
            # Build with signing
            if [ -z "$KEYSTORE_PASSWORD" ] || [ -z "$KEY_ALIAS" ] || [ -z "$KEY_PASSWORD" ]; then
                echo -e "${RED}❌ Missing signing environment variables:${NC}"
                echo -e "   KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD"
                exit 1
            fi

            ./gradlew assembleRelease \
                -Pandroid.injected.signing.store.file="$PROJECT_DIR/$KEYSTORE_PATH" \
                -Pandroid.injected.signing.store.password="$KEYSTORE_PASSWORD" \
                -Pandroid.injected.signing.key.alias="$KEY_ALIAS" \
                -Pandroid.injected.signing.key.password="$KEY_PASSWORD" \
                --no-daemon

            APK_PATH="app/build/outputs/apk/release/app-release.apk"
        fi

        if [ -f "$APK_PATH" ]; then
            APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
            echo ""
            echo -e "${GREEN}✅ Build successful!${NC}"
            echo -e "   APK: $APK_PATH"
            echo -e "   Size: $APK_SIZE"

            cp "$APK_PATH" "./anime-tv-release.apk"
            echo -e "   Copied to: ./anime-tv-release.apk"
        else
            echo -e "${RED}❌ Build failed - APK not found${NC}"
            exit 1
        fi
        ;;

    bundle)
        echo -e "${YELLOW}▶ Building Release AAB (App Bundle)...${NC}"
        echo ""

        KEYSTORE_PATH="keystore/release.jks"
        if [ ! -f "$KEYSTORE_PATH" ]; then
            echo -e "${YELLOW}⚠️  No keystore found. Building unsigned bundle.${NC}"
            ./gradlew bundleRelease --no-daemon
        else
            if [ -z "$KEYSTORE_PASSWORD" ] || [ -z "$KEY_ALIAS" ] || [ -z "$KEY_PASSWORD" ]; then
                echo -e "${RED}❌ Missing signing environment variables${NC}"
                exit 1
            fi

            ./gradlew bundleRelease \
                -Pandroid.injected.signing.store.file="$PROJECT_DIR/$KEYSTORE_PATH" \
                -Pandroid.injected.signing.store.password="$KEYSTORE_PASSWORD" \
                -Pandroid.injected.signing.key.alias="$KEY_ALIAS" \
                -Pandroid.injected.signing.key.password="$KEY_PASSWORD" \
                --no-daemon
        fi

        AAB_PATH="app/build/outputs/bundle/release/app-release.aab"
        if [ -f "$AAB_PATH" ]; then
            AAB_SIZE=$(du -h "$AAB_PATH" | cut -f1)
            echo ""
            echo -e "${GREEN}✅ Build successful!${NC}"
            echo -e "   AAB: $AAB_PATH"
            echo -e "   Size: $AAB_SIZE"

            cp "$AAB_PATH" "./anime-tv-release.aab"
            echo -e "   Copied to: ./anime-tv-release.aab"
        else
            echo -e "${RED}❌ Build failed - AAB not found${NC}"
            exit 1
        fi
        ;;

    clean)
        echo -e "${YELLOW}▶ Cleaning build cache...${NC}"
        ./gradlew clean --no-daemon
        rm -f anime-tv-debug.apk anime-tv-release.apk anime-tv-release.aab
        echo -e "${GREEN}✅ Clean complete${NC}"
        ;;

    *)
        echo -e "${RED}Unknown build type: $BUILD_TYPE${NC}"
        echo ""
        echo "Usage:"
        echo "  ./build_apk.sh          - Build debug APK"
        echo "  ./build_apk.sh release  - Build release APK"
        echo "  ./build_apk.sh bundle   - Build release AAB"
        echo "  ./build_apk.sh clean    - Clean build cache"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  Done!${NC}"
echo -e "${GREEN}============================================================${NC}"
