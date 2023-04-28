FROM alpine:3.15.4
RUN apk add openjdk17
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar","--enable-preview", "/app.jar"]
EXPOSE 8082