# Stage 1: Build the React app
FROM node:20-alpine AS build

WORKDIR /app

# Install dependencies first (cached layer unless package files change)
COPY package.json package-lock.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# Stage 2: Serve the built files with nginx
FROM nginx:alpine

# Replace the default nginx config with ours
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the compiled React app from the build stage
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
