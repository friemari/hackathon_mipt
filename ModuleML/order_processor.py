import pandas as pd
import numpy as np
from fastapi import FastAPI, HTTPException, UploadFile, File, Request
from fastapi.responses import JSONResponse
from typing import Optional, List, Dict, Any
from datetime import datetime
import uvicorn
import logging
import io

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# ============================================
# ГЛОБАЛЬНОЕ ХРАНИЛИЩЕ ДАННЫХ
# ============================================

class DataStore:
    original_df: pd.DataFrame = None
    cleaned_df: pd.DataFrame = None
    is_loaded: bool = False
    all_cities: List[str] = []
    all_delivery_services: List[str] = []
    all_payment_types: List[str] = []
    all_qualifications: List[str] = []

data_store = DataStore()

# ============================================
# ФУНКЦИЯ ПРИМЕНЕНИЯ НАЧАЛЬНЫХ ФИЛЬТРОВ
# ============================================

def apply_initial_filters(df: pd.DataFrame) -> pd.DataFrame:
    """Применяет начальные фильтры: lifecycle_incomplete == False и outcome_unknown == False"""
    result = df.copy()
    
    if 'lifecycle_incomplete' in result.columns:
        result = result[result['lifecycle_incomplete'] == False]
        logger.info(f"  - applied lifecycle_incomplete filter")
    
    if 'outcome_unknown' in result.columns:
        result = result[result['outcome_unknown'] == False]
        logger.info(f"  - applied outcome_unknown filter")
    
    return result

def update_dynamic_lists(df: pd.DataFrame):
    """Обновляет динамические списки на основе данных из датасета"""
    if 'contact_Город' in df.columns:
        data_store.all_cities = sorted(df['contact_Город'].dropna().unique().tolist())
        logger.info(f"  - загружено {len(data_store.all_cities)} городов")
    
    if 'lead_Служба доставки' in df.columns:
        data_store.all_delivery_services = sorted(df['lead_Служба доставки'].dropna().unique().tolist())
        logger.info(f"  - загружено {len(data_store.all_delivery_services)} служб доставки")
    
    if 'lead_Вид оплаты' in df.columns:
        data_store.all_payment_types = sorted(df['lead_Вид оплаты'].dropna().unique().tolist())
        logger.info(f"  - загружено {len(data_store.all_payment_types)} способов оплаты")
    
    if 'lead_Квалификация лида' in df.columns:
        data_store.all_qualifications = sorted(df['lead_Квалификация лида'].dropna().unique().tolist())
        logger.info(f"  - загружено {len(data_store.all_qualifications)} квалификаций")

# ============================================
# FASTAPI ПРИЛОЖЕНИЕ
# ============================================

app = FastAPI(
    title="Order Processor Service",
    description="Сервис для фильтрации, сортировки и анализа заказов",
    version="1.0.0"
)

# ============================================
# API ЭНДПОИНТЫ
# ============================================

@app.get("/")
async def root():
    return {
        "service": "Order Processor Service",
        "version": "1.0.0",
        "status": "running",
        "data_loaded": data_store.is_loaded,
        "records_original": len(data_store.original_df) if data_store.original_df is not None else 0,
        "records_cleaned": len(data_store.cleaned_df) if data_store.cleaned_df is not None else 0
    }

@app.get("/status")
async def get_status():
    return {
        "data_loaded": data_store.is_loaded,
        "original_records": len(data_store.original_df) if data_store.original_df is not None else 0,
        "cleaned_records": len(data_store.cleaned_df) if data_store.cleaned_df is not None else 0
    }

