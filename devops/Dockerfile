# ── Stage 1: Build Angular SPA ───────────────────────────────────────────────
FROM node:22-alpine AS webapp-build
WORKDIR /workspace/solarcalc-webapp
COPY solarcalc-webapp/package*.json ./
RUN npm ci
COPY solarcalc-webapp/ ./
RUN npm run build

# ── Stage 2: Resolve Gradle dependencies (cached layer) ──────────────────────
FROM eclipse-temurin:25-jdk AS deps
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY buildSrc ./buildSrc
COPY solarcalc-domain-models/build.gradle.kts    ./solarcalc-domain-models/
COPY solarcalc-domain-services/build.gradle.kts  ./solarcalc-domain-services/
COPY solarcalc-rest-api/build.gradle.kts          ./solarcalc-rest-api/
COPY solarcalc-kotlin-extensions/build.gradle.kts ./solarcalc-kotlin-extensions/
COPY solarcalc-logging/build.gradle.kts           ./solarcalc-logging/
COPY solarcalc-server/build.gradle.kts            ./solarcalc-server/
RUN ./gradlew dependencies --no-daemon -q 2>/dev/null || true

# ── Stage 3: Build bootJar ────────────────────────────────────────────────────
FROM deps AS backend-build
COPY solarcalc-domain-models    ./solarcalc-domain-models
COPY solarcalc-domain-services  ./solarcalc-domain-services
COPY solarcalc-rest-api          ./solarcalc-rest-api
COPY solarcalc-kotlin-extensions ./solarcalc-kotlin-extensions
COPY solarcalc-logging           ./solarcalc-logging
COPY solarcalc-server            ./solarcalc-server
COPY --from=webapp-build /workspace/solarcalc-webapp/dist/solarcalc-webapp/browser \
     ./solarcalc-server/src/main/resources/static
RUN ./gradlew :solarcalc-server:bootJar --no-daemon -q

# ── Stage 4: Minimal runtime image ───────────────────────────────────────────
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=backend-build /workspace/solarcalc-server/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
