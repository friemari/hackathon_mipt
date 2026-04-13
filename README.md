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

### Проверка

- `GET /api/health` — состояние приложения
```bash
http://localhost:8080/api/health
```

### SLA B2c Summary

- `GET /api/sla/b2c/summary`
```bash
http://localhost:8080/api/sla/b2c/summary
```
### Описание

Возвращает агрегированные метрики SLA по воронке **B2C** за указанный период.

Используется для получения общей статистики по заказам:
- выполнение SLA
- нарушения SLA
- распределение просрочек
- ключевые перцентильные метрики

### Параметры запроса

| Параметр         | Тип    | Обязательный | Описание |
|------------------|--------|--------------|----------|
| `from`           | date   | да           | Дата начала периода (включительно) |
| `to`             | date   | да           | Дата окончания периода (включительно) |
| `manager_id`     | string | нет          | Фильтр по B2C-менеджеру |
| `qualification`  | string | нет          | Фильтр по квалификации (по умолчанию: A, B, C) |

#### Пример запроса

```
GET /api/sla/b2c/summary?from=2024-01-01&to=2024-01-31
```

### Логика расчета

- В выборку попадают заказы с `sale_date` в диапазоне `[from, to]`
- По умолчанию учитываются только квалификации: `A`, `B`, `C`
- Метрики считаются на основе временных интервалов между этапами:
    - SLA1: `sale_ts - следующий этап`
    - SLA2: `sale_ts - to_assembly_ts`
- Исключаются:
    - записи с `NULL`
    - некорректные интервалы (отрицательные значения)

### Ответ

```json
{
  "period": {
    "from": "2024-01-01",
    "to": "2024-01-31"
  },
  "metrics": {
    "sla1_reaction": {
      "threshold_minutes": 30,
      "total_orders": 1200,
      "met_count": 950,
      "met_percent": 79.17,
      "breach_count": 250,
      "breach_percent": 20.83,
      "avg_minutes": 18.5,
      "median_minutes": 15,
      "p90_minutes": 35,
      "breach_distribution": {
        "up_to_15min": 100,
        "15_to_60min": 120,
        "over_60min": 30
      }
    }
  }
}
```
### Поля ответа

#### period

| Поле | Тип  | Описание |
|------|------|----------|
| from | date | Начало периода |
| to   | date | Конец периода |

#### metrics

Объект с метриками SLA (например: `sla1_reaction`, `sla2_to_assembly`)

#### Внутри метрики

| Поле | Тип   | Описание |
|------|-------|----------|
| threshold_minutes | int   | Порог SLA в минутах |
| total_orders      | int   | Общее количество заказов |
| met_count         | int   | Количество заказов, уложившихся в SLA |
| met_percent       | float | Процент выполнения SLA |
| breach_count      | int   | Количество нарушений SLA |
| breach_percent    | float | Процент нарушений |
| avg_minutes       | float | Среднее время |
| median_minutes    | float | Медиана |
| p90_minutes       | float | 90-й перцентиль |
| breach_distribution | object | Распределение нарушений |

#### breach_distribution

| Поле           | Описание |
|----------------|----------|
| up_to_15min    | Просрочка до 15 минут |
| 15_to_60min    | Просрочка от 15 до 60 минут |
| over_60min     | Просрочка более 60 минут |

--- 

### Загрузка данных
- `POST /api/data/load`
```bash
http://localhost:8080/api/data/load
```

### Описание

Загружает CSV датасет в систему и сохраняет данные в базу.

Используется для первоначальной загрузки или обновления данных.

### Формат запроса

- Content-Type: multipart/form-data
- Поле:
  - file — CSV файл (string, binary)


### Пример запроса (curl)

```
curl -X POST http://localhost:8080/api/data/load \
  -H "Content-Type: multipart/form-data" \
  -F "file=@dataset.csv"
```

### Ответ (200 OK)

```json
{
  "fileName": "string",
  "totalRows": 0,
  "insertedRows": 0,
  "updatedRows": 0,
  "skippedRows": 0,
  "errorRows": 0,
  "batchId": 0,
  "status": "string"
}
```

### Поля ответа

| Поле          | Тип    | Описание |
|---------------|--------|----------|
| fileName      | string | Имя загруженного файла |
| totalRows     | int    | Общее количество строк в файле |
| insertedRows  | int    | Количество вставленных записей |
| updatedRows   | int    | Количество обновленных записей |
| skippedRows   | int    | Пропущенные строки |
| errorRows     | int    | Строки с ошибками |
| batchId       | int    | Идентификатор загрузки |
| status        | string | Статус обработки |

---

### Примечания

- Принимается только CSV файл
- Данные валидируются при загрузке
- Ошибки не прерывают загрузку, а учитываются в errorRows



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
