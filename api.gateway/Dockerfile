FROM eclipse-temurin:24-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:24-jre-alpine

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/target/api-gateway-*.jar app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
