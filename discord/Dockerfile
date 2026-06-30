FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} discord.jar
ENTRYPOINT ["java", "-jar", "/discord.jar"]
EXPOSE 8082