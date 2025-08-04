FROM node:22.17.1 AS builder

WORKDIR /app

COPY package*.json ./

RUN npm install && npm install -g serve

COPY . .

RUN npm run build

EXPOSE 3000
CMD [ "serve", "-s", "dist" ]
