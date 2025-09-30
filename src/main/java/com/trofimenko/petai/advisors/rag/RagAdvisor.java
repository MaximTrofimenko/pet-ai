package com.trofimenko.petai.advisors.rag;

import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.trofimenko.petai.advisors.expansion.ExpansionQueryAdvisor.ENRICHED_QUESTION;

@Builder
public class RagAdvisor implements BaseAdvisor {

    @Builder.Default
    private static final PromptTemplate template = PromptTemplate.builder().template("""
            CONTEXT: {context}
            Question: {question}
            """).build();

    private VectorStore vectorStore;

    //Spring AI всегда работает с косинусным сходством (cosine similarity)?
    @Builder.Default
    private SearchRequest searchRequest = SearchRequest.builder()
            .topK(4)//количество наиболее релевантных (ближайших по векторному сходству) результатов, которые нужно вернуть.
            //Это просто LIMIT в SQL для pgVector
            .similarityThreshold(0.62)//Метрика сходства: обычно это косинусное сходство (cosine similarity)
            // 0.0 — ортогональные (независимые) векторы,
            // 1.0 — полное совпадение,
            .build();

    @Getter
    private final int order;

    public static RagAdvisorBuilder build(VectorStore vectorStore) {
        return new RagAdvisorBuilder().vectorStore(vectorStore);
    }

    //тут мы перехватываем запрос от юзера
    //ходим в векторную базу
    //и обогащаем контекст тем что нашли в бд
    //получается запрос в ллм с огромным найденным контекстом
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String originalUserQuestion = chatClientRequest.prompt().getUserMessage().getText();
        String queryToRag = chatClientRequest.context().getOrDefault(ENRICHED_QUESTION, originalUserQuestion).toString();

        //в запросе к векторной БД специально запрашиваем документов больше в два раза чтобы в rerankEngine выбрать наиболее релевантные
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.from(searchRequest)
                        .query(queryToRag)
                        .topK(searchRequest.getTopK() * 2)
                        .build()
        );

        if (documents == null || documents.isEmpty()) {
            return chatClientRequest.mutate().context("CONTEXT", "ни один документ не обнаружен").build();
        }

        BM25RerankEngine rerankEngine = BM25RerankEngine.builder().build();

        //дополнительный отбор документов
        documents = rerankEngine.rerank(documents, queryToRag, searchRequest.getTopK());

        String llmContext = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String finalUserPrompt = template.render(
                Map.of("context", llmContext, "question", originalUserQuestion)
        );


        //новый измененный промт в котором есть доки из хранилища
        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(finalUserPrompt)).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}