@app.post("/load")
async def load_dataset(file: UploadFile = File(...)):
    """Загрузка датасета (CSV, Excel или JSON)"""
    global data_store
    
    logger.info(f"Загрузка файла: {file.filename}")
    
    try:
        content = await file.read()
        
        if file.filename.endswith('.csv'):
            df = pd.read_csv(io.BytesIO(content))
        elif file.filename.endswith(('.xlsx', '.xls')):
            df = pd.read_excel(io.BytesIO(content))
        elif file.filename.endswith('.json'):
            df = pd.read_json(io.BytesIO(content))
        else:
            raise HTTPException(status_code=400, detail="Неподдерживаемый формат. Используйте CSV, Excel или JSON.")
        
        data_store.original_df = df
        data_store.cleaned_df = apply_initial_filters(df)
        update_dynamic_lists(data_store.cleaned_df)
        data_store.is_loaded = True
        
        return {
            "status": "success",
            "original_records": len(data_store.original_df),
            "cleaned_records": len(data_store.cleaned_df),
            "removed_records": len(data_store.original_df) - len(data_store.cleaned_df),
            "columns": len(data_store.cleaned_df.columns)
        }
        
    except Exception as e:
        logger.error(f"Ошибка загрузки: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/fields")
async def get_fields():
    """Список всех полей из датасета"""
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    
    fields_info = []
    for col in data_store.cleaned_df.columns:
        non_null = data_store.cleaned_df[col].notna().sum()
        sample = data_store.cleaned_df[col].dropna().iloc[0] if non_null > 0 else None
        
        fields_info.append({
            "name": col,
            "type": str(data_store.cleaned_df[col].dtype),
            "non_null_count": int(non_null),
            "fill_rate": round(non_null / len(data_store.cleaned_df) * 100, 1),
            "sample": str(sample)[:100] if sample is not None else None
        })
    
    return {"total_fields": len(fields_info), "fields": fields_info}

@app.get("/stats/{field}")
async def get_field_stats(field: str):
    """Статистика по конкретному полю"""
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    
    if field not in data_store.cleaned_df.columns:
        raise HTTPException(status_code=404, detail=f"Поле '{field}' не найдено")
    
    series = data_store.cleaned_df[field].dropna()
    
    if pd.api.types.is_numeric_dtype(series):
        return {
            "field": field,
            "type": "numeric",
            "count": len(series),
            "min": float(series.min()),
            "max": float(series.max()),
            "mean": float(series.mean()),
            "median": float(series.median()),
            "std": float(series.std())
        }
    else:
        return {
            "field": field,
            "type": "categorical",
            "count": len(series),
            "unique": series.nunique(),
            "top_values": series.value_counts().head(20).to_dict()
        }

@app.get("/cities")
async def get_cities():
    if not data_store.is_loaded:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    return {"cities": data_store.all_cities, "count": len(data_store.all_cities)}

@app.get("/delivery_services")
async def get_delivery_services():
    if not data_store.is_loaded:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    return {"delivery_services": data_store.all_delivery_services, "count": len(data_store.all_delivery_services)}

@app.get("/payment_types")
async def get_payment_types():
    if not data_store.is_loaded:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    return {"payment_types": data_store.all_payment_types, "count": len(data_store.all_payment_types)}

@app.get("/qualifications")
async def get_qualifications():
    if not data_store.is_loaded:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    return {"qualifications": data_store.all_qualifications, "count": len(data_store.all_qualifications)}

