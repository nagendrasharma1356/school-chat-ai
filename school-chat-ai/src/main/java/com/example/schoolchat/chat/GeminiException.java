package com.example.schoolchat.chat;

public class GeminiException extends RuntimeException {
    public GeminiException(String userMessage) {
        super(userMessage);
    }
}
