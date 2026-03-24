package de.bund.zrb.infrastructure;

import de.bund.zrb.domain.DiagramDocument;
import de.bund.zrb.domain.DocumentRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Infrastructure adapter for loading/saving diagram documents to the file system.
 * Files are stored as plain Mermaid text (UTF-8, .mmd extension by convention).
 */
public class FileDocumentRepository implements DocumentRepository {

    @Override
    public void save(DiagramDocument document, Path path) throws IOException {
        Files.writeString(path, document.getSource(), StandardCharsets.UTF_8);
        document.setFilePath(path);
        document.markSaved();
    }

    @Override
    public DiagramDocument load(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        DiagramDocument doc = new DiagramDocument(content);
        doc.setFilePath(path);
        doc.markSaved();
        return doc;
    }
}
