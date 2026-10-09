#!/usr/bin/env bash
# Replaces the app's data on a connected emulator/device with scripts/seed.sql.
# The current database is backed up to build/db-backups/<timestamp>/ first.
# Requires: adb, sqlite3, and a debug build of the app installed (run-as needs a debuggable app).
set -euo pipefail

PKG=com.example.habittracker
DB=habit_tracker.db
EXPECTED_VERSION=1
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ADB="${ADB:-$(command -v adb || echo "$HOME/Library/Android/sdk/platform-tools/adb")}"
BACKUP="$ROOT/build/db-backups/$(date +%Y%m%d-%H%M%S)"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

"$ADB" shell am force-stop "$PKG"

if ! "$ADB" shell run-as "$PKG" ls "databases/$DB" >/dev/null 2>&1; then
  echo "No database yet. Open the app once so Room creates it, then rerun." >&2
  exit 1
fi

mkdir -p "$BACKUP"
for f in "$DB" "$DB-wal" "$DB-shm"; do
  "$ADB" exec-out run-as "$PKG" cat "databases/$f" > "$BACKUP/$f" 2>/dev/null || rm -f "$BACKUP/$f"
done
echo "Backed up current data to $BACKUP"

cp "$BACKUP"/* "$WORK"/
VERSION="$(sqlite3 "$WORK/$DB" 'PRAGMA user_version;')"
if [ "$VERSION" != "$EXPECTED_VERSION" ]; then
  echo "Database is at version $VERSION but seed.sql targets $EXPECTED_VERSION. Update the seed first." >&2
  exit 1
fi

# Fold the WAL into the main file so a single file can be pushed back.
sqlite3 "$WORK/$DB" "PRAGMA wal_checkpoint(TRUNCATE); PRAGMA journal_mode=DELETE;" >/dev/null
sqlite3 "$WORK/$DB" < "$ROOT/scripts/seed.sql"

"$ADB" exec-in run-as "$PKG" sh -c "cat > databases/$DB" < "$WORK/$DB"
"$ADB" shell run-as "$PKG" rm -f "databases/$DB-wal" "databases/$DB-shm"

echo "Seeded: $(sqlite3 "$WORK/$DB" 'SELECT count(*) FROM savings_goals;') goals, $(sqlite3 "$WORK/$DB" 'SELECT count(*) FROM habits;') habits"
echo "Restore with: for f in $BACKUP/*; do $ADB exec-in run-as $PKG sh -c \"cat > databases/\$(basename \$f)\" < \$f; done"
