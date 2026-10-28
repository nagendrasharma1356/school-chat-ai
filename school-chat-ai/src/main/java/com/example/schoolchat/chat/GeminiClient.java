package com.example.schoolchat.chat;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Google AI Studio (Gemini API) ko call karta hai. Key sirf backend mein rehti hai. */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final RestClient client;
    private final GeminiProperties props;

    public GeminiClient(GeminiProperties props) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(props.timeoutSeconds()));
        this.client = RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /** Gemini ko sirf JSON return karne par majboor karta hai (responseMimeType). */
    public String generateJson(String systemPrompt, String userText) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new GeminiException("GEMINI_API_KEY set nahi hai. AI Studio se key lekar environment variable mein daalo.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
        body.put("contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userText)))));
        body.put("generationConfig", Map.of(
                "temperature", props.temperature(),
                "maxOutputTokens", props.maxOutputTokens(),
                "responseMimeType", "application/json"));

        JsonNode res;
        try {
            res = client.post()
                    .uri("/models/{model}:generateContent", props.model())
                    .header("x-goog-api-key", props.apiKey())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            log.warn("Gemini API error {}: {}", status, e.getResponseBodyAsString());
            throw new GeminiException(switch (status) {
                case 429 -> "Gemini ki free limit abhi khatam ho gayi. Thodi der baad try karo.";
                case 400, 401, 403 -> "Gemini API key galat ya band lag rahi hai. AI Studio mein key check karo.";
                case 404 -> "Gemini model nahi mila. gemini.model ka naam check karo.";
                default -> "Gemini abhi jawab nahi de raha (error " + status + "). Baad mein try karo.";
            });
        } catch (ResourceAccessException e) {
            log.warn("Gemini tak pahunch nahi paya: {}", e.getMessage());
            throw new GeminiException("Gemini tak pahunch nahi paya (internet ya timeout). Dobara try karo.");
        }

        JsonNode text = res == null ? null
                : res.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (text == null || text.isMissingNode() || text.asText().isBlank()) {
            log.warn("Gemini se text nahi aaya: {}", res);
            throw new GeminiException("Gemini ne is sawal ka jawab nahi diya. Sawal alag tareeke se likh kar dekho.");
        }
        return text.asText();
    }
}
