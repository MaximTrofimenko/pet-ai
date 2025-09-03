package com.trofimenko.petai.model;

import lombok.Data;

@Data
public class ChatRequestDto {
    private String systemPrompt;
    private String userPrompt;
}
