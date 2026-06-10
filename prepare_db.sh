#!/bin/bash

# ============================================================
# Prepare anime_digital.db for the app
# ============================================================
# This script:
# 1. Adds missing tables required by Room (players, watch_history, favorites, downloads)
# 2. Ensures id columns have NOT NULL constraint
# 3. Verifies the database is ready for packaging
#
# Usage:
#   ./prepare_db.sh                          # Process default path
#   ./prepare_db.sh /path/to/your.db         # Process custom db file
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

DB_PATH="${1:-app/src/main/assets/anime_digital.db}"

if [ ! -f "$DB_PATH" ]; then
    echo -e "${RED}❌ Database not found: $DB_PATH${NC}"
    exit 1
fi

echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  Prepare Database for AnimeBR${NC}"
echo -e "${GREEN}============================================================${NC}"
echo -e "  File: $DB_PATH"
echo ""

# Step 1: Check and fix animes table (ensure id NOT NULL)
echo -e "${YELLOW}▶ Checking animes table...${NC}"
HAS_NOT_NULL=$(sqlite3 "$DB_PATH" ".schema animes" | grep -c "NOT NULL" || echo "0")
if [ "$HAS_NOT_NULL" -eq "0" ]; then
    echo "  Rebuilding animes with id NOT NULL..."
    sqlite3 "$DB_PATH" "
        ALTER TABLE animes RENAME TO animes_old;
        CREATE TABLE animes(
            id INTEGER PRIMARY KEY NOT NULL,
            name TEXT, nameAlternative TEXT, slug TEXT, imagen TEXT,
            overview TEXT, aired TEXT, type TEXT,
            status INTEGER DEFAULT 0, genres TEXT, rating TEXT,
            trailer TEXT, voteAverage TEXT, visitas INTEGER DEFAULT 0,
            isDubbing INTEGER DEFAULT 0, nums INTEGER DEFAULT 0,
            isTopic INTEGER DEFAULT 0, createdAt TEXT
        );
        INSERT INTO animes SELECT * FROM animes_old;
        DROP TABLE animes_old;
    "
    echo -e "  ${GREEN}✓ animes rebuilt${NC}"
else
    echo -e "  ${GREEN}✓ animes OK${NC}"
fi

# Step 2: Check and fix episodes table
echo -e "${YELLOW}▶ Checking episodes table...${NC}"
HAS_NOT_NULL=$(sqlite3 "$DB_PATH" ".schema episodes" | grep -c "id INTEGER PRIMARY KEY NOT NULL" || echo "0")
if [ "$HAS_NOT_NULL" -eq "0" ]; then
    echo "  Rebuilding episodes with id NOT NULL..."
    sqlite3 "$DB_PATH" "
        ALTER TABLE episodes RENAME TO episodes_old;
        CREATE TABLE episodes(
            id INTEGER PRIMARY KEY NOT NULL,
            animeId INTEGER, title TEXT, imagen TEXT,
            overview TEXT, url TEXT, visitas INTEGER DEFAULT 0,
            nums TEXT, aired TEXT, status INTEGER DEFAULT 0, createdAt TEXT
        );
        INSERT INTO episodes SELECT * FROM episodes_old;
        DROP TABLE episodes_old;
    "
    echo -e "  ${GREEN}✓ episodes rebuilt${NC}"
else
    echo -e "  ${GREEN}✓ episodes OK${NC}"
fi

# Step 3: Create players table
echo -e "${YELLOW}▶ Creating players table...${NC}"
sqlite3 "$DB_PATH" "
    DROP TABLE IF EXISTS players;
    CREATE TABLE players(
        id INTEGER PRIMARY KEY NOT NULL,
        animeId INTEGER, episodeId INTEGER,
        link TEXT, server TEXT, embed TEXT,
        status INTEGER DEFAULT 0, createdAt TEXT
    );
"
echo -e "  ${GREEN}✓ players created${NC}"

