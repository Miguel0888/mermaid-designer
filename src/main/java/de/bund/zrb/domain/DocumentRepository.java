package de.bund.zrb.domain;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Port interface for loading and saving diagram documents.
 */
public interface DocumentRepository {

    /**
     * Save the document's Mermaid source to the given path.
     */
    void save(DiagramDocument document, Path path) throws IOException;

    /**
     * Load a diagram document from a file.
     */
    DiagramDocument load(Path path) throws IOException;
}
