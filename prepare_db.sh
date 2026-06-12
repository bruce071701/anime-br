#!/bin/bash

# ============================================================
# Prepare anime_digital.db for the app
# ============================================================
# 1. Rebuild animes/episodes tables with id NOT NULL
# 2. Create Room required tables (players, watch_history, favorites, downloads)
# 3. VACUUM optimize
# 4. Compress (gzip) + Encrypt (XOR) → .db.enc
# 5. Remove original .db from assets
#
# Usage:
#   ./prepare_db.sh                          # Default path
#   ./prepare_db.sh /path/to/your.db         # Custom path
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

DB_PATH="${1:-app/src/main/assets/anime_digital.db}"
ENC_PATH="app/src/main/assets/anime_digital.db.enc"

if [ ! -f "$DB_PATH" ]; then
    echo -e "${RED}❌ Database not found: $DB_PATH${NC}"
    exit 1
fi

echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  Prepare Database for AnimeBR${NC}"
echo -e "${GREEN}============================================================${NC}"
echo -e "  File: $DB_PATH"
echo ""

# Step 1: Rebuild animes table with id NOT NULL
echo -e "${YELLOW}▶ Rebuilding animes table (id NOT NULL)...${NC}"
sqlite3 "$DB_PATH" "
    ALTER TABLE animes RENAME TO _animes_old;
    CREATE TABLE animes(
        id INTEGER PRIMARY KEY NOT NULL,
        name TEXT, nameAlternative TEXT, slug TEXT, imagen TEXT,
        overview TEXT, aired TEXT, type TEXT,
        status INTEGER DEFAULT 0, genres TEXT, rating TEXT,
        trailer TEXT, voteAverage TEXT, visitas INTEGER DEFAULT 0,
        isDubbing INTEGER DEFAULT 0, nums INTEGER DEFAULT 0,
        isTopic INTEGER DEFAULT 0, createdAt TEXT
    );
    INSERT OR IGNORE INTO animes SELECT * FROM _animes_old;
    DROP TABLE _animes_old;
"
echo -e "  ${GREEN}✓ animes OK${NC}"

# Step 2: Rebuild episodes table with id NOT NULL
echo -e "${YELLOW}▶ Rebuilding episodes table (id NOT NULL)...${NC}"
sqlite3 "$DB_PATH" "
    ALTER TABLE episodes RENAME TO _episodes_old;
    CREATE TABLE episodes(
        id INTEGER PRIMARY KEY NOT NULL,
        animeId INTEGER, title TEXT, imagen TEXT,
        overview TEXT, url TEXT, visitas INTEGER DEFAULT 0,
        nums TEXT, aired TEXT, status INTEGER DEFAULT 0, createdAt TEXT
    );
    INSERT OR IGNORE INTO episodes SELECT * FROM _episodes_old;
    DROP TABLE _episodes_old;
"
echo -e "  ${GREEN}✓ episodes OK${NC}"

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
echo -e "  ${GREEN}✓ players${NC}"

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
echo -e "  ${GREEN}✓ watch_history${NC}"

# Step 5: Create favorites table
echo -e "${YELLOW}▶ Creating favorites table...${NC}"
sqlite3 "$DB_PATH" "
    DROP TABLE IF EXISTS favorites;
    CREATE TABLE favorites(
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        animeId INTEGER NOT NULL, createdAt INTEGER NOT NULL
    );
"
echo -e "  ${GREEN}✓ favorites${NC}"

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
echo -e "  ${GREEN}✓ downloads${NC}"

# Step 7: VACUUM
echo -e "${YELLOW}▶ Optimizing (VACUUM)...${NC}"
sqlite3 "$DB_PATH" "VACUUM;"
echo -e "  ${GREEN}✓ optimized${NC}"

# Step 8: Verify
echo ""
echo -e "${YELLOW}▶ Verification:${NC}"
ANIME_COUNT=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM animes;")
EPISODE_COUNT=$(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM episodes;")
TABLES=$(sqlite3 "$DB_PATH" ".tables")
DB_SIZE=$(du -h "$DB_PATH" | cut -f1)
echo "  Tables:   $TABLES"
echo "  Animes:   $ANIME_COUNT"
echo "  Episodes: $EPISODE_COUNT"
echo "  Size:     $DB_SIZE"

# Step 9: Compress + Encrypt
echo ""
echo -e "${YELLOW}▶ Compressing and encrypting...${NC}"
python3 << PYEOF
import gzip, os

DB_PATH = "$DB_PATH"
OUT_PATH = "$ENC_PATH"
KEY = b'AnimeBR2026SecretKey!@#'

with open(DB_PATH, 'rb') as f:
    data = f.read()

compressed = gzip.compress(data, compresslevel=9)
key_len = len(KEY)
encrypted = bytes(b ^ KEY[i % key_len] for i, b in enumerate(compressed))

with open(OUT_PATH, 'wb') as f:
    f.write(encrypted)

original_kb = os.path.getsize(DB_PATH) / 1024
encrypted_kb = os.path.getsize(OUT_PATH) / 1024
saved = (1 - encrypted_kb / original_kb) * 100
print(f'  Original:   {original_kb:.1f} KB')
print(f'  Encrypted:  {encrypted_kb:.1f} KB')
print(f'  Saved:      {saved:.1f}%')
PYEOF

# Step 10: Remove original .db
rm -f "$DB_PATH"

echo -e "  ${GREEN}✓ Output: $ENC_PATH${NC}"
echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  ✅ Database ready for packaging!${NC}"
echo -e "${GREEN}============================================================${NC}"
