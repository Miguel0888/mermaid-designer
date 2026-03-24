package de.bund.zrb.usecase;

import de.bund.zrb.domain.DiagramType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MermaidSnippetFactoryTest {

    private final MermaidSnippetFactory factory = new MermaidSnippetFactory();

    @Test
    void flowchartNode() {
        String snippet = factory.flowchartNode("A", "Startknoten");
        assertTrue(snippet.contains("A"));
        assertTrue(snippet.contains("Startknoten"));
    }

    @Test
    void flowchartEdgeWithLabel() {
        String snippet = factory.flowchartEdge("A", "B", "ja");
        assertTrue(snippet.contains("A"));
        assertTrue(snippet.contains("B"));
        assertTrue(snippet.contains("ja"));
    }

    @Test
    void flowchartEdgeWithoutLabel() {
        String snippet = factory.flowchartEdge("A", "B", null);
        assertTrue(snippet.contains("A --> B"));
    }

    @Test
    void classDefinition() {
        String snippet = factory.classDefinition("Person");
        assertTrue(snippet.contains("class Person"));
    }

    @Test
    void sequenceMessage() {
        String snippet = factory.sequenceMessage("Client", "Server", "POST /api", false);
        assertTrue(snippet.contains("Client"));
        assertTrue(snippet.contains("Server"));
        assertTrue(snippet.contains("POST /api"));
    }

    @Test
    void addNodeSnippetDelegatesToCorrectType() {
        assertNotNull(factory.addNodeSnippet(DiagramType.FLOWCHART, "A", "Label"));
        assertNotNull(factory.addNodeSnippet(DiagramType.CLASS, "Cls", "MyClass"));
        assertNotNull(factory.addNodeSnippet(DiagramType.SEQUENCE, "P1", "Participant"));
        assertNotNull(factory.addNodeSnippet(DiagramType.MINDMAP, "node", "Topic"));
        assertNotNull(factory.addNodeSnippet(DiagramType.STATE, "S1", "Active"));
        assertNotNull(factory.addNodeSnippet(DiagramType.ER, "E1", "Customer"));
    }

    @Test
    void addEdgeSnippetDelegatesToCorrectType() {
        assertNotNull(factory.addEdgeSnippet(DiagramType.FLOWCHART, "A", "B", "label"));
        assertNotNull(factory.addEdgeSnippet(DiagramType.CLASS, "A", "B", "inherits"));
        assertNotNull(factory.addEdgeSnippet(DiagramType.SEQUENCE, "A", "B", "msg"));
        assertNotNull(factory.addEdgeSnippet(DiagramType.STATE, "S1", "S2", "trigger"));
        assertNotNull(factory.addEdgeSnippet(DiagramType.ER, "A", "B", "rel"));
    }
}
