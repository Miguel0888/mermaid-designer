package de.bund.zrb.domain;

/**
 * Port interface for rendering Mermaid code to SVG.
 * The infrastructure layer provides the actual implementation
 * (e.g. via mermaid-java / GraalJS).
 */
public interface DiagramRenderer {

    /**
     * Render Mermaid source code to an SVG string.
     *
     * @param mermaidCode the Mermaid definition
     * @return SVG string, or null if rendering failed
     */
    String renderToSvg(String mermaidCode);

    /**
     * Render with detailed result including potential error messages.
     */
    RenderResult renderDetailed(String mermaidCode);

    /**
     * Immutable result from a render attempt.
     */
    record RenderResult(boolean success, String svg, String errorMessage) {
        public static RenderResult ok(String svg) {
            return new RenderResult(true, svg, null);
        }
        public static RenderResult error(String message) {
            return new RenderResult(false, null, message);
        }
    }
}
