FROM alpine:latest
RUN apk add openjdk17
RUN apk add firefox
RUN apk add ca-certificates
RUN apk add --no-cache msttcorefonts-installer fontconfig
RUN update-ms-fonts
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} backend_dirtrally2.jar
ENTRYPOINT ["java", "-jar", "/backend_dirtrally2.jar"]
EXPOSE 8081