# syntax=docker/dockerfile:1
# Build context is the repo root (see docker-compose.yml) so the Maven reactor
# can resolve the sibling modules (common, api) this service depends on.

# ---- build stage: compile the reactor and produce the fat jar ----
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY . .
# typescript-generator runs while the api module is built and writes into the
# frontend tree, which is excluded from the build context; give it a target dir.
RUN mkdir -p frontend/src/app/common/generated
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests -pl backend-acrally -am clean package

# ---- runtime stage ----
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/backend-acrally/target/*.jar backend_acrally.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/backend_acrally.jar"]
