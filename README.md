# SLA Service (Spring Boot)

Сервис для работы с SLA, разработанный в рамках хакатона МФТИ.

---

## О проекте

Сервис загружает выгрузку из CRM, рассчитывает SLA-метрики для двух воронок (B2C и доставка) и сквозного цикла сделки. Дополнительно включает ML-модуль для предсказания вероятности отказа клиента от выкупа.

---

### Основные функции

| Функция | Описание |
|---------|----------|
| **Загрузка данных** | CSV-импорт с UPSERT и кросс-проверкой полей |
| **B2C воронка** | SLA-1 (30 мин), SLA-2 (4 ч), SLA-3 (1 день), B2C-total (2 дня) |
| **Воронка доставки** | SLA-4 (5 дн), SLA-5 (7 дн), DEL-total (14 дн) |
| **Сквозной SLA** | FULL — полный цикл сделки (16 дней) |
| **ML предсказания** | Двухэтапное предсказание выкупа, группы риска, рекомендации |
| **Агрегаты** | % выполнения, среднее, медиана, 90-й перцентиль, распределение нарушений |
| **Фильтрация** | По дате, менеджеру, квалификации, службе доставки |

---

## Технологии

| Компонент | Технологии |
|-----------|------------|
| **Язык** | Java 23, Python 3.11 |
| **Фреймворки** | Spring Boot 3.5, FastAPI |
| **База данных** | PostgreSQL 15 |
| **ML** | CatBoost, Pandas, NumPy, scikit-learn |
| **Сборка** | Maven |
| **Контейнеризация** | Docker / Docker Compose |
| **Документация** | Swagger / OpenAPI |

---

## Требования

Перед запуском убедитесь, что у вас установлено:

| Компонент  | Версия            | Проверка           |
| ---------- | ----------------- | ------------------ |
| Java       | 23+               | `java -version`    |
| PostgreSQL | 14+               | `psql --version`   |
| Docker     | 20+ (опционально) | `docker --version` |
| Maven      | 3.8+              | `mvn --version`    |

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

### 3) через Docker

#### Этап 1. Сборка приложения

```bash
./mvnw clean package
```

Windows:

```bash
mvnw.cmd clean package
```

#### Этап 2. Запуск через Docker

```bash
docker compose up --build
```

---

## API Эндпоинты

### Проверка здоровья

- `GET /api/health` — состояние приложения

```bash
curl http://localhost:8080/api/health
```

### SLA B2C Summary

- `GET /api/sla/b2c/summary` - Агрегаты B2C с фильтрацией
- `GET /api/sla/b2c/by-manager` - Разбивка по менеджерам B2C

```bash
# Агрегаты за период
curl "http://localhost:8080/api/sla/b2c/summary?date_from=2025-03-01&date_to=2025-03-31"

# По конкретному менеджеру
curl "http://localhost:8080/api/sla/b2c/summary?date_from=2025-03-01&date_to=2025-03-31&manager_id=MGR_0001"

# По квалификации
curl "http://localhost:8080/api/sla/b2c/summary?date_from=2025-03-01&date_to=2025-03-31&qualification=A"
```

### Воронка доставки

- `GET /api/sla/delivery/summary` - Агрегаты доставки
- `GET /api/sla/delivery/by-manager` - Разбивка по менеджерам доставки

```bash
curl "http://localhost:8080/api/sla/delivery/summary?date_from=2025-03-01&date_to=2025-03-31"
```

### Сквозной SLA

- `GET /api/sla/full/summary` - Полный цикл сделки

```bash
curl "http://localhost:8080/api/sla/full/summary?date_from=2025-03-01&date_to=2025-03-31"
```

### Конфигурация

- `GET /api/sla/config` - Tекущие нормативы SLA

```bash
curl http://localhost:8080/api/sla/config
```

### Загрузка данных 

- `POST /api/data/load` - Загрузка CSV датасета

```bash
curl -X POST http://localhost:8080/api/data/load \
  -H "Content-Type: multipart/form-data" \
  -F "file=@dataset.csv"
```

### Timeline сделки

- `GET /api/orders/{leadId}/timeline` - Временная линия сделки

```bash
curl "http://localhost:8080/api/orders/LEAD_0172/timeline"
```

---

## ML модуль

### Функции

