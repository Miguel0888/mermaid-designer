package de.bund.zrb.domain;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DiagramDocumentTest {

    @Test
    void initialStateIsClean() {
        DiagramDocument doc = new DiagramDocument("flowchart TD\n    A-->B");
        assertFalse(doc.isModified());
        assertEquals(DiagramType.FLOWCHART, doc.getDiagramType());
        assertNull(doc.getFilePath());
    }

    @Test
    void updateSourceMarksAsModified() {
        DiagramDocument doc = new DiagramDocument("flowchart TD");
        doc.updateSource("classDiagram");
        assertTrue(doc.isModified());
        assertEquals(DiagramType.CLASS, doc.getDiagramType());
        assertEquals("classDiagram", doc.getSource());
    }

    @Test
    void undoRestoresPreviousState() {
        DiagramDocument doc = new DiagramDocument("V1");
        doc.updateSource("V2");
        doc.updateSource("V3");

        assertTrue(doc.canUndo());
        doc.undo();
        assertEquals("V2", doc.getSource());
        doc.undo();
        assertEquals("V1", doc.getSource());
        assertFalse(doc.canUndo());
    }

    @Test
    void redoRestoresAfterUndo() {
        DiagramDocument doc = new DiagramDocument("V1");
        doc.updateSource("V2");
        doc.undo();
        assertTrue(doc.canRedo());
        doc.redo();
        assertEquals("V2", doc.getSource());
        assertFalse(doc.canRedo());
    }

    @Test
    void updateAfterUndoClearsRedoStack() {
        DiagramDocument doc = new DiagramDocument("V1");
        doc.updateSource("V2");
        doc.undo();
        doc.updateSource("V3");
        assertFalse(doc.canRedo());
        assertEquals("V3", doc.getSource());
    }

    @Test
    void markSavedClearsModifiedFlag() {
        DiagramDocument doc = new DiagramDocument("V1");
        doc.updateSource("V2");
        assertTrue(doc.isModified());
        doc.markSaved();
        assertFalse(doc.isModified());
    }

    @Test
    void changeListenerIsNotified() {
        DiagramDocument doc = new DiagramDocument("V1");
        AtomicInteger callCount = new AtomicInteger(0);
        doc.addChangeListener(d -> callCount.incrementAndGet());

        doc.updateSource("V2");
        assertEquals(1, callCount.get());

        doc.undo();
        assertEquals(2, callCount.get());
    }

    @Test
    void titleReflectsModifiedState() {
        DiagramDocument doc = new DiagramDocument("V1");
        assertEquals("Unbenannt", doc.getTitle());
        doc.updateSource("V2");
        assertEquals("Unbenannt *", doc.getTitle());
    }

    @Test
    void duplicateUpdateIsIgnored() {
        DiagramDocument doc = new DiagramDocument("V1");
        doc.updateSource("V1"); // same content
        assertFalse(doc.isModified());
        assertFalse(doc.canUndo());
    }
}
