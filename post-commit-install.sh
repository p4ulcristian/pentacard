#!/bin/bash


echo "Copying post-commit hook script..."
# Copy the pre-commit hook script to the .git/hooks directory
cp post-commit .git/hooks/post-commit

# Make sure the hook script is executable
chmod +x .git/hooks/post-commit

echo "post-commit hook installed successfully!"