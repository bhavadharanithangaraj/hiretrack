# ---- Stage 1: build the jar with Maven ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies. Docker caches this layer,
# so dependencies are only re-downloaded when pom.xml changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Tests run in CI (GitHub Actions), so we skip them here to keep image builds fast
RUN mvn -B package -DskipTests

# ---- Stage 2: run the jar on a small JRE-only image ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Don't run as root inside the container
RUN useradd --system --create-home spring
USER spring

COPY --from=build /app/target/hiretrack-backend-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
