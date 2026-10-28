package com.example.schoolchat.chat;

/** Message seedha user ko dikhane layak (Hinglish) hota hai. */
public class GeminiException extends RuntimeException {
    public GeminiException(String userMessage) {
        super(userMessage);
    }
}
