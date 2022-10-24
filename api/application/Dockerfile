FROM alpine:latest
RUN apk add openjdk17
RUN apk add firefox
RUN apk add ca-certificates
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar","--enable-preview", "/app.jar"]
EXPOSE 8081