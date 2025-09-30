package com.trofimenko.petai.controller;

import com.trofimenko.petai.model.ChatRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PetAiController {

    private final ChatClient chatClient;

    @PostMapping
    public String chat(@RequestBody ChatRequestDto request) {
        log.info("Chat request: {}", request);

        return chatClient.prompt()
                .user(request.getUserPrompt())
                .call()
                .content();
    }
}
