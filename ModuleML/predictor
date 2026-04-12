import pandas as pd
import numpy as np
import pickle
from datetime import datetime
import re
import sys
import os
from sklearn.model_selection import train_test_split
from sklearn.metrics import f1_score, roc_auc_score
from catboost import CatBoostClassifier


class Logger:
    """Класс для логирования в консоль и файл."""
    
    def __init__(self, log_file="model_training.log"):
        self.log_file = log_file
        self.console = sys.stdout
        
        log_dir = os.path.dirname(log_file)
        if log_dir and not os.path.exists(log_dir):
            os.makedirs(log_dir)
        
        self.file = open(log_file, 'a', encoding='utf-8')
    
    def log(self, message):
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        formatted = f"[{timestamp}] {message}"
        print(formatted)
        self.file.write(formatted + '\n')
        self.file.flush()
    
    def close(self):
        self.file.close()
    
    def __del__(self):
        self.close()


class LeadScoringModel:
    """
    Класс для двухэтапного предсказания выкупа заказа с рекомендациями.
    """
    
    def __init__(self, model_params=None, log_file="lead_scoring_model.log"):
        self.logger = Logger(log_file)
        
        if model_params is None:
            self.model_params = {
                'iterations': 500,
                'learning_rate': 0.05,
                'depth': 5,
                'random_seed': 42,
                'verbose': 100,
                'eval_metric': 'AUC',
                'od_type': 'Iter',
                'od_wait': 50
            }
        else:
            self.model_params = model_params
        
        self.pipeline_stage1 = None
        self.pipeline_stage2 = None
        self.feature_columns_stage1 = None
        self.feature_columns_stage2 = None
        self.cat_features_stage1 = None
        self.cat_features_stage2 = None
        self.is_fitted = False
        self.metrics = {'stage1': {}, 'stage2': {}}
        self.feature_importance = {'stage1': None, 'stage2': None}
        self.validation_log = {}
        self.date_mismatches_warning = []
        self.date_mismatches_critical = []
        
        self.avg_delivery_cost = None
        self.avg_return_cost = None
        self.avg_order_value = None
        
        self.leakage_columns = [
            'handed_to_delivery_ts', 'issued_or_pvz_ts', 'received_ts',
            'rejected_ts', 'returned_ts', 'closed_ts',
            'days_sale_to_handed', 'days_handed_to_issued_pvz', 'days_to_outcome',
            'buyout_flag', 'outcome_unknown', 'lifecycle_incomplete',
            'current_status_id', 'lead_status_id',
            'lead_updated_at', 'lead_closed_at', 'contact_updated_at',
            'lead_Дата получения денег на Р/С', 'lead_Счет оплачен', 'lead_Оплачено клиентом',
            'contact_LTV', 'lead_LTV',
            'lead_Дата возврата посылки на склад',
            'lead_Дата перехода Передан в доставку', 'lead_Дата перехода в Сборку',
            'lead_Дата создания накладной СДЭК',
            'lead_Дата предполагаемой доставки отправления',
            'lead_Дата создания сделки',
            'lead_Условный отказ',
            'lead_name', 'lead_created_at', 'sale_date',
            'lead_будущие покупки', 'lead_pipeline_id',
            'lead_Трек-номер СДЭК', 'lead_Трек-номер',
            'contact_Число сделок',
        ]
        
        self.technical_columns = [
            'lead_updated_at', 'contact_updated_at', 'contact_edited',
            'lead_created_add',
        ]
        
        self.id_columns = [
            'lead_id', 'contact_id', 'lead_responsible_user_id',
            'contact_responsible_user_id', 'lead_group_id', 'lead_account_id',
            'lead_TRANID', 'lead_clientID', 'lead_FORMID', 'lead_roistat',
            'lead_COOKIES', 'lead_yclid', 'lead__ym_uid', 'lead_YSCLID',
            'contact_Max User ID', 'contact_Max ID', 'lead_ID реферала на сайте',
            'contact_id партнёра на сайте', 'lead_Нумерация сделки',
        ]
        
        self.tracking_columns = [
            'lead_REFERER', 'lead_utm_referrer', 'lead_URL страницы',
            'lead_Ссылка на бланк отправления', 'lead_Ссылка на документ',
            'contact_Ссылка на документ', 'lead_ROISTAT_REFERRER',
            'lead_ROISTAT_URL', 'lead_ROISTAT_FIELDS_ROISTAT',
            'lead_ACTUAL-FORMAT', 'lead_BANNER-SIZES', 'lead_WIDTH', 'lead_HEIGHT',
            'lead_CLICKX', 'lead_CLICKY', 'lead_CLICK-TIME', 'lead_CTIME',
            'lead_STAT-ID', 'lead_PCODEVER', 'lead_PCODE-ACTIVE-TESTIDS',
            'lead_RENDERED-DIRECT-ASSETS', 'lead_CONSTRUCTOR-RENDERED-ASSETS',
            'lead_ASSET-CLICK', 'lead_POLICY', 'lead_POLICY_MARKETING',
            'lead_TEST-TAG', 'lead_BANNER-TEST-TAGS', 'lead_BANNERS-VIDEOS',
            'lead_ORDER-BANNERS-OPTIONS', 'lead_YPRQEE', 'lead_CB', 'lead_RS_STAT',
            'lead_ETEXT', 'lead_YSCLID', 'lead_ROISTAT_POS', 'lead_Solo', 'lead_YBAIP',
            'lead_from', 'lead_Source phone', 'lead_Номер линии билайн:',
        ]
    
    def _ts_to_datetime(self, ts):
        try:
            return pd.to_datetime(ts, unit='s', errors='coerce')
        except:
            return pd.NaT
    
    def _parse_date_flexible(self, date_val):
        if pd.isna(date_val):
            return pd.NaT
        if isinstance(date_val, (int, float)):
            return self._ts_to_datetime(date_val)
        date_str = str(date_val).strip()
        if date_str == '':
            return pd.NaT
        formats_to_try = ['%Y-%m-%d', '%d.%m.%Y', '%Y-%m-%d %H:%M:%S', '%d.%m.%Y %H:%M:%S', '%Y/%m/%d', '%d/%m/%Y']
        for fmt in formats_to_try:
            try:
                return pd.to_datetime(date_str, format=fmt, errors='coerce')
            except:
                continue
        return pd.to_datetime(date_str, errors='coerce')
    
    def _parse_product_type(self, composition):
        if pd.isna(composition) or str(composition).strip() == '':
            return 'unknown'
        text = str(composition).lower()
        articles = re.findall(r'артикул[:\s]*\d+', text)
        count = len(articles)
        if count == 0:
            quantities = re.findall(r'кол-во[:\s]*(\d+)', text)
            if quantities:
                total_qty = sum(int(q) for q in quantities)
                return 'single' if total_qty == 1 else 'set'
            return 'unknown'
        return 'single' if count == 1 else 'set'
    
    def _validate_and_filter_data(self, df):
        df = df.copy()
        initial_count = len(df)
        validation_log = {
            'initial_count': initial_count,
            'lifecycle_incomplete_removed': 0,
            'outcome_unknown_removed': 0,
            'timestamp_mismatches_total': 0,
            'timestamp_mismatches_warning': 0,
            'timestamp_mismatches_critical': 0,
            'final_count': 0
        }
        
        if 'lifecycle_incomplete' in df.columns:
            mask = df['lifecycle_incomplete'].astype(str).str.lower() == 'true'
            validation_log['lifecycle_incomplete_removed'] = mask.sum()
            df = df[~mask]
            self.logger.log(f"  Удалено незавершённых сделок (lifecycle_incomplete=True): {validation_log['lifecycle_incomplete_removed']}")
        
        if 'outcome_unknown' in df.columns:
            mask = df['outcome_unknown'].astype(str).str.lower() == 'true'
            validation_log['outcome_unknown_removed'] = mask.sum()
            df = df[~mask]
            self.logger.log(f"  Удалено сделок с неизвестным исходом (outcome_unknown=True): {validation_log['outcome_unknown_removed']}")
        
        ts_col = 'handed_to_delivery_ts'
        date_col = 'lead_Дата перехода Передан в доставку'
        
        if ts_col in df.columns and date_col in df.columns:
            df['_ts_date'] = pd.to_datetime(df[ts_col], unit='s', errors='coerce').dt.date
            df['_field_datetime'] = df[date_col].apply(self._parse_date_flexible)
            df['_field_date'] = df['_field_datetime'].dt.date
            
            mismatch_mask = (
                df['_ts_date'].notna() & 
                df['_field_date'].notna() & 
                (df['_ts_date'] != df['_field_date'])
            )
            
            df['_diff_days'] = (pd.to_datetime(df['_ts_date']) - pd.to_datetime(df['_field_date'])).dt.days.abs()
            
            validation_log['timestamp_mismatches_total'] = mismatch_mask.sum()
            
            if validation_log['timestamp_mismatches_total'] > 0:
                mismatches = df[mismatch_mask].copy()
                mismatches['diff_days'] = mismatches['_diff_days']
                
                warning_mask = (mismatches['diff_days'] >= 2) & (mismatches['diff_days'] <= 7)
                critical_mask = mismatches['diff_days'] > 7
                
                warning_df = mismatches[warning_mask][['lead_id', '_ts_date', '_field_date', 'diff_days']].copy()
                warning_df.columns = ['lead_id', 'ts_date', 'field_date', 'diff_days']
                warning_df['severity'] = 'warning'
                
                critical_df = mismatches[critical_mask][['lead_id', '_ts_date', '_field_date', 'diff_days']].copy()
                critical_df.columns = ['lead_id', 'ts_date', 'field_date', 'diff_days']
                critical_df['severity'] = 'critical'
                
                self.date_mismatches_warning = warning_df.to_dict('records')
                self.date_mismatches_critical = critical_df.to_dict('records')
                
                validation_log['timestamp_mismatches_warning'] = len(warning_df)
                validation_log['timestamp_mismatches_critical'] = len(critical_df)
                
                normal_count = validation_log['timestamp_mismatches_total'] - len(warning_df) - len(critical_df)
                self.logger.log(f"  Сравнение дат передачи в доставку (handed_to_delivery_ts и поле CRM):")
                self.logger.log(f"     - совпадают с точностью до 1 дня: {normal_count}")
                self.logger.log(f"     - расхождение 2-7 дней: {len(warning_df)}")
                self.logger.log(f"     - расхождение более 7 дней: {len(critical_df)}")
            
            df = df.drop(['_ts_date', '_field_datetime', '_field_date', '_diff_days'], axis=1)
        
        validation_log['final_count'] = len(df)
        self.validation_log = validation_log
        return df.reset_index(drop=True)
    
    def _calculate_customer_history(self, df):
        if 'contact_id' not in df.columns or 'sale_ts' not in df.columns:
            return df
        
        df = df.sort_values(['contact_id', 'sale_ts'])
        history_features = []
        
        for contact_id, group in df.groupby('contact_id'):
            group = group.copy()
            group['customer_order_number'] = range(1, len(group) + 1)
            group['prev_orders_count'] = group['customer_order_number'].shift(1).fillna(0)
            
            if 'lead_price' in group.columns:
                group['lead_price_numeric'] = pd.to_numeric(group['lead_price'], errors='coerce').fillna(0)
                group['prev_total_spent'] = group['lead_price_numeric'].shift(1).cumsum().fillna(0)
                group['prev_avg_check'] = group['prev_total_spent'] / group['prev_orders_count']
                group['prev_avg_check'] = group['prev_avg_check'].fillna(0)
                group = group.drop('lead_price_numeric', axis=1)
            
            if 'buyout_flag' in group.columns:
                group['prev_successful_orders'] = group['buyout_flag'].shift(1).cumsum().fillna(0)
                group['prev_success_rate'] = group['prev_successful_orders'] / group['prev_orders_count']
                group['prev_success_rate'] = group['prev_success_rate'].fillna(0)
            
            group['days_since_last_order'] = (group['sale_ts'] - group['sale_ts'].shift(1)) / 86400
            group['days_since_last_order'] = group['days_since_last_order'].fillna(-1)
            
            group['customer_type'] = group['prev_orders_count'].apply(
                lambda x: 'new' if x == 0 else ('repeat' if x <= 3 else 'vip')
            )
            group['is_new_customer'] = (group['customer_type'] == 'new').astype(int)
            group['is_repeat_customer'] = (group['customer_type'] == 'repeat').astype(int)
            group['is_vip_customer'] = (group['customer_type'] == 'vip').astype(int)
            
            group['prev_orders_category'] = pd.cut(
                group['prev_orders_count'],
                bins=[-1, 0, 1, 3, 10, 1000],
                labels=['new', '1_order', '2-3_orders', '4-10_orders', '10+_orders']
            )
            
            history_features.append(group)
        
        return pd.concat(history_features).reset_index(drop=True)
    
    def _load_and_clean_data(self, csv_path):
        self.logger.log(f"Загрузка данных из {csv_path}")
        
        dtype_dict = {
            'lead_Оплата МОП': 'str', 'lead_Счет оплачен': 'str', 'lead_Название товара': 'str',
            'lead_Наименование получателя': 'str', 'lead_ФИО': 'str', 'lead_Адрес доставки': 'str',
            'contact_TelegramId_WZ': 'str', 'lead_СМС-уведомления': 'str', 'contact_TelegramUsername_WZ': 'str',
            'lead_Объявление': 'str', 'lead_URL объявления': 'str', 'lead_Поиск товаров GoSklad': 'str',
            'lead_Список товаров GoSklad': 'str', 'lead_Колесо': 'str', 'contact_Канал Callibri': 'str',
            'lead_ROISTAT_REFERRER': 'str', 'lead_Категория и варианты выбора': 'str', 'lead_Ссылка на документ': 'str',
            'lead_Выбранные товары': 'str', 'contact_Ссылка на документ': 'str', 'lead_Solo': 'str',
            'lead_YBAIP': 'str', 'lead_from': 'str', 'lead_Source phone': 'str', 'lead_ИМ сдэк': 'str',
            'lead_Комментарий к отправлению': 'str', 'lead_POLICY': 'str', 'lead_LEADQUALIFYCATION': 'str',
            'lead_ROISTAT_POS': 'str', 'lead_ROISTAT_FIELDS_ROISTAT': 'str', 'lead_ROISTAT_URL': 'str',
            'lead_TEST-TAG': 'str', 'lead_BANNER-SIZES': 'str', 'lead_CTIME': 'str', 'lead_BANNER-TEST-TAGS': 'str',
            'lead_BANNERS-VIDEOS': 'str', 'lead_CONSTRUCTOR-RENDERED-ASSETS': 'str', 'lead_RENDERED-DIRECT-ASSETS': 'str',
            'lead_PCODE-ACTIVE-TESTIDS': 'str', 'lead_ORDER-BANNERS-OPTIONS': 'str', 'lead_CLICK-TIME': 'str',
            'lead_CLICKX': 'str', 'lead_CLICKY': 'str', 'lead_YPRQEE': 'str', 'lead_CB': 'str',
            'lead_RS_STAT': 'str', 'lead_Номер линии билайн:': 'str', 'lead_ETEXT': 'str',
            'lead_Ответственный за доставку': 'str', 'lead_AI_abcde': 'str', 'lead_YSCLID': 'str',
            'contact_Max ID': 'str', 'lead_POLICY_MARKETING': 'str', 'lead_ASSET-CLICK': 'str', 'lead_URL': 'str',
        }
        
        df = pd.read_csv(csv_path, encoding='utf-8', low_memory=False, dtype=dtype_dict)
        self.logger.log(f"  Исходный размер: {df.shape[0]} записей, {df.shape[1]} признаков")
        
        df = df.dropna(how='all')
        
        if 'lead_Состав заказа' in df.columns:
            before = len(df)
            df = df[df['lead_Состав заказа'].notna()]
            df = df[df['lead_Состав заказа'].astype(str).str.strip() != '']
            if before - len(df) > 0:
                self.logger.log(f"  Удалено тестовых заказов (без состава): {before - len(df)}")
        
        if 'lead_id' in df.columns:
            before = len(df)
            df = df.drop_duplicates(subset=['lead_id'], keep='first')
            if before - len(df) > 0:
                self.logger.log(f"  Удалено дубликатов: {before - len(df)}")
        
        if 'buyout_flag' in df.columns:
            if pd.api.types.is_object_dtype(df['buyout_flag']) or pd.api.types.is_string_dtype(df['buyout_flag']):
                df['buyout_flag'] = df['buyout_flag'].astype(str).str.lower() == 'true'
            df['buyout_flag'] = df['buyout_flag'].astype(int)
        
        self.logger.log("  Валидация данных...")
        df = self._validate_and_filter_data(df)
        
        return df
    
    def _create_features(self, df, stage='stage1'):
        df = df.copy()
        
        ts_columns = ['sale_ts', 'contact_created_at']
        for col in ts_columns:
            if col in df.columns:
                df[f'{col}_dt'] = self._ts_to_datetime(df[col])
        
        if 'sale_ts_dt' in df.columns:
            df['sale_dayofweek'] = df['sale_ts_dt'].dt.dayofweek
            df['sale_hour'] = df['sale_ts_dt'].dt.hour
            df['sale_month'] = df['sale_ts_dt'].dt.month
            df['sale_is_weekend'] = df['sale_dayofweek'].isin([5, 6]).astype(int)
            df['sale_quarter'] = df['sale_ts_dt'].dt.quarter
        
        if stage == 'stage2':
            if 'sale_ts' in df.columns and 'handed_to_delivery_ts' in df.columns:
                df['days_assembly'] = (df['handed_to_delivery_ts'] - df['sale_ts']) / 86400
                df['days_assembly'] = df['days_assembly'].clip(lower=0)
                df['assembly_speed'] = pd.cut(
                    df['days_assembly'],
                    bins=[-1, 1, 3, 7, 1000],
                    labels=['same_day', 'fast', 'normal', 'slow']
                )
                df['log_days_assembly'] = np.log1p(df['days_assembly'])
        
        if 'sale_ts' in df.columns and 'contact_created_at' in df.columns:
            df['days_contact_to_order'] = (df['sale_ts'] - df['contact_created_at']) / 86400
            df['days_contact_to_order'] = df['days_contact_to_order'].clip(lower=0)
            df['is_fresh_contact'] = (df['days_contact_to_order'] <= 1).astype(int)
        
        numeric_cols = [
            'lead_price', 'lead_Сумма заказа', 'lead_Сумма наложенного платежа (руб)',
            'lead_Стоимость доставки', 'lead_Объявленная ценность (руб)',
            'lead_Масса (гр)', 'lead_Вес (грамм)*', 'lead_Цена товара',
        ]
        for col in numeric_cols:
            if col in df.columns:
                df[col] = pd.to_numeric(df[col], errors='coerce')
        
        if 'lead_price' in df.columns:
            df['log_price'] = np.log1p(df['lead_price'].fillna(0))
            try:
                df['price_bucket'] = pd.qcut(df['lead_price'].fillna(0), q=5, duplicates='drop', labels=False)
            except:
                df['price_bucket'] = 0
            
            self._median_price = df['lead_price'].median()
            df['is_high_price'] = (df['lead_price'] > self._median_price).astype(int)
        
        if 'lead_Масса (гр)' in df.columns and 'lead_Вес (грамм)*' in df.columns:
            df['weight_grams'] = df['lead_Масса (гр)'].fillna(df['lead_Вес (грамм)*'])
        elif 'lead_Масса (гр)' in df.columns:
            df['weight_grams'] = df['lead_Масса (гр)']
        elif 'lead_Вес (грамм)*' in df.columns:
            df['weight_grams'] = df['lead_Вес (грамм)*']
        
        if 'weight_grams' in df.columns:
            df['log_weight'] = np.log1p(df['weight_grams'].fillna(0))
        
        if 'lead_price' in df.columns and 'weight_grams' in df.columns:
            df['price_per_gram'] = df['lead_price'] / (df['weight_grams'].fillna(1) + 1)
            df['log_price_per_gram'] = np.log1p(df['price_per_gram'])
        
        if 'lead_Состав заказа' in df.columns:
            df['product_type'] = df['lead_Состав заказа'].apply(self._parse_product_type)
            df['is_single_item'] = (df['product_type'] == 'single').astype(int)
            df['is_set'] = (df['product_type'] == 'set').astype(int)
        
        if 'lead_Скидка' in df.columns:
            df['has_discount'] = (
                df['lead_Скидка'].notna() & 
                (df['lead_Скидка'].astype(str).str.strip() != '') &
                (df['lead_Скидка'].astype(str).str.strip() != '0')
            ).astype(int)
        
        if 'lead_tags' in df.columns:
            common_tags = ['tilda', 'yur', 'npotpz.ru', 'Callibri', 'Baza ARTRAID']
            for tag in common_tags:
                df[f'tag_{tag}'] = df['lead_tags'].astype(str).str.contains(tag, case=False).astype(int)
            df['has_tags'] = df['lead_tags'].notna().astype(int)
        
        if 'lead_Категория и варианты выбора' in df.columns:
            disc_series = df['lead_Категория и варианты выбора'].fillna('').astype(str)
            df['disc_profile'] = disc_series.str[0]
            df['disc_profile'] = df['disc_profile'].where(
                df['disc_profile'].isin(['D', 'I', 'S', 'C']), 'unknown'
            )
            for profile in ['D', 'I', 'S', 'C']:
                df[f'disc_is_{profile}'] = (df['disc_profile'] == profile).astype(int)
            df['disc_is_known'] = (df['disc_profile'] != 'unknown').astype(int)
            df['disc_is_unknown'] = (df['disc_profile'] == 'unknown').astype(int)
        
        if 'lead_Квалификация лида' in df.columns:
            df['lead_qualification'] = df['lead_Квалификация лида'].fillna('unknown')
        else:
            df['lead_qualification'] = 'unknown'
        df['has_qualification'] = (df['lead_qualification'] != 'unknown').astype(int)
        
        if 'contact_Город' in df.columns:
            city_lower = df['contact_Город'].fillna('').astype(str).str.lower()
            far_regions = ['сахалин', 'владивосток', 'хабаровск', 'камчат', 'магадан', 'якут', 'южно-сахалинск']
            def check_far_region(city):
                if not city:
                    return False
                return any(region in city for region in far_regions)
            df['is_far_region'] = city_lower.apply(check_far_region).astype(int)
        else:
            df['is_far_region'] = 0
        
        df['estimated_delivery_days'] = 3
        if 'is_far_region' in df.columns:
            df.loc[df['is_far_region'] == 1, 'estimated_delivery_days'] = 10
        df['is_long_delivery'] = (df['estimated_delivery_days'] >= 7).astype(int)
        
        if 'disc_profile' in df.columns and 'customer_type' in df.columns:
            df['disc_customer_type'] = (
                df['disc_profile'].astype(str) + '_' + df['customer_type'].astype(str)
            )
        
        if 'sale_is_weekend' in df.columns and 'is_new_customer' in df.columns:
            df['weekend_new_customer'] = df['sale_is_weekend'] * df['is_new_customer']
        
        if 'is_far_region' in df.columns and 'assembly_speed' in df.columns:
            df['far_region_slow'] = (
                df['is_far_region'] * (df['assembly_speed'] == 'slow').astype(int)
            )
        
        if 'disc_is_I' in df.columns and 'is_new_customer' in df.columns:
            df['disc_i_new_customer'] = df['disc_is_I'] * df['is_new_customer']
        
        if 'is_high_price' in df.columns and 'is_far_region' in df.columns:
            df['high_price_far_region'] = df['is_high_price'] * df['is_far_region']
        
        if 'tag_tilda' in df.columns and 'is_new_customer' in df.columns:
            df['tilda_new_customer'] = df['tag_tilda'] * df['is_new_customer']
        
        if 'is_far_region' in df.columns and 'is_single_item' in df.columns:
            df['far_region_single_item'] = df['is_far_region'] * df['is_single_item']
        
        if 'sale_is_weekend' in df.columns and 'is_far_region' in df.columns:
            df['weekend_far_region'] = df['sale_is_weekend'] * df['is_far_region']
        
        dt_cols = [c for c in df.columns if c.endswith('_dt')]
        df = df.drop(columns=dt_cols, errors='ignore')
        
        return df
    
    def _select_features(self, df, target_col='buyout_flag', stage='stage1'):
        cols_to_drop = set()
        cols_to_drop.update(self.leakage_columns)
        cols_to_drop.update(self.technical_columns)
        cols_to_drop.update(self.id_columns)
        cols_to_drop.update(self.tracking_columns)
        cols_to_drop.add(target_col)
        
        ts_cols = [c for c in df.columns if c.endswith('_ts')]
        cols_to_drop.update(ts_cols)
        
        text_patterns = ['Комментарий', 'Состав', 'Список', 'Поиск', 'Проблема', 'Адрес', 'REFERER', 'URL']
        for col in df.columns:
            if any(p in col for p in text_patterns):
                cols_to_drop.add(col)
        
        if stage == 'stage1':
            assembly_related = ['days_assembly', 'assembly_speed', 'handed_to_delivery', 'log_days_assembly']
            cols_to_drop.update([c for c in df.columns if any(a in c for a in assembly_related)])
        
        nunique = df.nunique()
        const_cols = nunique[nunique <= 1].index.tolist()
        cols_to_drop.update(const_cols)
        
        missing_ratio = df.isnull().mean()
        high_missing = missing_ratio[missing_ratio > 0.5].index.tolist()
        cols_to_drop.update(high_missing)
        
        dt_cols = [c for c in df.columns if c.endswith('_dt')]
        cols_to_drop.update(dt_cols)
        
        cols_to_drop = [c for c in cols_to_drop if c in df.columns]
        X = df.drop(columns=cols_to_drop, errors='ignore')
        
        remaining_cols = set(X.columns)
        still_leaking = [c for c in self.leakage_columns if c in remaining_cols]
        if still_leaking:
            self.logger.log(f"  Внимание: в данных остались поля-утечки: {still_leaking[:10]}")
        
        return X
    
    def _check_data_leakage(self, X, y):
        self.logger.log("")
        self.logger.log("Проверка корреляций с целевой переменной (поиск утечек)")
        
        num_cols = X.select_dtypes(include=['int64', 'float64', 'int32', 'float32']).columns
        correlations = X[num_cols].corrwith(y).abs().sort_values(ascending=False)
        
        self.logger.log("  Топ-10 признаков по абсолютной корреляции:")
        for col, corr in correlations.head(10).items():
            self.logger.log(f"    {col}: {corr:.6f}")
        
        high_corr = correlations[correlations > 0.3].index.tolist()
        if high_corr:
            self.logger.log(f"  Внимание: признаки с корреляцией >0.3 (возможные утечки): {high_corr}")
        else:
            self.logger.log("  Признаков с аномально высокой корреляцией не найдено.")
    
    def _generate_recommendation(self, probability, features, stage='stage1'):
        risk_group = 'green'
        if probability >= 0.7:
            risk_group = 'red'
        elif probability >= 0.3:
            risk_group = 'yellow'
        
        reasons = []
        actions = []
        
        if risk_group == 'red':
            if features.get('is_new_customer', 0) == 1:
                reasons.append("новый клиент")
            if features.get('sale_is_weekend', 0) == 1:
                reasons.append("заказ в выходные")
            if features.get('is_far_region', 0) == 1:
                reasons.append("удалённый регион")
            if features.get('disc_profile', '') == 'I':
                reasons.append("импульсивный профиль (DISC I)")
            
            if reasons:
                reason_str = ", ".join(reasons)
                actions.append(f"Обязательный обзвон. Факторы риска: {reason_str}.")
                if features.get('is_far_region', 0) == 1:
                    delivery_method = features.get('delivery_method', '')
                    if stage == 'stage1':
                        if 'Двери' in delivery_method or 'Курьер' in delivery_method:
                            actions.append("Клиент выбрал доставку курьером в удалённый регион. Предложить сменить способ доставки на ПВЗ до отправки.")
                        else:
                            actions.append("Рекомендуется SMS-подтверждение за день до прибытия в ПВЗ.")
                    else:
                        actions.append("Заказ уже в пути. Смена способа доставки невозможна. Обзвон для подтверждения получения.")
                elif features.get('disc_profile', '') == 'I':
                    actions.append("Мягкий скрипт с акцентом на выгоду и срочность.")
                else:
                    actions.append("Подтвердить заказ, уточнить детали доставки.")
            else:
                actions.append("Обязательный обзвон. Высокая вероятность отказа.")
        
        elif risk_group == 'yellow':
            if features.get('is_far_region', 0) == 1:
                actions.append("Уведомить клиента о сроках доставки (до 10-14 дней).")
            if features.get('assembly_speed') == 'slow':
                actions.append("Ускорить сборку или уведомить клиента о задержке.")
            if features.get('is_new_customer', 0) == 1:
                actions.append("Отправить SMS с подтверждением заказа.")
            if not actions:
                actions.append("Стандартная обработка, наблюдать за сроками сборки.")
        
        else:
            actions.append("Автоматическая обработка. Дополнительных действий не требуется.")
        
        return {
            'risk_group': risk_group,
            'risk_factors': reasons,
            'recommendation': ' '.join(actions)
        }
    
    def print_business_diagnostics(self, y_test, y_proba, stage_name="Stage 1"):
        self.logger.log("")
        self.logger.log(f"Диагностика модели: {stage_name}")
        
        bins = [0, 0.3, 0.7, 1.0]
        labels = ['высокий риск', 'средний риск', 'низкий риск']
        risk_groups = pd.cut(y_proba, bins=bins, labels=labels)
        
        self.logger.log("  Распределение заказов по группам риска:")
        for label in labels:
            count = (risk_groups == label).sum()
            pct = count / len(risk_groups) * 100
            self.logger.log(f"    {label}: {count} заказов ({pct:.1f}%)")
        
        self.logger.log("  Фактическая доля отказов в каждой группе:")
        for label in labels:
            mask = risk_groups == label
            if mask.sum() > 0:
                churn_rate = 1 - y_test[mask].mean()
                churn_count = (y_test[mask] == 0).sum()
                self.logger.log(f"    {label}: {churn_rate:.1%} ({churn_count} из {mask.sum()})")
        
        total_churns = (y_test == 0).sum()
        red_churns = ((risk_groups == labels[0]) & (y_test == 0)).sum()
        red_total = (risk_groups == labels[0]).sum()
        
        if red_total > 0:
            self.logger.log("  Эффективность модели:")
            self.logger.log(f"    Всего отказов в выборке: {total_churns}")
            self.logger.log(f"    Из них попало в группу высокого риска: {red_churns} ({red_churns/total_churns:.1%})")
            self.logger.log(f"    Модель находит {red_churns/total_churns:.1%} отказов, проверяя только {red_total} заказов ({red_total/len(y_test):.1%} от всех)")
        
        self.logger.log("  Примерный экономический эффект (при обзвоне только группы высокого риска):")
        saved_calls = len(y_test) - red_total
        self.logger.log(f"    Сэкономлено звонков: {saved_calls} из {len(y_test)} ({saved_calls/len(y_test):.1%})")
        self.logger.log(f"    Потенциально спасено заказов (при эффективности обзвона 30%): ~{int(red_churns * 0.3)}")
    
    def _calculate_losses(self, df):
        self.logger.log("")
        self.logger.log("Расчёт потерь от отказов")
        
        total_orders = len(df)
        rejected_orders = (df['buyout_flag'] == 0).sum()
        reject_rate = rejected_orders / total_orders
        
        if 'lead_Стоимость доставки' in df.columns:
            self.avg_delivery_cost = df['lead_Стоимость доставки'].mean()
        else:
            self.avg_delivery_cost = 350
        
        self.avg_return_cost = self.avg_delivery_cost * 1.2
        
        if 'lead_price' in df.columns:
            self.avg_order_value = df['lead_price'].mean()
        else:
            self.avg_order_value = 3000
        
        loss_per_reject = self.avg_delivery_cost + self.avg_return_cost
        total_losses = rejected_orders * loss_per_reject
        lost_profit = rejected_orders * self.avg_order_value * 0.3
        
        self.logger.log(f"  Всего заказов: {total_orders}")
        self.logger.log(f"  Отказов: {rejected_orders} ({reject_rate:.2%})")
        self.logger.log(f"  Средняя стоимость доставки: {self.avg_delivery_cost:.0f} руб.")
        self.logger.log(f"  Средняя стоимость возврата: {self.avg_return_cost:.0f} руб.")
        self.logger.log(f"  Потери на один отказ: {loss_per_reject:.0f} руб.")
        self.logger.log(f"  Общие потери на логистике: {total_losses:,.0f} руб.")
        self.logger.log(f"  Упущенная прибыль (оценка): {lost_profit:,.0f} руб.")
        self.logger.log(f"  Суммарные потери: {total_losses + lost_profit:,.0f} руб.")
        
        return {
            'total_orders': total_orders,
            'rejected_orders': rejected_orders,
            'reject_rate': reject_rate,
            'avg_delivery_cost': self.avg_delivery_cost,
            'avg_return_cost': self.avg_return_cost,
            'loss_per_reject': loss_per_reject,
            'total_logistics_loss': total_losses,
            'lost_profit': lost_profit,
            'total_losses': total_losses + lost_profit
        }
    
    def _print_business_recommendations(self, df):
        self.logger.log("")
        self.logger.log("Рекомендации по улучшению процессов")
        
        recommendations = []
        
        if 'is_far_region' in df.columns:
            far_rate = df[df['is_far_region'] == 1]['buyout_flag'].mean()
            near_rate = df[df['is_far_region'] == 0]['buyout_flag'].mean()
            diff = near_rate - far_rate
            if diff > 0.03:
                recommendations.append(
                    f"Удалённые регионы: выкуп на {diff:.1%} ниже ({far_rate:.1%} против {near_rate:.1%}). "
                    f"Рекомендация: ввести частичную предоплату 30% или предлагать доставку до ПВЗ вместо курьера."
                )
        
        if 'disc_is_I' in df.columns:
            i_rate = df[df['disc_is_I'] == 1]['buyout_flag'].mean()
            other_rate = df[(df['disc_is_known'] == 1) & (df['disc_is_I'] == 0)]['buyout_flag'].mean()
            diff = other_rate - i_rate
            if diff > 0.03:
                recommendations.append(
                    f"Клиенты с DISC I: выкуп на {diff:.1%} ниже ({i_rate:.1%} против {other_rate:.1%}). "
                    f"Рекомендация: специальный скрипт для менеджеров с акцентом на выгоду и срочность."
                )
        
        if 'weekend_new_customer' in df.columns:
            mask = df['weekend_new_customer'] == 1
            if mask.sum() > 50:
                weekend_new_rate = df[mask]['buyout_flag'].mean()
                overall_rate = df['buyout_flag'].mean()
                diff = overall_rate - weekend_new_rate
                if diff > 0.03:
                    recommendations.append(
                        f"Новые клиенты в выходные: выкуп на {diff:.1%} ниже ({weekend_new_rate:.1%} против {overall_rate:.1%}). "
                        f"Рекомендация: отложенный обзвон в понедельник утром для подтверждения заказа."
                    )
        
        if 'assembly_speed' in df.columns:
            slow_rate = df[df['assembly_speed'] == 'slow']['buyout_flag'].mean() if 'slow' in df['assembly_speed'].values else None
            fast_rate = df[df['assembly_speed'] == 'fast']['buyout_flag'].mean() if 'fast' in df['assembly_speed'].values else None
            if slow_rate and fast_rate and (fast_rate - slow_rate) > 0.02:
                recommendations.append(
                    f"Медленная сборка (более 7 дней): снижает выкуп на {(fast_rate - slow_rate):.1%}. "
                    f"Рекомендация: оптимизировать процессы склада, установить KPI на сборку до 3 дней."
                )
        
        if 'disc_is_unknown' in df.columns:
            unknown_pct = df['disc_is_unknown'].mean()
            if unknown_pct > 0.3:
                recommendations.append(
                    f"DISC не заполнен у {unknown_pct:.0%} заказов. "
                    f"Рекомендация: обязать менеджеров заполнять DISC при первом контакте для улучшения качества прогноза."
                )
        
        if recommendations:
            for i, rec in enumerate(recommendations, 1):
                self.logger.log(f"  {i}. {rec}")
        else:
            self.logger.log("  Критических отклонений не найдено, все процессы в норме.")
    
    def analyze_data(self, csv_path):
        self.logger.log("")
        self.logger.log("Анализ данных")
        
        df = self._load_and_clean_data(csv_path)
        
        if self.validation_log:
            self.logger.log("")
            self.logger.log("Результаты фильтрации данных")
            log = self.validation_log
            self.logger.log(f"  Исходное количество записей: {log['initial_count']}")
            self.logger.log(f"  Удалено незавершённых сделок: {log['lifecycle_incomplete_removed']}")
            self.logger.log(f"  Удалено сделок с неизвестным исходом: {log['outcome_unknown_removed']}")
            self.logger.log(f"  Расхождений дат (2-7 дней): {log['timestamp_mismatches_warning']}")
            self.logger.log(f"  Расхождений дат (более 7 дней): {log['timestamp_mismatches_critical']}")
            self.logger.log(f"  Осталось записей для анализа: {log['final_count']}")
        
        if self.date_mismatches_warning or self.date_mismatches_critical:
            all_mismatches = self.date_mismatches_warning + self.date_mismatches_critical
            mismatches_df = pd.DataFrame(all_mismatches)
            mismatches_df.to_csv("date_mismatches.csv", index=False)
            self.logger.log(f"  Критические расхождения дат сохранены в файл date_mismatches.csv ({len(mismatches_df)} записей)")
        
        df = self._calculate_customer_history(df)
        df = self._create_features(df, stage='stage2')
        
        loss_stats = self._calculate_losses(df)
        
        self.logger.log("")
        self.logger.log("Корреляции признаков с выкупом (топ-15)")
        num_cols = df.select_dtypes(include=['int64', 'float64', 'int32', 'float32']).columns
        correlations = df[num_cols].corr()['buyout_flag'].sort_values(ascending=False)
        for idx, val in correlations.head(15).items():
            self.logger.log(f"  {idx}: {val:.6f}")
        
        self.logger.log("")
        self.logger.log("Доля выкупа")
        buyout_rate = df['buyout_flag'].mean()
        self.logger.log(f"  Общая доля выкупа: {buyout_rate:.2%}")
        self.logger.log(f"  Выкуплено: {df['buyout_flag'].sum():.0f} заказов")
        self.logger.log(f"  Не выкуплено: {(1 - df['buyout_flag']).sum():.0f} заказов")
        
        if 'sale_dayofweek' in df.columns:
            self.logger.log("")
            self.logger.log("Выкуп по дням недели")
            day_names = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота', 'Воскресенье']
            for day in range(7):
                day_data = df[df['sale_dayofweek'] == day]
                if len(day_data) > 0:
                    rate = day_data['buyout_flag'].mean()
                    self.logger.log(f"  {day_names[day]}: {rate:.2%} ({len(day_data)} заказов)")
        
        if 'customer_type' in df.columns:
            self.logger.log("")
            self.logger.log("Выкуп по типу клиента")
            for ctype in ['new', 'repeat', 'vip']:
                cdata = df[df['customer_type'] == ctype]
                if len(cdata) > 0:
                    self.logger.log(f"  {ctype}: {cdata['buyout_flag'].mean():.2%} ({len(cdata)} заказов)")
        
        if 'disc_profile' in df.columns:
            self.logger.log("")
            self.logger.log("Выкуп по DISC профилю")
            for profile in ['D', 'I', 'S', 'C', 'unknown']:
                pdata = df[df['disc_profile'] == profile]
                if len(pdata) > 0:
                    self.logger.log(f"  {profile}: {pdata['buyout_flag'].mean():.2%} ({len(pdata)} заказов)")
        
        if 'assembly_speed' in df.columns:
            self.logger.log("")
            self.logger.log("Выкуп по скорости сборки")
            for speed in df['assembly_speed'].dropna().unique():
                sdata = df[df['assembly_speed'] == speed]
                if len(sdata) > 0:
                    self.logger.log(f"  {speed}: {sdata['buyout_flag'].mean():.2%} ({len(sdata)} заказов)")
        
        if 'is_far_region' in df.columns:
            self.logger.log("")
            self.logger.log("Выкуп по удалённости региона")
            for is_far in [0, 1]:
                fdata = df[df['is_far_region'] == is_far]
                if len(fdata) > 0:
                    region_name = 'Удалённый' if is_far == 1 else 'Ближний'
                    self.logger.log(f"  {region_name}: {fdata['buyout_flag'].mean():.2%} ({len(fdata)} заказов)")
        
        self.logger.log("")
        self.logger.log("Детальный анализ по сегментам")
        
        if 'disc_is_known' in df.columns and 'customer_type' in df.columns:
            self.logger.log("  Выкуп по известности DISC и типу клиента:")
            for ctype in ['new', 'repeat', 'vip']:
                for known in [0, 1]:
                    mask = (df['customer_type'] == ctype) & (df['disc_is_known'] == known)
                    if mask.sum() > 0:
                        rate = df.loc[mask, 'buyout_flag'].mean()
                        known_str = 'известен' if known == 1 else 'неизвестен'
                        self.logger.log(f"    {ctype} / DISC {known_str}: {rate:.2%} ({mask.sum()} заказов)")
        
        if 'disc_is_known' in df.columns and 'lead_qualification' in df.columns:
            self.logger.log("  Выкуп по известности DISC и квалификации лида:")
            for qual in df['lead_qualification'].unique():
                for known in [0, 1]:
                    mask = (df['lead_qualification'] == qual) & (df['disc_is_known'] == known)
                    if mask.sum() > 20:
                        rate = df.loc[mask, 'buyout_flag'].mean()
                        known_str = 'известен' if known == 1 else 'неизвестен'
                        self.logger.log(f"    {qual} / DISC {known_str}: {rate:.2%} ({mask.sum()} заказов)")
        
        if 'tag_tilda' in df.columns and 'lead_utm_source' in df.columns:
            self.logger.log("  Выкуп для tilda-трафика по источникам (топ-5):")
            tilda_df = df[df['tag_tilda'] == 1]
            if len(tilda_df) > 0:
                source_stats = tilda_df.groupby('lead_utm_source').agg({
                    'buyout_flag': ['mean', 'count']
                }).reset_index()
                source_stats.columns = ['source', 'buyout_rate', 'count']
                source_stats = source_stats.sort_values('count', ascending=False).head(5)
                for _, row in source_stats.iterrows():
                    self.logger.log(f"    {row['source']}: {row['buyout_rate']:.2%} ({int(row['count'])} заказов)")
        
        if 'is_far_region' in df.columns and 'assembly_speed' in df.columns:
            self.logger.log("  Выкуп по удалённости и скорости сборки:")
            for is_far in [0, 1]:
                for speed in df['assembly_speed'].dropna().unique():
                    mask = (df['is_far_region'] == is_far) & (df['assembly_speed'] == speed)
                    if mask.sum() > 0:
                        rate = df.loc[mask, 'buyout_flag'].mean()
                        far_str = 'Удалённый' if is_far == 1 else 'Ближний'
                        self.logger.log(f"    {far_str} / {speed}: {rate:.2%} ({mask.sum()} заказов)")
        
        if 'sale_is_weekend' in df.columns and 'customer_type' in df.columns:
            self.logger.log("  Выкуп по выходным и типу клиента:")
            for ctype in ['new', 'repeat', 'vip']:
                for is_weekend in [0, 1]:
                    mask = (df['customer_type'] == ctype) & (df['sale_is_weekend'] == is_weekend)
                    if mask.sum() > 0:
                        rate = df.loc[mask, 'buyout_flag'].mean()
                        day_str = 'выходной' if is_weekend == 1 else 'будни'
                        self.logger.log(f"    {ctype} / {day_str}: {rate:.2%} ({mask.sum()} заказов)")
        
        if 'price_bucket' in df.columns and 'is_far_region' in df.columns:
            self.logger.log("  Выкуп по ценовому сегменту и удалённости:")
            for bucket in sorted(df['price_bucket'].dropna().unique()):
                for is_far in [0, 1]:
                    mask = (df['price_bucket'] == bucket) & (df['is_far_region'] == is_far)
                    if mask.sum() > 0:
                        rate = df.loc[mask, 'buyout_flag'].mean()
                        far_str = 'Удалённый' if is_far == 1 else 'Ближний'
                        self.logger.log(f"    Бакет {int(bucket)} / {far_str}: {rate:.2%} ({mask.sum()} заказов)")
        
        if 'product_type' in df.columns and 'is_far_region' in df.columns:
            self.logger.log("  Выкуп по типу товара и удалённости:")
            for ptype in df['product_type'].unique():
                for is_far in [0, 1]:
                    mask = (df['product_type'] == ptype) & (df['is_far_region'] == is_far)
                    if mask.sum() > 0:
                        rate = df.loc[mask, 'buyout_flag'].mean()
                        far_str = 'Удалённый' if is_far == 1 else 'Ближний'
                        self.logger.log(f"    {ptype} / {far_str}: {rate:.2%} ({mask.sum()} заказов)")
        
        self._print_business_recommendations(df)
        
        return df
    
    def fit(self, csv_path, target_col='buyout_flag', test_size=0.2):
        self.logger.log("")
        self.logger.log("Обучение модели")
        
        df = self._load_and_clean_data(csv_path)
        df = self._calculate_customer_history(df)
        
        self.logger.log("  Создание признаков...")
        df_stage1 = self._create_features(df, stage='stage1')
        df_stage2 = self._create_features(df, stage='stage2')
        
        y = df['buyout_flag']
        
        self.logger.log(f"  Доля выкупа в обучающей выборке: {y.mean():.2%}")
        
        self.logger.log("  Отбор признаков...")
        X1 = self._select_features(df_stage1, target_col, stage='stage1')
        self.feature_columns_stage1 = X1.columns.tolist()
        self.logger.log(f"    Stage 1: {len(self.feature_columns_stage1)} признаков")
        
        X2 = self._select_features(df_stage2, target_col, stage='stage2')
        self.feature_columns_stage2 = X2.columns.tolist()
        self.logger.log(f"    Stage 2: {len(self.feature_columns_stage2)} признаков")
        
        self._check_data_leakage(X1, y)
        
        self.cat_features_stage1 = X1.select_dtypes(include=['object', 'string', 'category']).columns.tolist()
        self.cat_features_stage2 = X2.select_dtypes(include=['object', 'string', 'category']).columns.tolist()
        self.logger.log(f"    Категориальных признаков Stage 1: {len(self.cat_features_stage1)}")
        self.logger.log(f"    Категориальных признаков Stage 2: {len(self.cat_features_stage2)}")
        
        X1_train, X1_test, X2_train, X2_test, y_train, y_test = train_test_split(
            X1, X2, y, test_size=test_size, random_state=42, stratify=y
        )
        
        n_class_0 = (y_train == 0).sum()
        n_class_1 = (y_train == 1).sum()
        scale_pos_weight = n_class_1 / n_class_0 if n_class_0 > 0 else 1.0
        
        self.logger.log(f"  Балансировка классов:")
        self.logger.log(f"    Класс 0 (не выкуп): {n_class_0}")
        self.logger.log(f"    Класс 1 (выкуп):    {n_class_1}")
        self.logger.log(f"    scale_pos_weight:   {scale_pos_weight:.2f}")
        
        self.logger.log("  Обучение Stage 1 (CatBoost)...")
        
        X1_train_fixed = X1_train.copy()
        X1_test_fixed = X1_test.copy()
        for col in self.cat_features_stage1:
            if col in X1_train_fixed.columns:
                if isinstance(X1_train_fixed[col].dtype, pd.CategoricalDtype):
                    X1_train_fixed[col] = X1_train_fixed[col].astype(str)
                    X1_test_fixed[col] = X1_test_fixed[col].astype(str)
                X1_train_fixed[col] = X1_train_fixed[col].fillna('MISSING')
                X1_test_fixed[col] = X1_test_fixed[col].fillna('MISSING')
        
        model1 = CatBoostClassifier(**self.model_params)
        model1.set_params(scale_pos_weight=scale_pos_weight)
        
        self.pipeline_stage1 = model1
        self.pipeline_stage1.fit(
            X1_train_fixed, y_train,
            cat_features=self.cat_features_stage1,
            eval_set=(X1_test_fixed, y_test),
            verbose=100
        )
        
        y1_pred = self.pipeline_stage1.predict(X1_test_fixed)
        y1_proba = self.pipeline_stage1.predict_proba(X1_test_fixed)[:, 1]
        self.metrics['stage1'] = {
            'f1_macro': f1_score(y_test, y1_pred, average='macro'),
            'roc_auc': roc_auc_score(y_test, y1_proba),
            'accuracy': (y1_pred == y_test).mean()
        }
        
        self.feature_importance['stage1'] = pd.DataFrame({
            'feature': self.feature_columns_stage1,
            'importance': model1.feature_importances_
        }).sort_values('importance', ascending=False)
        
        self.print_business_diagnostics(y_test, y1_proba, "Stage 1 (на момент заказа)")
        
        self.logger.log("  Обучение Stage 2 (CatBoost)...")
        
        X2_train_fixed = X2_train.copy()
        X2_test_fixed = X2_test.copy()
        for col in self.cat_features_stage2:
            if col in X2_train_fixed.columns:
                if isinstance(X2_train_fixed[col].dtype, pd.CategoricalDtype):
                    X2_train_fixed[col] = X2_train_fixed[col].astype(str)
                    X2_test_fixed[col] = X2_test_fixed[col].astype(str)
                X2_train_fixed[col] = X2_train_fixed[col].fillna('MISSING')
                X2_test_fixed[col] = X2_test_fixed[col].fillna('MISSING')
        
        model2 = CatBoostClassifier(**self.model_params)
        model2.set_params(scale_pos_weight=scale_pos_weight)
        
        self.pipeline_stage2 = model2
        self.pipeline_stage2.fit(
            X2_train_fixed, y_train,
            cat_features=self.cat_features_stage2,
            eval_set=(X2_test_fixed, y_test),
            verbose=100
        )
        
        y2_pred = self.pipeline_stage2.predict(X2_test_fixed)
        y2_proba = self.pipeline_stage2.predict_proba(X2_test_fixed)[:, 1]
        self.metrics['stage2'] = {
            'f1_macro': f1_score(y_test, y2_pred, average='macro'),
            'roc_auc': roc_auc_score(y_test, y2_proba),
            'accuracy': (y2_pred == y_test).mean()
        }
        
        self.feature_importance['stage2'] = pd.DataFrame({
            'feature': self.feature_columns_stage2,
            'importance': model2.feature_importances_
        }).sort_values('importance', ascending=False)
        
        self.print_business_diagnostics(y_test, y2_proba, "Stage 2 (после сборки)")
        
        self.is_fitted = True
        
        self.logger.log("")
        self.logger.log("Результаты обучения")
        self.logger.log(f"  Stage 1 (на момент заказа):")
        self.logger.log(f"    F1-macro: {self.metrics['stage1']['f1_macro']:.4f}")
        self.logger.log(f"    ROC-AUC:  {self.metrics['stage1']['roc_auc']:.4f}")
        self.logger.log(f"    Accuracy: {self.metrics['stage1']['accuracy']:.4f}")
        
        self.logger.log(f"  Stage 2 (после сборки):")
        self.logger.log(f"    F1-macro: {self.metrics['stage2']['f1_macro']:.4f}")
        self.logger.log(f"    ROC-AUC:  {self.metrics['stage2']['roc_auc']:.4f}")
        self.logger.log(f"    Accuracy: {self.metrics['stage2']['accuracy']:.4f}")
        
        improvement = self.metrics['stage2']['roc_auc'] - self.metrics['stage1']['roc_auc']
        self.logger.log(f"  Улучшение после учёта сборки: {improvement:+.4f} ROC-AUC")
        
        if self.feature_importance['stage1'] is not None:
            self.logger.log("")
            self.logger.log("  Топ-15 признаков Stage 1:")
            for _, row in self.feature_importance['stage1'].head(15).iterrows():
                self.logger.log(f"    {row['feature']}: {row['importance']:.6f}")
        
        if self.feature_importance['stage2'] is not None:
            self.logger.log("")
            self.logger.log("  Топ-15 признаков Stage 2:")
            for _, row in self.feature_importance['stage2'].head(15).iterrows():
                self.logger.log(f"    {row['feature']}: {row['importance']:.6f}")
        
        self.logger.log("")
        self.logger.log("  Обучение завершено успешно")
        
        return self
    
    def predict_stage1(self, data):
        if not self.is_fitted:
            raise RuntimeError("Модель не обучена.")
        data_clean = data.copy()
        data_clean = self._create_features(data_clean, stage='stage1')
        missing = set(self.feature_columns_stage1) - set(data_clean.columns)
        for col in missing:
            data_clean[col] = np.nan
        data_clean = data_clean[self.feature_columns_stage1]
        for col in self.cat_features_stage1:
            if col in data_clean.columns:
                if isinstance(data_clean[col].dtype, pd.CategoricalDtype):
                    data_clean[col] = data_clean[col].astype(str)
                data_clean[col] = data_clean[col].fillna('MISSING')
        proba = self.pipeline_stage1.predict_proba(data_clean)[:, 1]
        pred = (proba >= 0.5).astype(int)
        return {'prediction': pred.tolist(), 'probability': proba.tolist()}
    
    def predict_stage2(self, data):
        if not self.is_fitted:
            raise RuntimeError("Модель не обучена.")
        data_clean = data.copy()
        data_clean = self._create_features(data_clean, stage='stage2')
        missing = set(self.feature_columns_stage2) - set(data_clean.columns)
        for col in missing:
            data_clean[col] = np.nan
        data_clean = data_clean[self.feature_columns_stage2]
        for col in self.cat_features_stage2:
            if col in data_clean.columns:
                if isinstance(data_clean[col].dtype, pd.CategoricalDtype):
                    data_clean[col] = data_clean[col].astype(str)
                data_clean[col] = data_clean[col].fillna('MISSING')
        proba = self.pipeline_stage2.predict_proba(data_clean)[:, 1]
        pred = (proba >= 0.5).astype(int)
        return {'prediction': pred.tolist(), 'probability': proba.tolist()}
    
    def predict_single_stage1(self, data_dict):
        df = pd.DataFrame([data_dict])
        df_features = self._create_features(df, stage='stage1')
        
        result = self.predict_stage1(df)
        proba = result['probability'][0]
        
        features_for_rec = {
            'is_new_customer': df_features['is_new_customer'].iloc[0] if 'is_new_customer' in df_features.columns else 0,
            'sale_is_weekend': df_features['sale_is_weekend'].iloc[0] if 'sale_is_weekend' in df_features.columns else 0,
            'is_far_region': df_features['is_far_region'].iloc[0] if 'is_far_region' in df_features.columns else 0,
            'disc_profile': df_features['disc_profile'].iloc[0] if 'disc_profile' in df_features.columns else 'unknown',
            'assembly_speed': None,
            'delivery_method': data_dict.get('lead_Служба доставки', '') or data_dict.get('lead_Метод доставки', '')
        }
        
        rec = self._generate_recommendation(proba, features_for_rec, stage='stage1')
        
        return {
            'prediction': result['prediction'][0],
            'probability': proba,
            'risk_group': rec['risk_group'],
            'risk_factors': rec['risk_factors'],
            'recommendation': rec['recommendation']
        }
    
    def predict_single_stage2(self, data_dict):
        df = pd.DataFrame([data_dict])
        df_features = self._create_features(df, stage='stage2')
        
        result = self.predict_stage2(df)
        proba = result['probability'][0]
        
        features_for_rec = {
            'is_new_customer': df_features['is_new_customer'].iloc[0] if 'is_new_customer' in df_features.columns else 0,
            'sale_is_weekend': df_features['sale_is_weekend'].iloc[0] if 'sale_is_weekend' in df_features.columns else 0,
            'is_far_region': df_features['is_far_region'].iloc[0] if 'is_far_region' in df_features.columns else 0,
            'disc_profile': df_features['disc_profile'].iloc[0] if 'disc_profile' in df_features.columns else 'unknown',
            'assembly_speed': df_features['assembly_speed'].iloc[0] if 'assembly_speed' in df_features.columns else None,
            'delivery_method': data_dict.get('lead_Служба доставки', '') or data_dict.get('lead_Метод доставки', '')
        }
        
        rec = self._generate_recommendation(proba, features_for_rec, stage='stage2')
        
        return {
            'prediction': result['prediction'][0],
            'probability': proba,
            'risk_group': rec['risk_group'],
            'risk_factors': rec['risk_factors'],
            'recommendation': rec['recommendation']
        }
    
    def save(self, path):
        with open(path, 'wb') as f:
            pickle.dump({
                'pipeline_stage1': self.pipeline_stage1,
                'pipeline_stage2': self.pipeline_stage2,
                'feature_columns_stage1': self.feature_columns_stage1,
                'feature_columns_stage2': self.feature_columns_stage2,
                'cat_features_stage1': self.cat_features_stage1,
                'cat_features_stage2': self.cat_features_stage2,
                'metrics': self.metrics,
                'feature_importance': self.feature_importance,
                'validation_log': self.validation_log,
                'is_fitted': self.is_fitted,
                'model_params': self.model_params,
                '_median_price': getattr(self, '_median_price', 0),
                'avg_delivery_cost': self.avg_delivery_cost,
                'avg_return_cost': self.avg_return_cost,
                'avg_order_value': self.avg_order_value
            }, f)
        self.logger.log(f"Модель сохранена в {path}")
    
    def load(self, path):
        with open(path, 'rb') as f:
            data = pickle.load(f)
        self.pipeline_stage1 = data['pipeline_stage1']
        self.pipeline_stage2 = data['pipeline_stage2']
        self.feature_columns_stage1 = data['feature_columns_stage1']
        self.feature_columns_stage2 = data['feature_columns_stage2']
        self.cat_features_stage1 = data.get('cat_features_stage1', [])
        self.cat_features_stage2 = data.get('cat_features_stage2', [])
        self.metrics = data['metrics']
        self.feature_importance = data.get('feature_importance', {'stage1': None, 'stage2': None})
        self.validation_log = data.get('validation_log', {})
        self.is_fitted = data['is_fitted']
        self.model_params = data.get('model_params', self.model_params)
        self._median_price = data.get('_median_price', 0)
        self.avg_delivery_cost = data.get('avg_delivery_cost')
        self.avg_return_cost = data.get('avg_return_cost')
        self.avg_order_value = data.get('avg_order_value')
        self.logger.log(f"Модель загружена из {path}")
        return self


