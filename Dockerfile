# Stage build UI
FROM node:22-alpine3.19 AS builder-ui

WORKDIR /app
COPY ./tools-ui/. .

ENV NODE_OPTIONS=--openssl-legacy-provider

RUN npm install
RUN npm run buildProd

# Stage build service
FROM openjdk:17-alpine AS builder-service

WORKDIR /app
COPY . .
COPY --from=builder-ui /app/build/ui tools-server/src/main/resources/static/.

RUN chmod +x gradlew
RUN ./gradlew bootJar

# Stage run
FROM openjdk:17-alpine

WORKDIR /app
COPY --from=builder-service /app/tools-server/build/libs .

ENV PRODUCTION=true

CMD ["java", "-jar", "tools.jar"]