# Order Processor ML API

REST API сервис для прогнозирования отказа заказов и приоритизации по риску.

## Назначение

Сервис позволяет:

* обучать модель на исторических данных
* получать предсказания по одному заказу
* обрабатывать файлы с заказами (batch)
* получать статус и метрики модели

## Установка

pip install pandas numpy scikit-learn catboost fastapi uvicorn python-multipart openpyxl

## Запуск

Если файл называется order_processor.py:

uvicorn order_processor:app --host 0.0.0.0 --port 8000

После запуска API доступен по адресу:
http://localhost:8000

Swagger UI:
http://localhost:8000/docs

## Основные endpoints

### Проверка сервиса

GET /

Возвращает статус сервиса, модели и данных.

### Статус модели

GET /model/status

Показывает:

* обучена ли модель
* идёт ли обучение
* метрики модели
* количество признаков

### Обучение модели

POST /model/train

Формат: multipart/form-data
Поле: file (CSV)

Пример:

curl -X POST "http://localhost:8000/model/train" -F "file=@train.csv"

Обучение выполняется в фоне.

## Предсказания

### Stage 1 (при создании заказа)

POST /model/predict/stage1

Пример JSON:

{
"lead_price": 12000,
"sale_ts": 1713000000,
"contact_created_at": 1712910000,
"contact_Город": "Москва",
"lead_Вид оплаты": "Наложенный платеж",
"lead_Служба доставки": "СДЭК",
"lead_Состав заказа": "Товар 1",
"contact_Email": "[test@test.ru](mailto:test@test.ru)"
}

### Stage 2 (после сборки заказа)

POST /model/predict/stage2

Дополнительно используется поле:
"handed_to_delivery_ts": 1713172800

## Ответ модели

{
"status": "success",
"prediction": {
"prediction": 0,
"probability": 0.23,
"risk_group": "red",
"recommendation": "Обязательный обзвон"
}
}

## Batch-предсказание

POST /model/predict/batch?stage=stage1

или

POST /model/predict/batch?stage=stage2

Формат: multipart/form-data
Файл: CSV или Excel

Пример:

curl -X POST "http://localhost:8000/model/predict/batch?stage=stage1" -F "file=@orders.csv"

## Логика работы

Stage 1 используется сразу после создания заказа
Stage 2 используется после сборки и даёт более точную оценку риска

Модель оценивает риск отказа и выделяет проблемные заказы

## Интерпретация результата

prediction — прогноз (0 / 1)
probability — вероятность отказа

risk_group:

* green — низкий риск
* yellow — средний риск
* red — высокий риск

recommendation — рекомендация для обработки заказа

## Рекомендуемый сценарий использования

1. Отправить заказ в /model/predict/stage1
2. После сборки — в /model/predict/stage2
3. Если риск высокий (red):

   * позвонить клиенту
   * подтвердить заказ
   * предложить предоплату

## Проверка перед использованием

Рекомендуется проверить:

* сервис запущен
* /model/status отвечает
* модель обучена
* stage1 работает
* stage2 работает
* batch обрабатывает файл

## Важно

* передавайте максимально полный набор полей
* при отсутствии данных модель подставит пропуски
* это может снизить точность

## Кратко

Сервис позволяет:

* находить рискованные заказы
* приоритизировать обработку
* снижать потери
