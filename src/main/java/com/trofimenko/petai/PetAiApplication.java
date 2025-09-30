package com.trofimenko.petai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
        return builder.defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        getRagAdviser(),
                        SimpleLoggerAdvisor.builder().build()
                )
                .defaultOptions(
                        OllamaOptions.builder()
                                .temperature(0.3)
                                .topP(0.7)
                                .topK(20)
                                .repeatPenalty(1.1)
                                .build()
                )
                .build();
    }

    private Advisor getRagAdviser() {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .searchRequest(
                        SearchRequest.builder()
                                .topK(4)//сколько взять документов из рага (4 чанка)
                                .similarityThreshold(0.65)//по дефолту 0.0, если 0.9 то документ похож на 90 процентов
                                .build())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(PetAiApplication.class, args);
    }
}
