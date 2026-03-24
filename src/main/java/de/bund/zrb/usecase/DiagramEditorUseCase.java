package de.bund.zrb.usecase;

import de.bund.zrb.domain.DiagramDocument;
import de.bund.zrb.domain.DiagramType;
import de.bund.zrb.domain.ValidationError;

import java.util.List;

/**
 * Application-level use cases for editing a diagram document.
 * Orchestrates domain model, snippet generation and validation.
 */
public class DiagramEditorUseCase {

    private final DiagramDocument document;
    private final MermaidSnippetFactory snippetFactory;
    private final DiagramValidationService validationService;

    public DiagramEditorUseCase(DiagramDocument document,
                                MermaidSnippetFactory snippetFactory,
                                DiagramValidationService validationService) {
        this.document = document;
        this.snippetFactory = snippetFactory;
        this.validationService = validationService;
    }

    // ────────────────────────────────────────────────────────────
    //  Basic source operations
    // ────────────────────────────────────────────────────────────

    public void updateSource(String newSource) {
        document.updateSource(newSource);
    }

    public void undo() {
        document.undo();
    }

    public void redo() {
        document.redo();
    }

    // ────────────────────────────────────────────────────────────
    //  Structured editing operations
    // ────────────────────────────────────────────────────────────

    /**
     * Append a new node to the current source.
     *
     * @param id    node identifier (e.g. "A", "MyClass")
     * @param label display label
     */
    public void addNode(String id, String label) {
        DiagramType type = document.getDiagramType();
        String snippet = snippetFactory.addNodeSnippet(type, id, label);
        appendToSource(snippet);
    }

    /**
     * Append a new edge/relation to the current source.
     */
    public void addEdge(String fromId, String toId, String label) {
        DiagramType type = document.getDiagramType();
        String snippet = snippetFactory.addEdgeSnippet(type, fromId, toId, label);
        appendToSource(snippet);
    }

    /**
     * Rename an element by simple text replacement.
     *
     * @param oldName current name/id in the source
     * @param newName replacement name/id
     */
    public void renameElement(String oldName, String newName) {
        if (oldName == null || newName == null || oldName.equals(newName)) return;
        String current = document.getSource();
        // Replace all whole-word occurrences
        String updated = current.replaceAll("\\b" + java.util.regex.Pattern.quote(oldName) + "\\b", newName);
        if (!updated.equals(current)) {
            document.updateSource(updated);
        }
    }

    /**
     * Delete a line from the source by line number (1-based).
     */
    public void deleteLine(int lineNumber) {
        String[] lines = document.getSource().split("\\n", -1);
        if (lineNumber < 1 || lineNumber > lines.length) return;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i + 1 == lineNumber) continue;
            if (!sb.isEmpty()) sb.append("\n");
            sb.append(lines[i]);
        }
        document.updateSource(sb.toString());
    }

    // ────────────────────────────────────────────────────────────
    //  Validation
    // ────────────────────────────────────────────────────────────

    public List<ValidationError> validate() {
        return validationService.validate(document.getSource());
    }

    // ────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────

    private void appendToSource(String snippet) {
        String current = document.getSource();
        String separator = current.endsWith("\n") ? "" : "\n";
        document.updateSource(current + separator + snippet);
    }
}
