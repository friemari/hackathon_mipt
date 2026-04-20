import pandas as pd
import numpy as np
from fastapi import FastAPI, HTTPException, UploadFile, File, Request
from fastapi.responses import JSONResponse, StreamingResponse
from typing import Optional, List, Dict, Any
import uvicorn
import logging
import io
import os
import tempfile
import threading
from datetime import datetime

from lead_scoring_model import LeadScoringModel

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class DataStore:
    original_df: pd.DataFrame = None
    cleaned_df: pd.DataFrame = None
    is_loaded: bool = False
    all_cities: List[str] = []
    all_delivery_services: List[str] = []
    all_payment_types: List[str] = []
    all_qualifications: List[str] = []


data_store = DataStore()

model: LeadScoringModel = None
model_status: str = "not_initialized"
model_training_started: Optional[datetime] = None
model_trained_at: Optional[datetime] = None
training_lock = threading.Lock()
model_path: str = "lead_scoring_model_final.pkl"


def apply_initial_filters(df: pd.DataFrame) -> pd.DataFrame:
    result = df.copy()
    if 'lifecycle_incomplete' in result.columns:
        result = result[result['lifecycle_incomplete'] == False]
        logger.info("  - applied lifecycle_incomplete filter")
    if 'outcome_unknown' in result.columns:
        result = result[result['outcome_unknown'] == False]
        logger.info("  - applied outcome_unknown filter")
    return result


def update_dynamic_lists(df: pd.DataFrame):
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


def init_model(log_file: str = "api_model.log"):
    global model, model_status, model_trained_at
    try:
        if os.path.exists(model_path):
            model = LeadScoringModel(log_file=log_file)
            model.load(model_path)
            model_status = "ready"
            model_trained_at = datetime.now()
            logger.info(f"Модель загружена из {model_path}")
        else:
            model = LeadScoringModel(log_file=log_file)
            model_status = "not_trained"
            logger.info("Создан новый экземпляр модели, требуется обучение")
    except Exception as e:
        logger.error(f"Ошибка инициализации модели: {e}")
        model = None
        model_status = "not_initialized"


def train_model_async(tmp_path: str):
    global model, model_status, model_training_started, model_trained_at
    try:
        logger.info("Начало обучения модели...")
        new_model = LeadScoringModel(log_file="api_model_training.log")
        new_model.fit(tmp_path)
        new_model.save(model_path)
        with training_lock:
            model = new_model
            model_status = "ready"
            model_trained_at = datetime.now()
            model_training_started = None
        logger.info("Модель успешно обучена и сохранена")
    except Exception as e:
        logger.exception(f"Ошибка обучения модели: {e}")
        with training_lock:
            model_status = "not_trained"
            model_training_started = None
    finally:
        if os.path.exists(tmp_path):
            os.remove(tmp_path)


def check_model_ready():
    global model, model_status, model_training_started
    with training_lock:
        current_status = model_status
        current_started = model_training_started
        current_model = model
    if current_status == "not_initialized":
        raise HTTPException(status_code=500, detail="Модель не инициализирована")
    if current_status == "not_trained":
        raise HTTPException(status_code=400, detail="Модель не обучена. Загрузите CSV файл через /model/train")
    if current_status == "training":
        raise HTTPException(
            status_code=409,
            detail={
                "message": "Модель находится в процессе обучения",
                "started_at": current_started.isoformat() if current_started else None,
                "status_endpoint": "/model/status"
            }
        )
    if current_model is None or not current_model.is_fitted:
        raise HTTPException(status_code=500, detail="Модель не готова к предсказаниям")


app = FastAPI(
    title="Order Processor Service with ML",
    description="Сервис для фильтрации, сортировки, анализа и предсказания выкупа заказов",
    version="2.0.0"
)


@app.on_event("startup")
async def startup_event():
    init_model()
    logger.info("Сервис запущен")


@app.get("/")
async def root():
    return {
        "service": "Order Processor Service with ML",
        "version": "2.0.0",
        "status": "running",
        "data_loaded": data_store.is_loaded,
        "records_original": len(data_store.original_df) if data_store.original_df is not None else 0,
        "records_cleaned": len(data_store.cleaned_df) if data_store.cleaned_df is not None else 0,
        "model_status": model_status
    }


@app.get("/status")
async def get_status():
    return {
        "data_loaded": data_store.is_loaded,
        "original_records": len(data_store.original_df) if data_store.original_df is not None else 0,
        "cleaned_records": len(data_store.cleaned_df) if data_store.cleaned_df is not None else 0,
        "model_status": model_status
    }


