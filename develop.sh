#!/bin/bash

# This script is used to compile the project for production.

# Change directory to resources/frontend
# This is where the frontend code is located
cd resources/frontend

# Install the dependencies listed in package.json
# This is necessary for the frontend code to run
npm install

cd ../backend 

npm install

# Compile the frontend code using shadow-cljs
# The "watch" command automatically recompiles the code when it changes
 
shadow-cljs watch backend frontend > shadow-cljs.log 2>&1 &



# Get the process ID of the shadow-cljs process
SHADOW_PID=$!

# Monitor the log file for the specific line
tail -f shadow-cljs.log | while IFS= read -r line
do
    echo "$line"
    if [[ "$line" == *"[:backend] Build completed."* ]]; then
        echo "Starting Node.js process..."
        cd ../frontend
        webpack --config webpack.config.js --mode development
        cd ../backend
        node core.js
        
        # Optionally, exit the script after starting the node process
        kill $SHADOW_PID
        exit 0
    fi
done

