FROM debian:latest
RUN apt-get update
RUN apt-get install -y openjdk-21-jdk

RUN addgroup spring && adduser --ingroup spring spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /racenet_authenticator.jar

# Install Playwright's bundled Chromium and its OS dependencies into a shared,
# world-readable location so the non-root "spring" user can launch it at runtime.
# The CLI lives inside the Spring Boot fat jar, run via the loader's PropertiesLauncher.
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN java -cp /racenet_authenticator.jar \
    -Dloader.main=com.microsoft.playwright.CLI \
    org.springframework.boot.loader.PropertiesLauncher install --with-deps chromium

# Note: the app itself sets PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD via Playwright.CreateOptions,
# so runtime never auto-downloads Firefox/WebKit; only the Chromium installed above is used.

USER spring:spring
ENTRYPOINT ["java", "-jar", "/racenet_authenticator.jar"]
EXPOSE 8082