| Функция | Описание |
|---------|----------|
| **Stage 1** | Предсказание на момент заказа (без данных о сборке) |
| **Stage 2** | Предсказание после сборки (с учётом скорости сборки) |
| **Группы риска** | 🔴 Красный (>70%), 🟡 Жёлтый (30-70%), 🟢 Зелёный (<30%) |
| **Рекомендации** | Автоматические действия для менеджеров |
| **Экономический эффект** | Расчёт потерь от отказов |

### API ML сервера

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| `GET` | `/health` | Проверка статуса |
| `POST` | `/model/train` | Обучение модели на CSV |
| `GET` | `/model/status` | Статус модели |
| `POST` | `/model/predict/stage1` | Предсказание на момент заказа |
| `POST` | `/model/predict/stage2` | Предсказание после сборки |
| `GET` | `/model/importance` | Важность признаков |

```bash
# Проверка статуса ML сервера
curl http://localhost:8000/health

# Статус модели
curl http://localhost:8000/model/status

# Предсказание Stage 1
curl -X POST http://localhost:8000/model/predict/stage1 \
  -H "Content-Type: application/json" \
  -d '{"lead_price": 5000, "contact_Город": "Москва"}'
```

### Метрики модели

| Показатель | Stage 1 | Stage 2 |
|------------|---------|---------|
| **ROC-AUC** | 0.714 | 0.734 |
| **Accuracy** | 84.8% | 85.7% |

---

## Структура проекта

```
sla-service/
├── src/main/java/com/hackathon/sla_service/
│   ├── controller/              # REST API
│   │   ├── SlaController.java
│   │   ├── DeliveryController.java
│   │   ├── FullSlaController.java
│   │   ├── OrderTimelineController.java
│   │   ├── SlaConfigController.java
│   │   ├── HealthController.java
│   │   └── DataLoadController.java
│   ├── service/                 # Бизнес-логика
│   │   ├── SlaSummaryService.java
│   │   ├── DeliverySummaryService.java
│   │   ├── SlaFullSummaryService.java
│   │   ├── OrderTimelineService.java
│   │   ├── SlaConfigService.java
│   │   ├── DataLoadService.java
│   │   ├── impl/
│   │   └── calculator/
│   ├── repository/              # Работа с БД
│   │   ├── SlaRepository.java
│   │   ├── LeadRepository.java
│   │   ├── ImportBatchRepository.java
│   │   ├── ImportAnomalyRepository.java
│   │   └── model/
│   ├── dto/                     # DTO объекты
│   │   ├── common/
│   │   └── response/
│   ├── importer/                # CSV импортер
│   │   ├── CsvImportService.java
│   │   ├── CsvLeadRowMapper.java
│   │   └── model/
│   ├── config/                  # Конфигурация
│   │   └── SlaConfigProperties.java
│   └── exception/               # Обработка ошибок
│       └── GlobalExceptionHandler.java
├── ModuleML/                    # ML модуль
│   ├── lead_scoring_model.py
│   ├── order_processor.py
│   ├── Dockerfile.ml
│   └── requirements.txt
├── src/main/resources/
│   └── application.yml
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Swagger UI

Документация API доступна после запуска приложения:

```
http://localhost:8080/docs
```

- Просмотр всех эндпоинтов
- Тестирование запросов прямо в браузере
- Схемы запросов и ответов

---

## Возможные проблемы

### Java version mismatch

```bash
java -version
```

**Решение:** Используйте Java 23.

### Port 8080 already in use

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

---

## Логика расчета SLA

### Нормативы (B2C воронка)

| Этап | Показатель | Формула | Норматив |
|------|------------|---------|----------|
| 1 | Реакция менеджера | `sale_ts - created_at` | 30 минут |
| 2 | До сборки | `to_assembly_ts - sale_ts` | 4 часа |
| 3 | Доставка | `handed_to_delivery_ts - to_assembly_ts` | 1 день |
| 4 | B2C Total | `handed_to_delivery_ts - created_at` | 2 дня |

### Нормативы (доставка)

| Этап | Показатель | Формула | Норматив |
|------|------------|---------|----------|
| 4 | Время до ПВЗ | `issued_or_pvz_ts - handed_to_delivery_ts` | 5 дней |
| 5 | Хранение на ПВЗ | `COALESCE(received_ts, rejected_ts, returned_ts) - issued_or_pvz_ts` | 7 дней |
| DEL-total | Полный цикл доставки | `исход_ts - handed_to_delivery_ts` | 14 дней |

### Сквозная метрика

| Этап | Показатель | Формула | Норматив |
|------|------------|---------|----------|
| FULL | Полный цикл сделки | `closed_ts - lead_created_at` | 16 дней |

