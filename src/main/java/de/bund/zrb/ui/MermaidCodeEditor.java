package de.bund.zrb.ui;

import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * JavaFX component wrapping a RichTextFX {@link CodeArea} with Mermaid syntax highlighting,
 * line numbers and debounced change notifications.
 */
public class MermaidCodeEditor extends StackPane {

    private final CodeArea codeArea;
    private final VirtualizedScrollPane<CodeArea> scrollPane;
    private Subscription highlightSubscription;

    public MermaidCodeEditor() {
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setWrapText(false);
        codeArea.getStyleClass().add("mermaid-code-area");

        // Apply syntax highlighting with a slight delay to avoid excessive recomputation
        highlightSubscription = codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(50))
                .subscribe(ignore -> applyHighlighting());

        scrollPane = new VirtualizedScrollPane<>(codeArea);
        getChildren().add(scrollPane);

        // Load editor CSS
        var css = getClass().getResource("/de/bund/zrb/ui/mermaid-editor.css");
        if (css != null) {
            getStylesheets().add(css.toExternalForm());
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────

    public String getText() {
        return codeArea.getText();
    }

    public void setText(String text) {
        codeArea.replaceText(text != null ? text : "");
        codeArea.moveTo(0);
        applyHighlighting();
    }

    /**
     * Append text at the end and scroll to it.
     */
    public void appendText(String snippet) {
        String current = codeArea.getText();
        String separator = current.endsWith("\n") ? "" : "\n";
        codeArea.replaceText(current + separator + snippet);
        codeArea.moveTo(codeArea.getLength());
        codeArea.requestFollowCaret();
    }

    /**
     * Get the current caret line (0-based).
     */
    public int getCaretLine() {
        return codeArea.getCurrentParagraph();
    }

    /**
     * Move caret to a specific line (0-based).
     */
    public void goToLine(int line) {
        if (line >= 0 && line < codeArea.getParagraphs().size()) {
            codeArea.moveTo(line, 0);
            codeArea.requestFollowCaret();
        }
    }

    /**
     * Register a listener for text changes with debouncing.
     *
     * @param delayMs  debounce delay in milliseconds
     * @param listener callback receiving the new text
     * @return subscription that can be disposed to stop listening
     */
    public Subscription onTextChanged(long delayMs, Consumer<String> listener) {
        return codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(delayMs))
                .subscribe(ignore -> listener.accept(codeArea.getText()));
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public void dispose() {
        if (highlightSubscription != null) {
            highlightSubscription.unsubscribe();
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Syntax highlighting
    // ────────────────────────────────────────────────────────────

    private void applyHighlighting() {
        String text = codeArea.getText();
        if (text.isEmpty()) return;
        try {
            codeArea.setStyleSpans(0, MermaidSyntaxHighlighter.computeHighlighting(text));
        } catch (Exception e) {
            // Ignore highlighting errors gracefully
        }
    }
}
