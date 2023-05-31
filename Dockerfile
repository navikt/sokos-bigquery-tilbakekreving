FROM ghcr.io/navikt/baseimages/temurin:17
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"