# syntax=docker/dockerfile:1
# Build context is the repo root (see docker-compose.yml) so the Maven reactor
# can resolve the sibling modules (common, api) this service depends on.

# ---- build stage: compile the reactor and produce the fat jar ----
# Maven 3.9.16 (matches .mvn/wrapper) preinstalled on JDK 25 — using the image's
# mvn avoids the wrapper downloading Maven at build time.
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY . .
# typescript-generator runs while the api module is built and writes into the
# frontend tree, which is excluded from the build context; give it a target dir.
RUN mkdir -p frontend/src/app/common/generated
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests -pl backend-ea-sports-wrc -am clean package

# ---- runtime stage ----
FROM eclipse-temurin:25-jre-alpine

COPY backend-ea-sports-wrc/docker/certs/digicert-g2-intermediate.pem /usr/local/share/ca-certificates/digicert-g2-intermediate.crt
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt -alias digicert-g2-intermediate \
    -file /usr/local/share/ca-certificates/digicert-g2-intermediate.crt

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/backend-ea-sports-wrc/target/*.jar backend_easportswrc.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/backend_easportswrc.jar"]
