import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final RateLimiter rateLimiter;


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        long millis = timeUnit.toMillis(1) / 1000;
        this.rateLimiter = RateLimiter.create(requestLimit / millis);
    }

    public void submit(Document document, String signature) {
        rateLimiter.acquire();
        try {
            HttpRequest httpRequest = createHttpRequest(document, signature);
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignore) {
        }
    }

    private HttpRequest createHttpRequest(Document document, String signature) throws URISyntaxException, JsonProcessingException {
        String body = objectMapper.writeValueAsString(document);
        return HttpRequest.newBuilder()
                .uri(new URI("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    @Data
    public class Document {
        private Description description;
        private String docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private Date productionDate;
        private String productionType;
        private List<Product> products;
        private Date regDate;
        private String regNumber;

        @Data
        public static class Description {
            private String participantInn;

        }

        @Data
        public class Product {
            private String certificateDocument;
            private Date certificateDocumentDate;
            private String certificateDocumentNumber;
            private String ownerInn;
            private String producerInn;
            private Date productionDate;
            private String tnvedCode;
            private String uitCode;
            private String uituCode;

        }
    }
}

