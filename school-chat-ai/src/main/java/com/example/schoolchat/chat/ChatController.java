package com.example.schoolchat.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final int MAX_LEN = 300;

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    public record ChatRequest(String message) {
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest req) {
        String msg = req == null || req.message() == null ? "" : req.message().trim();
        if (msg.isEmpty()) {
            return ResponseEntity.badRequest().body(ChatResponse.error("Kuch likho to sahi."));
        }
        if (msg.length() > MAX_LEN) {
            return ResponseEntity.badRequest()
                    .body(ChatResponse.error("Sawal thoda chhota likho (" + MAX_LEN + " characters tak)."));
        }
        return ResponseEntity.ok(chatService.ask(msg));
    }
}
