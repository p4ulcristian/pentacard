
cd resources/frontend

npm install -g npm-check-updates

npx npm-check-updates -u

# Remove node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall all dependencies
npm install

cd ../backend 

npx npm-check-updates -u

# Remove node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall all dependencies
npm install