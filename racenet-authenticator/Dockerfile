# syntax=docker/dockerfile:1
# Build context is the repo root (see docker-compose.yml) so the Maven reactor
# can build this module via the wrapper without a host-side JDK/Maven.

# ---- build stage: compile and produce the fat jar ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -DskipTests -pl racenet-authenticator -am clean package

# ---- runtime stage ----
# Ubuntu 22.04 (jammy) + JDK 25. Playwright 1.44 supports jammy; the previous
# debian:latest moved to trixie, which Playwright doesn't support (its --with-deps
# step fails on the renamed/dropped font packages).
FROM eclipse-temurin:25-jdk-jammy
RUN apt-get update

RUN addgroup spring && adduser --ingroup spring spring

COPY --from=build /workspace/racenet-authenticator/target/*.jar /racenet_authenticator.jar

# Install Playwright's bundled Chromium and its OS dependencies into a shared,
# world-readable location so the non-root "spring" user can launch it at runtime.
# The CLI lives inside the Spring Boot fat jar, run via the loader's PropertiesLauncher.
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN java -cp /racenet_authenticator.jar \
    -Dloader.main=com.microsoft.playwright.CLI \
    org.springframework.boot.loader.launch.PropertiesLauncher install --with-deps chromium

# Note: the app itself sets PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD via Playwright.CreateOptions,
# so runtime never auto-downloads Firefox/WebKit; only the Chromium installed above is used.

USER spring:spring
ENTRYPOINT ["java", "-jar", "/racenet_authenticator.jar"]
EXPOSE 8082
