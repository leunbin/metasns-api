# ================
#  Build Stage
# ================
FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /app

# 캐시 최적화
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon

#소스 복사 후 빌드
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon

# ================
#  Runtime stage
# ================
FROM eclipse-temurin:17-jre
WORKDIR /app

#빌드 결과물만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]