@app.post("/load")
async def load_dataset(file: UploadFile = File(...)):
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


@app.get("/model/status")
async def get_model_status():
    global model, model_status, model_training_started, model_trained_at
    with training_lock:
        current_model = model
        current_status = model_status
        current_started = model_training_started
        current_trained_at = model_trained_at
    if current_model is None:
        return {"status": "not_initialized", "message": "Модель не инициализирована"}
    if current_status == "training":
        return {
            "status": "training",
            "message": "Модель обучается",
            "started_at": current_started.isoformat() if current_started else None
        }
    if current_status == "not_trained":
        return {"status": "not_trained", "message": "Модель не обучена. Загрузите CSV файл через /model/train"}
    return {
        "status": "ready",
        "is_fitted": bool(current_model.is_fitted),
        "metrics": current_model.metrics,
        "feature_count_stage1": len(current_model.feature_columns_stage1) if current_model.feature_columns_stage1 else 0,
        "feature_count_stage2": len(current_model.feature_columns_stage2) if current_model.feature_columns_stage2 else 0,
        "trained_at": current_trained_at.isoformat() if current_trained_at else None
    }


@app.post("/model/train")
async def train_model_endpoint(file: UploadFile = File(...)):
    global model_status, model_training_started
    if not file.filename.endswith('.csv'):
        raise HTTPException(status_code=400, detail="Файл должен быть в формате CSV")
    with training_lock:
        if model_status == "training":
            raise HTTPException(
                status_code=409,
                detail="Обучение уже выполняется. Отслеживайте статус через /model/status"
            )
        model_status = "training"
        model_training_started = datetime.now()
    try:
        content = await file.read()
        df = pd.read_csv(io.BytesIO(content))
        logger.info(f"Загружено {len(df)} записей из {file.filename}")
        data_store.original_df = df
        data_store.cleaned_df = apply_initial_filters(df)
        update_dynamic_lists(data_store.cleaned_df)
        data_store.is_loaded = True
        with tempfile.NamedTemporaryFile(mode='w', suffix='.csv', delete=False, encoding='utf-8') as tmp:
            tmp_path = tmp.name
            data_store.original_df.to_csv(tmp_path, index=False)
        training_thread = threading.Thread(
            target=train_model_async,
            args=(tmp_path,),
            daemon=True
        )
        training_thread.start()
        return {
            "status": "training_started",
            "message": "Обучение модели запущено. Отслеживайте статус через /model/status",
            "records_processed": int(len(data_store.original_df)),
            "records_after_filters": int(len(data_store.cleaned_df)),
            "started_at": model_training_started.isoformat()
        }
    except Exception as e:
        with training_lock:
            model_status = "not_trained"
            model_training_started = None
        logger.exception(f"Ошибка запуска обучения: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/model/predict/stage1")
async def predict_stage1(request: Request):
    check_model_ready()
    try:
        body = await request.json()
        if isinstance(body, list):
            results = []
            for order in body:
                result = model.predict_single_stage1(order)
                results.append({
                    "input": {k: v for k, v in order.items() if k != 'extra_fields'},
                    "prediction": result
                })
            return {"status": "success", "count": len(results), "results": results}
        else:
            result = model.predict_single_stage1(body)
            return {"status": "success", "prediction": result}
    except Exception as e:
        logger.error(f"Ошибка предсказания Stage 1: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/model/predict/stage2")
async def predict_stage2(request: Request):
    check_model_ready()
    try:
        body = await request.json()
        if isinstance(body, list):
            results = []
            for order in body:
                result = model.predict_single_stage2(order)
                results.append({
                    "input": {k: v for k, v in order.items() if k != 'extra_fields'},
                    "prediction": result
                })
            return {"status": "success", "count": len(results), "results": results}
        else:
            result = model.predict_single_stage2(body)
            return {"status": "success", "prediction": result}
    except Exception as e:
        logger.error(f"Ошибка предсказания Stage 2: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/model/predict/batch")
