FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew build --no-daemon -x test

RUN echo ">> JAR 파일 리스트:" && find build/libs -name "*.jar" -exec ls -lh {} \;

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/github-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]