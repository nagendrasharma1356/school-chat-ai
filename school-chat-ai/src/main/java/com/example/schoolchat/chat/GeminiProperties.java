package com.example.schoolchat.chat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        @DefaultValue("https://generativelanguage.googleapis.com/v1beta") String baseUrl,
        @DefaultValue("gemini-2.5-flash-lite") String model,
        @DefaultValue("0.1") double temperature,
        @DefaultValue("512") int maxOutputTokens,
        @DefaultValue("30") int timeoutSeconds) {
}
