package com.example.schoolchat.dto;

import jakarta.validation.constraints.NotBlank;

public record ClassRequest(@NotBlank String name, @NotBlank String section, Long classTeacherId) {
}
