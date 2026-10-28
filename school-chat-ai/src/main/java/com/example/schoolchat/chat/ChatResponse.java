package com.example.schoolchat.chat;

import java.util.List;
import java.util.Map;

public record ChatResponse(String answer, String tool, List<String> columns, List<Map<String, Object>> rows) {

    public static ChatResponse text(String answer) {
        return new ChatResponse(answer, "none", List.of(), List.of());
    }

    public static ChatResponse error(String answer) {
        return new ChatResponse(answer, "error", List.of(), List.of());
    }
}
