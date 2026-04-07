import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.File;
import java.util.*;

public class OrderProcessorClient {
    
    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper mapper;
    
    public OrderProcessorClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClients.createDefault();
        this.mapper = new ObjectMapper();
    }
    
    /**
     * Проверка статуса сервера
     */
    public JsonNode getStatus() throws Exception {
        HttpGet request = new HttpGet(baseUrl + "/status");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Загрузка датасета
     * @param filePath путь к файлу (CSV, Excel, JSON)
     */
    public JsonNode loadDataset(String filePath) throws Exception {
        HttpPost request = new HttpPost(baseUrl + "/load");
        
        File file = new File(filePath);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file);
        request.setEntity(builder.build());
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Получение списка всех полей
     */
    public JsonNode getFields() throws Exception {
        HttpGet request = new HttpGet(baseUrl + "/fields");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Получение статистики по полю
     */
    public JsonNode getFieldStats(String fieldName) throws Exception {
        HttpGet request = new HttpGet(baseUrl + "/stats/" + fieldName);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Получение списка городов
     */
    public JsonNode getCities() throws Exception {
        HttpGet request = new HttpGet(baseUrl + "/cities");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Получение списка служб доставки
     */
    public JsonNode getDeliveryServices() throws Exception {
        HttpGet request = new HttpGet(baseUrl + "/delivery_services");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Запрос данных с фильтрацией и сортировкой
     */
    public JsonNode queryData(QueryRequest queryRequest) throws Exception {
        HttpPost request = new HttpPost(baseUrl + "/query");
        String jsonBody = mapper.writeValueAsString(queryRequest);
        request.setEntity(new StringEntity(jsonBody));
        request.setHeader("Content-Type", "application/json");
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Сортировка датасета
     */
    public JsonNode sortDataset(String field, String direction) throws Exception {
        HttpPost request = new HttpPost(baseUrl + "/sort");
        Map<String, String> body = new HashMap<>();
        body.put("field", field);
        body.put("direction", direction);
        request.setEntity(new StringEntity(mapper.writeValueAsString(body)));
        request.setHeader("Content-Type", "application/json");
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Фильтрация датасета
     */
    public JsonNode filterDataset(List<Filter> filters, boolean replace) throws Exception {
        HttpPost request = new HttpPost(baseUrl + "/filter");
        Map<String, Object> body = new HashMap<>();
        body.put("filters", filters);
        body.put("replace", replace);
        request.setEntity(new StringEntity(mapper.writeValueAsString(body)));
        request.setHeader("Content-Type", "application/json");
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    /**
     * Экспорт датасета
     */
    public JsonNode exportDataset(String format) throws Exception {
        HttpGet request = new HttpGet(baseUrl + "/export?format=" + format);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            return mapper.readTree(json);
        }
    }
    
    // ============================================
    // ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ
    // ============================================
    
    public static class Filter {
        public String field;
        public String operator;  // eq, ne, gt, lt, gte, lte, contains
        public Object value;
        
        public Filter(String field, String operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
    }
    
    public static class Sort {
        public String field;
        public String direction;  // asc, desc
        
        public Sort(String field, String direction) {
            this.field = field;
            this.direction = direction;
        }
    }
    
    public static class QueryRequest {
        public List<Filter> filters;
        public List<Sort> sort;
        public int limit = 100;
        public int offset = 0;
        public List<String> fields;
        
        public QueryRequest() {
            this.filters = new ArrayList<>();
            this.sort = new ArrayList<>();
            this.fields = new ArrayList<>();
        }
    }
    
    // ============================================
    // ПРИМЕР ИСПОЛЬЗОВАНИЯ
    // ============================================
    
    public static void main(String[] args) throws Exception {
        OrderProcessorClient client = new OrderProcessorClient("http://localhost:8000");
        
        // 1. Проверка статуса
        System.out.println("=== СТАТУС ===");
        System.out.println(client.getStatus());
        
        // 2. Загрузка датасета
        System.out.println("\n=== ЗАГРУЗКА ===");
        System.out.println(client.loadDataset("/path/to/your/dataset.csv"));
        
        // 3. Получение списка полей
        System.out.println("\n=== ПОЛЯ ===");
        System.out.println(client.getFields());
        
        // 4. Получение городов
        System.out.println("\n=== ГОРОДА ===");
        System.out.println(client.getCities());
        
        // 5. Запрос с фильтрацией
        System.out.println("\n=== ЗАПРОС ===");
        QueryRequest query = new QueryRequest();
        query.filters.add(new Filter("lead_price", "gt", 10000));
        query.sort.add(new Sort("lead_price", "desc"));
        query.limit = 50;
        query.fields = Arrays.asList("lead_id", "lead_price", "contact_Город", "buyout_flag");
        
        System.out.println(client.queryData(query));
        
        // 6. Сортировка
        System.out.println("\n=== СОРТИРОВКА ===");
        System.out.println(client.sortDataset("lead_price", "desc"));
        
        // 7. Фильтрация
        System.out.println("\n=== ФИЛЬТРАЦИЯ ===");
        List<Filter> filters = Arrays.asList(
            new Filter("lead_price", "gt", 5000)
        );
        System.out.println(client.filterDataset(filters, true));
        
        // 8. Экспорт
        System.out.println("\n=== ЭКСПОРТ ===");
        System.out.println(client.exportDataset("json"));
    }
}