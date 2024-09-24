#!/bin/bash

# This script is used to compile the project for production.

# Change directory to resources/frontend
# This is where the frontend code is located
cd resources/frontend

# Install the dependencies listed in package.json
# This is necessary for the frontend code to run
npm install

# Compile the frontend code using shadow-cljs
# The "release" command compiles the code for production
shadow-cljs release frontend-ready

# Run webpack to bundle the frontend code for production
# The "--config" option specifies the configuration file
# The "--mode production" option enables optimizations for production
webpack --config webpack.config.js --mode production 

# Change directory to resources/backend
# This is where the backend code is located
cd ../backend

# Install the dependencies listed in package.json
# This is necessary for the backend code to run
npm install

# Compile the backend code using shadow-cljs
# The "release" command compiles the code for production
shadow-cljs release backend-ready