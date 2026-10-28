package com.example.schoolchat.chat;

import java.util.List;
import java.util.Map;

public record ToolResult(String answer, List<String> columns, List<Map<String, Object>> rows) {
}
