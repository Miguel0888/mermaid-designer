package de.bund.zrb.usecase;

import de.bund.zrb.domain.DiagramRenderer;
import de.bund.zrb.domain.DiagramRenderer.RenderResult;

/**
 * Application service that provides diagram preview rendering.
 * Delegates to the DiagramRenderer port.
 */
public class DiagramPreviewService {

    private final DiagramRenderer renderer;

    public DiagramPreviewService(DiagramRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Render a Mermaid source string to SVG for preview.
     *
     * @param source Mermaid code
     * @return SVG string or null
     */
    public String renderPreview(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return renderer.renderToSvg(source);
    }

    /**
     * Render with detailed error information.
     */
    public RenderResult renderDetailed(String source) {
        if (source == null || source.isBlank()) {
            return RenderResult.error("Kein Quelltext vorhanden.");
        }
        return renderer.renderDetailed(source);
    }
}
