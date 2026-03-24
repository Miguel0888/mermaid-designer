package de.bund.zrb.usecase;

import de.bund.zrb.domain.DiagramDocument;
import de.bund.zrb.domain.DiagramRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiagramEditorUseCaseTest {

    private DiagramDocument document;
    private DiagramEditorUseCase useCase;

    // Stub renderer that always succeeds
    private final DiagramRenderer stubRenderer = new DiagramRenderer() {
        @Override
        public String renderToSvg(String mermaidCode) {
            return "<svg>ok</svg>";
        }

        @Override
        public RenderResult renderDetailed(String mermaidCode) {
            return RenderResult.ok("<svg>ok</svg>");
        }
    };

    @BeforeEach
    void setUp() {
        document = new DiagramDocument("flowchart TD\n    A[Start] --> B[Ende]");
        useCase = new DiagramEditorUseCase(
                document,
                new MermaidSnippetFactory(),
                new DiagramValidationService(stubRenderer));
    }

    @Test
    void updateSourceChangesDocument() {
        useCase.updateSource("classDiagram");
        assertEquals("classDiagram", document.getSource());
        assertTrue(document.isModified());
    }

    @Test
    void undoAndRedo() {
        useCase.updateSource("V2");
        useCase.updateSource("V3");
        useCase.undo();
        assertEquals("V2", document.getSource());
        useCase.redo();
        assertEquals("V3", document.getSource());
    }

    @Test
    void addNodeAppendsToSource() {
        useCase.addNode("C", "Neuer Knoten");
        String source = document.getSource();
        assertTrue(source.contains("C"));
        assertTrue(source.contains("Neuer Knoten"));
    }

    @Test
    void addEdgeAppendsToSource() {
        useCase.addEdge("A", "B", "verbindet");
        String source = document.getSource();
        assertTrue(source.contains("A"));
        assertTrue(source.contains("B"));
        assertTrue(source.contains("verbindet"));
    }

    @Test
    void renameElementReplacesAllOccurrences() {
        useCase.renameElement("Start", "Begin");
        String source = document.getSource();
        assertFalse(source.contains("Start"));
        assertTrue(source.contains("Begin"));
    }

    @Test
    void deleteLineRemovesCorrectLine() {
        // Line 2 is "    A[Start] --> B[Ende]"
        useCase.deleteLine(2);
        String source = document.getSource();
        assertFalse(source.contains("A[Start]"));
        assertTrue(source.contains("flowchart TD"));
    }

    @Test
    void validateReturnsEmptyForValidDiagram() {
        var errors = useCase.validate();
        assertTrue(errors.isEmpty());
    }
}
