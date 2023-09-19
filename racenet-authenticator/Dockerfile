FROM alpine:latest
RUN apk add openjdk17

#import cert into java
ARG CERT="dirtgame-com.pem"
COPY $CERT $CERT
RUN keytool -importcert -file $CERT -alias $CERT -cacerts -storepass changeit -noprompt


RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8082