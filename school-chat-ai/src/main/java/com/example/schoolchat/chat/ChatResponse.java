package com.example.schoolchat.chat;

import java.util.List;
import java.util.Map;

/**
 * UI ko jo milta hai:
 *  answer  - chat mein dikhane wala text
 *  tool    - kaunsa tool chala ("none" = data wala sawal nahi, "error" = kuch fail hua)
 *  columns - table ke headers (order mein)
 *  rows    - table rows; har row ka key = column ka naam
 */
public record ChatResponse(String answer, String tool, List<String> columns, List<Map<String, Object>> rows) {

    public static ChatResponse text(String answer) {
        return new ChatResponse(answer, "none", List.of(), List.of());
    }

    public static ChatResponse error(String answer) {
        return new ChatResponse(answer, "error", List.of(), List.of());
    }
}
