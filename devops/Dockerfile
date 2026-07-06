FROM node:22-alpine AS webapp-build
WORKDIR /workspace/solarcalc-webapp
COPY solarcalc-webapp/package*.json ./
RUN npm ci
COPY solarcalc-webapp/ ./
RUN npm run build

FROM eclipse-temurin:25-jdk AS backend-build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY buildSrc ./buildSrc
COPY solarcalc-domain-models ./solarcalc-domain-models
COPY solarcalc-domain-services ./solarcalc-domain-services
COPY solarcalc-rest-api ./solarcalc-rest-api
COPY solarcalc-kotlin-extensions ./solarcalc-kotlin-extensions
COPY solarcalc-logging ./solarcalc-logging
COPY solarcalc-server ./solarcalc-server
COPY --from=webapp-build /workspace/solarcalc-webapp/dist/solarcalc-webapp/browser ./solarcalc-server/src/main/resources/static
RUN ./gradlew :solarcalc-server:bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=backend-build /workspace/solarcalc-server/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
