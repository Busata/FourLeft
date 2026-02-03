FROM eclipse-temurin:17-jre-alpine

COPY docker/certs/digicert-g2-intermediate.pem /usr/local/share/ca-certificates/digicert-g2-intermediate.crt
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt -alias digicert-g2-intermediate \
    -file /usr/local/share/ca-certificates/digicert-g2-intermediate.crt

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} backend_easportswrc.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/backend_easportswrc.jar"]
