# Этап сборки
FROM openjdk:8-slim AS build
COPY . /home/squid-game
WORKDIR /home/squid-game
RUN export GRADLE_OPTS="-Djdk.lang.Process.launchMechanism=vfork" && chmod +x gradlew && ./gradlew :node:build --no-daemon --stacktrace

# Переменные среды
ARG IMPLARIO_REPO_USER
ARG IMPLARIO_REPO_PASSWORD

ENV IMPLARIO_REPO_USER=$IMPLARIO_REPO_USER
ENV IMPLARIO_REPO_PASSWORD=$IMPLARIO_REPO_PASSWORD

# Этап запуска
FROM openjdk:8-slim
WORKDIR /app
RUN mkdir -p /app/data
VOLUME /app/data
COPY --from=build /home/squid-game/node/build/libs/squid-game.jar ./squid-game.jar
ENTRYPOINT exec java $JAVA_OPTS -jar squid-game.jar