# Step 4: Create watch_history table
echo -e "${YELLOW}▶ Creating watch_history table...${NC}"
sqlite3 "$DB_PATH" "
    DROP TABLE IF EXISTS watch_history;
    CREATE TABLE watch_history(
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        animeId INTEGER NOT NULL, episodeId INTEGER NOT NULL,
        episodeNumber INTEGER NOT NULL, progress INTEGER NOT NULL,
        duration INTEGER NOT NULL, lastWatchedAt INTEGER NOT NULL
    );
"
echo -e "  ${GREEN}✓ watch_history created${NC}"

# Step 5: Create favorites table
echo -e "${YELLOW}▶ Creating favorites table...${NC}"
sqlite3 "$DB_PATH" "
    DROP TABLE IF EXISTS favorites;
    CREATE TABLE favorites(
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        animeId INTEGER NOT NULL, createdAt INTEGER NOT NULL
    );
"
echo -e "  ${GREEN}✓ favorites created${NC}"

# Step 6: Create downloads table
echo -e "${YELLOW}▶ Creating downloads table...${NC}"
sqlite3 "$DB_PATH" "
    DROP TABLE IF EXISTS downloads;
    CREATE TABLE downloads(
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        animeId INTEGER NOT NULL, episodeId INTEGER NOT NULL,
        animeName TEXT, episodeTitle TEXT, episodeNumber INTEGER NOT NULL,
        videoUrl TEXT NOT NULL, filePath TEXT,
        fileSize INTEGER NOT NULL DEFAULT 0,
        downloadedSize INTEGER NOT NULL DEFAULT 0,
        status INTEGER NOT NULL DEFAULT 0,
        createdAt INTEGER NOT NULL DEFAULT 0
    );
"
echo -e "  ${GREEN}✓ downloads created${NC}"

# Step 7: Vacuum to optimize
echo -e "${YELLOW}▶ Optimizing database...${NC}"
sqlite3 "$DB_PATH" "VACUUM;"
echo -e "  ${GREEN}✓ optimized${NC}"

# Step 8: Verify
echo ""
echo -e "${YELLOW}▶ Verification:${NC}"
TABLES=$(sqlite3 "$DB_PATH" ".tables")
echo "  Tables: $TABLES"
ANIME_COUNT=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM animes;")
EPISODE_COUNT=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM episodes;")
echo "  Animes: $ANIME_COUNT"
echo "  Episodes: $EPISODE_COUNT"
DB_SIZE=$(du -h "$DB_PATH" | cut -f1)
echo "  File size: $DB_SIZE"

echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  ✅ Database ready!${NC}"
echo -e "${GREEN}============================================================${NC}"

# Step 9: Compress and encrypt for packaging
echo ""
echo -e "${YELLOW}▶ Compressing and encrypting for APK...${NC}"
ENCRYPTED_PATH="${DB_PATH}.enc"
# Compress with gzip then XOR encrypt with key
python3 -c "
import gzip, sys, os

DB_PATH = '$DB_PATH'
OUT_PATH = '$ENCRYPTED_PATH'
KEY = b'AnimeBR2026SecretKey!@#'  # XOR key

# Read and compress
with open(DB_PATH, 'rb') as f:
    data = f.read()
compressed = gzip.compress(data, compresslevel=9)

# XOR encrypt
key_len = len(KEY)
encrypted = bytes(b ^ KEY[i % key_len] for i, b in enumerate(compressed))

# Write
with open(OUT_PATH, 'wb') as f:
    f.write(encrypted)

original_size = os.path.getsize(DB_PATH)
encrypted_size = os.path.getsize(OUT_PATH)
ratio = (1 - encrypted_size / original_size) * 100
print(f'  Original:   {original_size / 1024:.1f} KB')
print(f'  Compressed: {encrypted_size / 1024:.1f} KB')
print(f'  Saved:      {ratio:.1f}%')
"
# Move encrypted file to assets, remove original
mv "$ENCRYPTED_PATH" "app/src/main/assets/anime_digital.db.enc"
rm -f "$DB_PATH"
echo -e "  ${GREEN}✓ Saved as app/src/main/assets/anime_digital.db.enc${NC}"
echo -e "  ${GREEN}✓ Original .db removed from assets (not needed in APK)${NC}"
