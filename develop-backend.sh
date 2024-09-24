
shadow-cljs watch backend > shadow-cljs.log 2>&1 &

# Get the process ID of the shadow-cljs process
SHADOW_PID=$!

# Monitor the log file for the specific line
tail -f shadow-cljs.log | while IFS= read -r line
do
    echo "$line"
    if [[ "$line" == *"[:backend] Build completed."* ]]; then
        cd resources/backend
        echo "Starting Node.js process..."
        node core.js    
        # Optionally, exit the script after starting the node process
        kill $SHADOW_PID
        exit 0
    fi
done

