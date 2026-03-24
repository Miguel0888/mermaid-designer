package de.bund.zrb.ui;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * JavaFX pane that displays rendered SVG diagrams in a WebView.
 * Supports zoom/pan and click-to-element bridging back to Java.
 */
public class SvgPreviewPane extends StackPane {

    private static final Logger LOG = Logger.getLogger(SvgPreviewPane.class.getName());

    private final WebView webView;
    private final WebEngine webEngine;
    private Consumer<String> onElementClicked;
    private String lastSvg;

    public SvgPreviewPane() {
        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Set up JS → Java bridge once page is loaded
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                installJsBridge();
            }
        });

        getChildren().add(webView);
        getStyleClass().add("svg-preview-pane");

        // Load initial empty page
        webEngine.loadContent(buildHtmlShell(""));
    }

    // ────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────

    /**
     * Display the given SVG in the preview. Call from the JavaFX thread.
     */
    public void showSvg(String svg) {
        this.lastSvg = svg;
        Platform.runLater(() -> {
            if (svg == null || svg.isBlank()) {
                webEngine.loadContent(buildHtmlShell("<p style='color:#888; font-family:sans-serif; text-align:center; margin-top:40px;'>Keine Vorschau verfügbar</p>"));
            } else {
                webEngine.loadContent(buildHtmlShell(svg));
            }
        });
    }

    /**
     * Display an error message in the preview pane.
     */
    public void showError(String message) {
        Platform.runLater(() -> {
            String html = "<div style='color:#e74c3c; font-family:monospace; white-space:pre-wrap; "
                    + "padding:20px; font-size:13px; background:#fff5f5; border:1px solid #e74c3c; "
                    + "border-radius:6px; margin:20px;'>"
                    + escapeHtml(message) + "</div>";
            webEngine.loadContent(buildHtmlShell(html));
        });
    }

    /**
     * Register a callback for when the user clicks on an SVG element.
     * The callback receives the element ID or data-attribute.
     */
    public void setOnElementClicked(Consumer<String> handler) {
        this.onElementClicked = handler;
    }

    /**
     * Get the last rendered SVG string.
     */
    public String getLastSvg() {
        return lastSvg;
    }

    /**
     * Zoom the preview by a factor.
     */
    public void setZoom(double factor) {
        webView.setZoom(factor);
    }

    public void zoomIn() {
        webView.setZoom(Math.min(webView.getZoom() * 1.15, 5.0));
    }

    public void zoomOut() {
        webView.setZoom(Math.max(webView.getZoom() / 1.15, 0.2));
    }

    public void resetZoom() {
        webView.setZoom(1.0);
    }

    // ────────────────────────────────────────────────────────────
    //  JS → Java bridge
    // ────────────────────────────────────────────────────────────

    private void installJsBridge() {
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaBridge", new JsBridge());
        } catch (Exception e) {
            LOG.fine("JS bridge installation skipped: " + e.getMessage());
        }
    }

    /**
     * Public inner class exposed to JavaScript for callbacks.
     */
    public class JsBridge {
        public void onElementClick(String elementId) {
            Platform.runLater(() -> {
                if (onElementClicked != null) {
                    onElementClicked.accept(elementId);
                }
            });
        }
    }

    // ────────────────────────────────────────────────────────────
    //  HTML generation
    // ────────────────────────────────────────────────────────────

    private String buildHtmlShell(String bodyContent) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body {
                        width: 100%%; height: 100%%;
                        overflow: auto;
                        background: #fafafa;
                        display: flex;
                        justify-content: center;
                        align-items: flex-start;
                        padding: 16px;
                    }
                    svg {
                        max-width: 100%%;
                        height: auto;
                        cursor: default;
                    }
                    /* Highlight hovered nodes */
                    .node:hover, .cluster:hover {
                        filter: brightness(0.92);
                        cursor: pointer;
                    }
                    .edgePath:hover {
                        filter: brightness(0.85);
                        cursor: pointer;
                    }
                </style>
                </head>
                <body>
                %s
                <script>
                    document.addEventListener('click', function(e) {
                        var el = e.target;
                        // Walk up to find the nearest element with an id or data-id
                        while (el && el !== document.body) {
                            var id = el.getAttribute('data-id') || el.getAttribute('id') || '';
                            if (id && id !== '' && !id.startsWith('_')) {
                                if (typeof javaBridge !== 'undefined') {
                                    javaBridge.onElementClick(id);
                                }
                                break;
                            }
                            el = el.parentElement;
                        }
                    });
                </script>
                </body>
                </html>
                """.formatted(bodyContent);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