async def predict_batch(file: UploadFile = File(...), stage: str = "stage1"):
    check_model_ready()
    try:
        content = await file.read()
        if file.filename.endswith('.csv'):
            df = pd.read_csv(io.BytesIO(content))
        elif file.filename.endswith(('.xlsx', '.xls')):
            df = pd.read_excel(io.BytesIO(content))
        else:
            raise HTTPException(status_code=400, detail="Неподдерживаемый формат. Используйте CSV или Excel.")
        results = []
        for _, row in df.iterrows():
            order_dict = row.to_dict()
            if stage == "stage1":
                result = model.predict_single_stage1(order_dict)
            else:
                result = model.predict_single_stage2(order_dict)
            results.append({"lead_id": order_dict.get('lead_id', 'unknown'), "prediction": result})
        df['prediction'] = [r['prediction']['prediction'] for r in results]
        df['probability'] = [r['prediction']['probability'] for r in results]
        df['risk_group'] = [r['prediction']['risk_group'] for r in results]
        df['recommendation'] = [r['prediction']['recommendation'] for r in results]
        for col in df.columns:
            if pd.api.types.is_datetime64_any_dtype(df[col]):
                df[col] = df[col].dt.strftime('%Y-%m-%d %H:%M:%S')
        return {
            "status": "success",
            "total_processed": len(results),
            "stage": stage,
            "summary": {
                "predicted_positive": sum(1 for r in results if r['prediction']['prediction'] == 1),
                "predicted_negative": sum(1 for r in results if r['prediction']['prediction'] == 0),
                "red_count": sum(1 for r in results if r['prediction']['risk_group'] == 'red'),
                "yellow_count": sum(1 for r in results if r['prediction']['risk_group'] == 'yellow'),
                "green_count": sum(1 for r in results if r['prediction']['risk_group'] == 'green')
            },
            "data": df.replace({np.nan: None}).to_dict(orient='records')
        }
    except Exception as e:
        logger.error(f"Ошибка пакетного предсказания: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/model/importance")
async def get_feature_importance(stage: str = "stage1"):
    check_model_ready()
    try:
        if stage == "stage1":
            importance = model.feature_importance['stage1']
        else:
            importance = model.feature_importance['stage2']
        if importance is None:
            return {"status": "error", "message": "Важность признаков недоступна"}
        return {
            "status": "success",
            "stage": stage,
            "importance": importance.head(20).to_dict(orient='records')
        }
    except Exception as e:
        logger.error(f"Ошибка получения важности признаков: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/fields")
async def get_fields():
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
        df_result = df_result.iloc[offset:offset + limit]
        if fields:
            available_fields = [f for f in fields if f in df_result.columns]
            df_result = df_result[available_fields]
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
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    try:
        body = await request.json()
        field = body.get('field')
        direction = body.get('direction', 'asc')
        replace = body.get('replace', False)
        if field not in data_store.cleaned_df.columns:
            raise HTTPException(status_code=404, detail=f"Поле '{field}' не найдено")
        ascending = direction.lower() == 'asc'
        df_sorted = data_store.cleaned_df.sort_values(by=field, ascending=ascending).copy()
        if replace:
            data_store.cleaned_df = df_sorted.copy()
        return {
            "status": "success",
            "message": f"Отсортировано по '{field}' ({direction})",
            "records": len(df_sorted),
            "replace_original": replace,
            "data": df_sorted.replace({np.nan: None}).to_dict(orient='records')
        }
    except Exception as e:
        logger.exception(f"Ошибка сортировки: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/filter")
async def filter_dataset(request: Request):
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    try:
        body = await request.json()
        filters = body.get('filters', [])
        replace = body.get('replace', False)
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
            data_store.cleaned_df = df_result.copy()
        return {
            "status": "success",
            "before_filtering": before_count,
            "after_filtering": len(df_result),
            "removed": before_count - len(df_result),
            "replace_original": replace,
            "data": df_result.replace({np.nan: None}).to_dict(orient='records')
        }
    except Exception as e:
        logger.exception(f"Ошибка фильтрации: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/export")
async def export_dataset(format: str = "json"):
    if not data_store.is_loaded or data_store.cleaned_df is None:
        raise HTTPException(status_code=400, detail="Датасет не загружен")
    df_export = data_store.cleaned_df.copy()
    for col in df_export.columns:
        if pd.api.types.is_datetime64_any_dtype(df_export[col]):
            df_export[col] = df_export[col].dt.strftime('%Y-%m-%d %H:%M:%S')
    if format.lower() == "csv":
        csv_bytes = df_export.to_csv(index=False).encode("utf-8-sig")
        return StreamingResponse(
            io.BytesIO(csv_bytes),
            media_type="text/csv; charset=utf-8",
            headers={"Content-Disposition": "attachment; filename=orders.csv"}
        )
    return {
        "status": "success",
        "records": len(df_export),
        "data": df_export.replace({np.nan: None}).to_dict(orient='records')
    }


if __name__ == "__main__":
    uvicorn.run(
        "order_processor:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
    