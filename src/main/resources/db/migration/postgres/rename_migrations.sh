#!/bin/bash

# Change to your migration directory
cd /Users/vun/Projects/cynergi-middleware/src/main/resources/db/migration/postgres

# Set the starting version number
start_version="56"

# Find all migration files in the directory
files=$(ls -1 | grep -E '^V[0-9]+__')

# Iterate over each file and rename it
for file in $files; do
  # Extract the version number from the filename
  current_version=$(echo "$file" | sed -n -E 's/V([0-9]+)__.*/\1/p')

  # Check if the version number is greater than or equal to the starting version
  if [[ "$current_version" -ge "$start_version" ]]; then
    # Generate the new filename with the incremented version number
    new_version=$((current_version + 5))
    new_filename=$(echo "$file" | sed -E "s/V$current_version/V$new_version/")

    echo "Renaming $file to $new_filename"
    # Rename the file
    mv "$file" "$new_filename"
  fi
done