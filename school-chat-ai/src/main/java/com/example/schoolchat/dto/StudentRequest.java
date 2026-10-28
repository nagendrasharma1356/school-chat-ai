package com.example.schoolchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentRequest(
        @NotBlank String name,
        Integer rollNo,
        String gender,
        String guardianName,
        String phone,
        String status,
        @NotNull Long classId) {
}
