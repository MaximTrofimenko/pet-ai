package com.trofimenko.petai.services;

import com.trofimenko.petai.model.LoadedDocument;
import com.trofimenko.petai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLoaderService implements CommandLineRunner {

    private final DocumentRepository documentRepository;
    private final ResourcePatternResolver resolver;
    private final VectorStore vectorStore;
    private final DocumentReaderFactory readerFactory;

    @Override
    public void run(String... args) {
        loadDocuments();
    }

    @SneakyThrows
    public void loadDocuments() {
        List<Resource> resources = Arrays.stream(resolver.getResources("classpath:/documentstorage/**/*.*"))
                .filter(resource -> {
                    String filename = Objects.requireNonNull(resource.getFilename()).toLowerCase();
                    return filename.endsWith(".pdf") || filename.endsWith(".txt");
                })
                .toList();

        resources.stream()
                .map(resource -> Pair.of(resource, calcContentHash(resource)))
                .filter(pair ->
                        !documentRepository.existsByFilenameAndContentHash(pair.getFirst().getFilename(), pair.getSecond()))
                .forEach(pair -> {
                    log.info("Найден новый файл для сохранения {}", pair.getFirst().getFilename());

                    Resource resource = pair.getFirst();

                    DocumentReader reader = readerFactory.getReader(resource);
                    List<Document> documents = reader.get();

                    TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                            .withChunkSize(300)
                            .build();

                    List<Document> chunks = textSplitter.apply(documents);
                    vectorStore.accept(chunks);

                    LoadedDocument loadedDocument = LoadedDocument.builder()
                            .documentType(getFileExtension(resource.getFilename()))
                            .chunkCount(chunks.size())
                            .filename(resource.getFilename())
                            .contentHash(pair.getSecond())
                            .build();
                    documentRepository.save(loadedDocument);
                });
    }

    @SneakyThrows
    private String calcContentHash(Resource resource) {
        return DigestUtils.md5DigestAsHex(resource.getInputStream());
    }

    private String getFileExtension(String filename) {
        return filename != null ? filename.substring(filename.lastIndexOf(".") + 1) : "unknown";
    }
}
