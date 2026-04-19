# Создать папку для сервиса
mkdir order_processor_service
cd order_processor_service

# Создать виртуальное окружение
python3 -m venv venv

# Активировать окружение
source venv/bin/activate

# Установить зависимости
pip install fastapi uvicorn pandas numpy scikit-learn joblib python-multipart catboost python-dateutil

# Запустить сервер
python order_processor.py
Сервер запустится на http://localhost:8000
