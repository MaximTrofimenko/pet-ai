package com.trofimenko.petai.services;

import lombok.SneakyThrows;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DocumentReaderFactory {

    @SneakyThrows
    public DocumentReader getReader(Resource resource) {
        String filename = Objects.requireNonNull(resource.getFilename()).toLowerCase();

        if (filename.endsWith(".pdf")) {
            return new PagePdfDocumentReader(resource);
        } else if (filename.endsWith(".txt")) {
            return new TextReader(resource);
        }
        // Добавить другие форматы по необходимости
        throw new IllegalArgumentException("Unsupported file format: " + filename);
    }
}