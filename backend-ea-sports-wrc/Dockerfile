FROM alpine:latest

RUN apk add --no-cache \
    openjdk17-jre \
    ca-certificates \
    java-cacerts \
    openssl \
 && update-ca-certificates

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} backend_easportswrc.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/backend_easportswrc.jar"]
