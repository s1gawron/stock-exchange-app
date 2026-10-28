# API URL is baked into the bundle at build time, override with --build-arg
ARG VITE_API_SERVICE_SCHEME=http
ARG VITE_API_SERVICE_HOST=localhost
ARG VITE_API_SERVICE_PORT=8080

FROM node:22.17.1 AS builder

ARG VITE_API_SERVICE_SCHEME
ARG VITE_API_SERVICE_HOST
ARG VITE_API_SERVICE_PORT

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci

COPY . .

RUN npm run build

FROM nginx:stable-alpine

ARG VITE_API_SERVICE_SCHEME
ARG VITE_API_SERVICE_HOST
ARG VITE_API_SERVICE_PORT

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx-security-headers.conf /etc/nginx/security-headers.conf

# Content-Security-Policy allows API calls only to the configured API origin
RUN API_ORIGIN="${VITE_API_SERVICE_SCHEME}://${VITE_API_SERVICE_HOST}${VITE_API_SERVICE_PORT:+:$VITE_API_SERVICE_PORT}" \
    && sed -i "s|__API_ORIGIN__|${API_ORIGIN}|" /etc/nginx/security-headers.conf \
    && chown -R nginx:nginx /var/cache/nginx /var/log/nginx

EXPOSE 80

USER nginx
