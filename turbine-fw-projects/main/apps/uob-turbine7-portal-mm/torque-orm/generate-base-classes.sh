#!/bin/bash
# Script to generate Torque base classes
# This script temporarily moves modifiable classes, generates base classes, then moves them back

echo "Generating Torque base classes..."

# Create backup directory
BACKUP_DIR="src/main/java-backup"
GENERATED_DIR="target/generated-sources/com/uob/om"

# Step 1: Backup modifiable classes
echo "Step 1: Backing up modifiable classes..."
mkdir -p "$BACKUP_DIR"
cp -r src/main/java/com/uob/om/*.java "$BACKUP_DIR/" 2>/dev/null || true

# Step 2: Remove modifiable classes temporarily
echo "Step 2: Temporarily removing modifiable classes..."
find src/main/java/com/uob/om -name "Gtp*.java" -type f -delete
find src/main/java/com/uob/om -name "Turbine*.java" -type f -delete

# Step 3: Generate base classes
echo "Step 3: Generating base classes..."
mvn clean generate-sources -DskipTests

# Step 4: Restore modifiable classes
echo "Step 4: Restoring modifiable classes..."
cp "$BACKUP_DIR"/*.java src/main/java/com/uob/om/ 2>/dev/null || true
rm -rf "$BACKUP_DIR"

# Step 5: Verify base classes were generated
if [ -f "$GENERATED_DIR/BaseGtpUser.java" ]; then
    echo "✓ Base classes generated successfully!"
    echo "Location: $GENERATED_DIR"
else
    echo "✗ Base classes not found. Check Torque generation output."
fi
