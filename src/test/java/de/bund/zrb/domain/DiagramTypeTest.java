package de.bund.zrb.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DiagramTypeTest {

    @ParameterizedTest
    @CsvSource({
            "'flowchart TD\n    A-->B', FLOWCHART",
            "'classDiagram\n    class Foo', CLASS",
            "'sequenceDiagram\n    A->>B: msg', SEQUENCE",
            "'stateDiagram-v2\n    [*]-->A', STATE",
            "'erDiagram\n    A||--o{B : rel', ER",
            "'mindmap\n    root', MINDMAP",
            "'pie title Test\n    \"A\": 50', PIE",
            "'gantt\n    title Plan', GANTT",
            "'journey\n    title UX', JOURNEY",
            "'gitGraph\n    commit', GIT_GRAPH",
            "'C4Context\n    Person(a,b)', C4_CONTEXT",
            "'graph LR\n    A-->B', GRAPH"
    })
    void detectDiagramType(String source, DiagramType expected) {
        assertEquals(expected, DiagramType.detect(source));
    }

    @Test
    void detectUnknown() {
        assertEquals(DiagramType.UNKNOWN, DiagramType.detect("some random text"));
        assertEquals(DiagramType.UNKNOWN, DiagramType.detect(""));
        assertEquals(DiagramType.UNKNOWN, DiagramType.detect(null));
    }

    @Test
    void detectIgnoresComments() {
        String source = "%% This is a comment\nflowchart TD\n    A-->B";
        assertEquals(DiagramType.FLOWCHART, DiagramType.detect(source));
    }
}
