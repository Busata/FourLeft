FROM alpine:latest
RUN apk add openjdk17
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} backend_dirtrally2.jar
ENTRYPOINT ["java", "-jar", "/backend_dirtrally2.jar"]
EXPOSE 8081