package de.bund.zrb.infrastructure;

import com.aresstack.Mermaid;
import com.aresstack.mermaid.JsExecutionResult;
import de.bund.zrb.domain.DiagramRenderer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Infrastructure adapter that implements {@link DiagramRenderer}
 * using the mermaid-java library (GraalJS + Batik).
 */
public class MermaidJavaRenderer implements DiagramRenderer {

    private static final Logger LOG = Logger.getLogger(MermaidJavaRenderer.class.getName());

    @Override
    public String renderToSvg(String mermaidCode) {
        try {
            return Mermaid.render(mermaidCode);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Render failed: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public RenderResult renderDetailed(String mermaidCode) {
        try {
            JsExecutionResult result = Mermaid.renderDetailed(mermaidCode);
            if (result.isSuccessful()) {
                return RenderResult.ok(result.getOutput());
            } else {
                return RenderResult.error(result.getErrorMessage());
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Render failed: " + e.getMessage(), e);
            return RenderResult.error(e.getMessage());
        }
    }
}
