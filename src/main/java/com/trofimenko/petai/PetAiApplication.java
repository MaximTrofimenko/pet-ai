package com.trofimenko.petai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

@SpringBootApplication
@RequiredArgsConstructor
public class PetAiApplication {

    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate("""
    {query}
    
    Контекст:
    ---------------------
    {question_answer_context}
    ---------------------
    
    Отвечай только на основе контекста выше. Если информации нет в контексте, сообщи, что не можешь ответить.
    """);

    private final VectorStore vectorStore;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(getRagAdviser())
                .build();
    }

    private Advisor getRagAdviser() {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .searchRequest(SearchRequest.builder().topK(4).build())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(PetAiApplication.class, args);
    }
}
