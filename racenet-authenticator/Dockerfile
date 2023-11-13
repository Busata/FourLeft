FROM debian:latest
RUN apt-get update
RUN apt-get install -y openjdk-17-jdk

#import cert into java
ARG CERT="dirtgame-com.pem"
COPY $CERT $CERT
RUN keytool -importcert -file $CERT -alias $CERT -cacerts -storepass changeit -noprompt

RUN apt-get install -y wget
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
RUN apt-get install -y ./google-chrome-stable_current_amd64.deb

RUN addgroup spring && adduser --ingroup spring spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} racenet_authenticator.jar
ENTRYPOINT ["java", "-jar", "/racenet_authenticator.jar"]
EXPOSE 8082
