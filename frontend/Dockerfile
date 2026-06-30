# Stage 1: build the Angular app
FROM node:24-alpine AS build

WORKDIR /app

# Install dependencies first (better layer caching).
# .npmrc pins the public npm registry so the build needs no private credentials.
COPY package.json package-lock.json .npmrc ./
RUN npm ci

# Build
COPY . .
RUN npm run build

# Stage 2: serve with nginx
FROM nginx:latest

COPY --from=build /app/dist/fourleft-frontend/browser /usr/share/nginx/html
COPY nginx-frontend.conf /etc/nginx/conf.d/default.conf
