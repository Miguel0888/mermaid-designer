package de.bund.zrb.domain;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Core domain model for one Mermaid diagram document.
 * Holds the source text, detected diagram type, file path, modification state
 * and a snapshot-based undo/redo stack.
 */
public class DiagramDocument {

    private static final int MAX_UNDO = 200;

    private String source;
    private DiagramType diagramType;
    private Path filePath;          // null = unsaved
    private boolean modified;

    private final Deque<String> undoStack = new ArrayDeque<>();
    private final Deque<String> redoStack = new ArrayDeque<>();

    private final List<Consumer<DiagramDocument>> changeListeners = new CopyOnWriteArrayList<>();

    // ────────────────────────────────────────────────────────────
    //  Construction
    // ────────────────────────────────────────────────────────────

    public DiagramDocument(String initialSource) {
        this.source = initialSource != null ? initialSource : "";
        this.diagramType = DiagramType.detect(this.source);
        this.modified = false;
    }

    public DiagramDocument() {
        this("");
    }

    // ────────────────────────────────────────────────────────────
    //  Source management
    // ────────────────────────────────────────────────────────────

    public String getSource() {
        return source;
    }

    /**
     * Update the source text. Pushes the previous state onto the undo stack,
     * clears the redo stack, re-detects the diagram type and notifies listeners.
     */
    public void updateSource(String newSource) {
        if (newSource == null) newSource = "";
        if (newSource.equals(this.source)) return;

        pushUndo(this.source);
        redoStack.clear();

        this.source = newSource;
        this.diagramType = DiagramType.detect(newSource);
        this.modified = true;
        fireChanged();
    }

    // ────────────────────────────────────────────────────────────
    //  Undo / Redo
    // ────────────────────────────────────────────────────────────

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void undo() {
        if (!canUndo()) return;
        redoStack.push(this.source);
        this.source = undoStack.pop();
        this.diagramType = DiagramType.detect(this.source);
        this.modified = true;
        fireChanged();
    }

    public void redo() {
        if (!canRedo()) return;
        undoStack.push(this.source);
        this.source = redoStack.pop();
        this.diagramType = DiagramType.detect(this.source);
        this.modified = true;
        fireChanged();
    }

    private void pushUndo(String state) {
        undoStack.push(state);
        while (undoStack.size() > MAX_UNDO) {
            // Remove oldest entry from the bottom
            ((ArrayDeque<String>) undoStack).removeLast();
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Metadata
    // ────────────────────────────────────────────────────────────

    public DiagramType getDiagramType() { return diagramType; }

    public Path getFilePath() { return filePath; }
    public void setFilePath(Path filePath) { this.filePath = filePath; }

    public boolean isModified() { return modified; }
    public void markSaved() { this.modified = false; fireChanged(); }

    public String getTitle() {
        String name = filePath != null ? filePath.getFileName().toString() : "Unbenannt";
        return modified ? name + " *" : name;
    }

    // ────────────────────────────────────────────────────────────
    //  Change listeners
    // ────────────────────────────────────────────────────────────

    public void addChangeListener(Consumer<DiagramDocument> listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(Consumer<DiagramDocument> listener) {
        changeListeners.remove(listener);
    }

    private void fireChanged() {
        for (Consumer<DiagramDocument> l : changeListeners) {
            l.accept(this);
        }
    }
}
