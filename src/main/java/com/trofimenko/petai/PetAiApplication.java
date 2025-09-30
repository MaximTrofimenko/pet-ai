package com.trofimenko.petai;

import com.trofimenko.petai.advisors.expansion.ExpansionQueryAdvisor;
import com.trofimenko.petai.advisors.rag.RagAdvisor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class PetAiApplication {

    private static final PromptTemplate SYSTEM_PROMPT = new PromptTemplate(
            """
                    Отвечай от первого лица, кратко и по делу.
                    
                    Вопрос может быть о СЛЕДСТВИИ факта из Context.
                    ВСЕГДА связывай: факт Context → вопрос.
                    
                    Нет связи, даже косвенной = "Упоминания об этом отсутствуют".
                    Есть связь = отвечай.
                    """
    );

    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        ExpansionQueryAdvisor.builder(chatModel).order(0).build(),
                        SimpleLoggerAdvisor.builder().order(1).build(),
                        RagAdvisor.build(vectorStore).order(2).build(),
                        SimpleLoggerAdvisor.builder().order(3).build()
                )
                .defaultOptions(
                        OllamaOptions.builder()
                                .temperature(0.3)
                                .topP(0.7)
                                .topK(20)
                                .repeatPenalty(1.1)
                                .build()
                )
                .defaultSystem(SYSTEM_PROMPT.render())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(PetAiApplication.class, args);
    }
}
