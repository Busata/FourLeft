FROM alpine:latest
RUN apk add openjdk17
RUN apk add --no-cache ca-certificates && update-ca-certificates
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} backend_easportswrc.jar
ENTRYPOINT ["java", "-jar", "/backend_easportswrc.jar"]
EXPOSE 8084