package com.trofimenko.petai.controller;

import com.trofimenko.petai.model.ChatRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PetAiController {

    @Autowired
    private ChatClient chatClient;

    @PostMapping
    public String chat(@RequestBody ChatRequestDto request) {
        log.info("Chat request: {}", request);

        return chatClient.prompt()
                .system(request.getSystemPrompt())
                .user(request.getUserPrompt())
                .call()
                .content();
    }
}
