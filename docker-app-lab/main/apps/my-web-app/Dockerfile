# Use lightweight Node image
FROM node:18-alpine

WORKDIR /app

# Only copy dependency manifests
COPY package*.json ./

# Install deps (include nodemon for dev via package.json)
RUN npm install

# Copy remaining files
COPY . .

EXPOSE 3000

# Default entry (can be overridden)
CMD ["node", "src/app.js"]