# SLA Service (Spring Boot)

Сервис для работы с SLA, разработанный в рамках хакатона МФТИ.

---

## Требования

Перед запуском убедитесь, что у вас установлено:

- Java 23
```bash
java -version
```
---

## Клонирование репозитория

```bash
git clone https://github.com/friemari/hackathon_mipt.git
cd hackathon_mipt/sla-service
```

---

## Запуск приложения
### 1) через Maven Wrapper

```bash
./mvnw spring-boot:run
```

Для Windows:
```bash
mvnw.cmd spring-boot:run
```

### 2) через IntelliJ IDEA

1. Открыть папку `sla-service` в IntelliJ IDEA
2. Дождаться загрузки Maven-зависимостей
3. Запустить класс SlaServiceApplication
---

## API Эндпоинты

### Проверка

- `GET /app/health` — состояние приложения
```bash
http://localhost:8080/app/health
```
---

## Swagger
```
http://localhost:8080/docs
```

---

## Возможные проблемы

### Java version mismatch

```bash
java -version
```

Используйте Java 23.

---

### Port 8080 already in use

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

---

## Структура проекта

- `controller` — REST API
- `service` — бизнес-логика
- `repository` — работа с БД
- `model` — сущности
