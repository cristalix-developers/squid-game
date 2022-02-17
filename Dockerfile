# Этап сборки
FROM openjdk:8-slim AS build
COPY . /home/squid-game
WORKDIR /home/squid-game

# Переменные среды
ARG IMPLARIO_REPO_USER
ARG IMPLARIO_REPO_PASSWORD

ENV IMPLARIO_REPO_USER=$IMPLARIO_REPO_USER
ENV IMPLARIO_REPO_PASSWORD=$IMPLARIO_REPO_PASSWORD

# Сборка
RUN export GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork" && chmod +x gradlew && ./gradlew :node:build --no-daemon --stacktrace

# Этап запуска
FROM registry.implario.net/cristalix-arcades/dark-paper:stable-itemmeta
WORKDIR /app
COPY --from=build /home/squid-game/node/build/libs/squid-game.jar ./plugins/squid-game.jar
