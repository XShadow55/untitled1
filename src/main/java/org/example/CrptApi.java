package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrptApi {
    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger requestCount;
    private final int requestLimit;


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.requestCount = new AtomicInteger(0);
        this.requestLimit = requestLimit;


        scheduler.scheduleAtFixedRate(() -> requestCount.set(0), timeUnit.toSeconds(1), timeUnit.toSeconds(1), TimeUnit.SECONDS);
    }

    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        synchronized (requestCount) {
            while (requestCount.get() >= requestLimit) {
                requestCount.wait();
            }
            requestCount.incrementAndGet();
        }

        String requestBody = objectMapper.writeValueAsString(new CreateDocumentRequest(document, signature));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API request failed with status code " + response.statusCode());
        }
    }

    public static class Document {
        public String description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;
    }

    public static class Product {
        public String certificate_document;
        public String certificate_document_date;
        public String certificate_document_number;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String tnved_code;
        public String uit_code;
        public String uitu_code;
    }

    private static class CreateDocumentRequest {
        public Document document;
        public String signature;

        public CreateDocumentRequest(Document document, String signature) {
            this.document = document;
            this.signature = signature;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Пример использования
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 10);
        Document doc = new Document();
        // заполняем поля объекта doc
        api.createDocument(doc, "подпись");
    }
}