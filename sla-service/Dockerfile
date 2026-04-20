# Первый этап: сборка
FROM eclipse-temurin:23-jdk AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Второй этап: запуск
FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