@app.post("/query")
async def query_data(request: Request):
    """Запрос данных с фильтрацией и сортировкой"""
    import time
    start_time = time.time()
    
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    
    try:
        body = await request.json()
        
        filters = body.get('filters', [])
        sort = body.get('sort', [])
        limit = body.get('limit', 100)
        offset = body.get('offset', 0)
        fields = body.get('fields', None)
        
        df_result = data_store.cleaned_df.copy()
        
        # Применяем фильтры
        for f in filters:
            field = f.get('field')
            operator = f.get('operator')
            value = f.get('value')
            
            if field not in df_result.columns:
                continue
            
            if operator == "eq":
                df_result = df_result[df_result[field] == value]
            elif operator == "ne":
                df_result = df_result[df_result[field] != value]
            elif operator == "gt":
                df_result = df_result[df_result[field] > value]
            elif operator == "lt":
                df_result = df_result[df_result[field] < value]
            elif operator == "gte":
                df_result = df_result[df_result[field] >= value]
            elif operator == "lte":
                df_result = df_result[df_result[field] <= value]
            elif operator == "contains":
                df_result = df_result[df_result[field].astype(str).str.contains(str(value), case=False, na=False)]
        
        total_count = len(df_result)
        
        # Применяем сортировку
        if sort:
            sort_cols = []
            ascending = []
            for s in sort:
                field = s.get('field')
                direction = s.get('direction', 'asc')
                if field in df_result.columns:
                    sort_cols.append(field)
                    ascending.append(direction.lower() == 'asc')
            if sort_cols:
                df_result = df_result.sort_values(by=sort_cols, ascending=ascending)
        
        # Пагинация
        df_result = df_result.iloc[offset:offset + limit]
        
        # Выбираем поля
        if fields:
            available_fields = [f for f in fields if f in df_result.columns]
            df_result = df_result[available_fields]
        
        # Конвертируем datetime в строки
        for col in df_result.columns:
            if pd.api.types.is_datetime64_any_dtype(df_result[col]):
                df_result[col] = df_result[col].dt.strftime('%Y-%m-%d %H:%M:%S')
        
        result_data = df_result.replace({np.nan: None}).to_dict(orient='records')
        
        processing_time = (time.time() - start_time) * 1000
        
        return {
            "status": "success",
            "total_count": total_count,
            "returned_count": len(result_data),
            "data": result_data,
            "processing_time_ms": round(processing_time, 2)
        }
        
    except Exception as e:
        logger.error(f"Ошибка запроса: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/sort")
async def sort_dataset(request: Request):
    """Сортировка датасета по полю"""
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    
    try:
        body = await request.json()
        field = body.get('field')
        direction = body.get('direction', 'asc')
        
        if field not in data_store.cleaned_df.columns:
            raise HTTPException(status_code=404, detail=f"Поле '{field}' не найдено")
        
        ascending = direction.lower() == 'asc'
        data_store.cleaned_df = data_store.cleaned_df.sort_values(by=field, ascending=ascending)
        
        return {
            "status": "success",
            "message": f"Отсортировано по '{field}' ({direction})",
            "records": len(data_store.cleaned_df)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/filter")
async def filter_dataset(request: Request):
    """Фильтрация датасета по условиям"""
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    
    try:
        body = await request.json()
        filters = body.get('filters', [])
        replace = body.get('replace', True)
        
        df_result = data_store.cleaned_df.copy()
        
        for f in filters:
            field = f.get('field')
            operator = f.get('operator')
            value = f.get('value')
            
            if field not in df_result.columns:
                continue
            
            if operator == "eq":
                df_result = df_result[df_result[field] == value]
            elif operator == "ne":
                df_result = df_result[df_result[field] != value]
            elif operator == "gt":
                df_result = df_result[df_result[field] > value]
            elif operator == "lt":
                df_result = df_result[df_result[field] < value]
            elif operator == "gte":
                df_result = df_result[df_result[field] >= value]
            elif operator == "lte":
                df_result = df_result[df_result[field] <= value]
            elif operator == "contains":
                df_result = df_result[df_result[field].astype(str).str.contains(str(value), case=False, na=False)]
        
        before_count = len(data_store.cleaned_df)
        
        if replace:
            data_store.cleaned_df = df_result
        
        return {
            "status": "success",
            "before_filtering": before_count,
            "after_filtering": len(df_result),
            "removed": before_count - len(df_result),
            "replace_original": replace
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/export")
async def export_dataset(format: str = "json"):
    """Экспорт очищенного датасета"""
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    
    df_export = data_store.cleaned_df.copy()
    
    for col in df_export.columns:
        if pd.api.types.is_datetime64_any_dtype(df_export[col]):
            df_export[col] = df_export[col].dt.strftime('%Y-%m-%d %H:%M:%S')
    
    if format.lower() == "csv":
        return JSONResponse(
            content={"csv": df_export.to_csv(index=False)},
            headers={"Content-Disposition": "attachment; filename=orders.csv"}
        )
    else:
        return {
            "status": "success",
            "records": len(df_export),
            "data": df_export.replace({np.nan: None}).to_dict(orient='records')
        }

# ============================================
# ЗАПУСК СЕРВЕРА
# ============================================

if __name__ == "__main__":
    uvicorn.run(
        "order_processor:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )