# syntax=docker/dockerfile:1
# Build context is the repo root (see docker-compose.yml) so the Maven reactor
# can resolve the sibling modules (common, api) this service depends on.

# ---- build stage: compile the reactor and produce the fat jar ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
# typescript-generator runs while the api module is built and writes into the
# frontend tree, which is excluded from the build context; give it a target dir.
RUN mkdir -p frontend/src/app/common/generated
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -DskipTests -pl discord -am clean package

# ---- runtime stage ----
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /workspace/discord/target/*.jar discord.jar
ENTRYPOINT ["java", "-jar", "/discord.jar"]
EXPOSE 8082
