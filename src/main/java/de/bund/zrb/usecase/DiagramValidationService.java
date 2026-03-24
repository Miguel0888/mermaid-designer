package de.bund.zrb.usecase;

import de.bund.zrb.domain.DiagramRenderer;
import de.bund.zrb.domain.DiagramRenderer.RenderResult;
import de.bund.zrb.domain.DiagramType;
import de.bund.zrb.domain.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validates Mermaid source code by attempting a render and analysing errors.
 */
public class DiagramValidationService {

    private final DiagramRenderer renderer;

    public DiagramValidationService(DiagramRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Validate the given Mermaid source.
     *
     * @return list of errors/warnings (empty if valid)
     */
    public List<ValidationError> validate(String source) {
        if (source == null || source.isBlank()) {
            return Collections.singletonList(
                    new ValidationError("Kein Diagramm-Quelltext vorhanden.",
                            ValidationError.Severity.WARNING));
        }

        List<ValidationError> errors = new ArrayList<>();

        // Check diagram type detection
        DiagramType type = DiagramType.detect(source);
        if (type == DiagramType.UNKNOWN) {
            errors.add(new ValidationError(
                    "Unbekannter Diagramm-Typ. Die erste Zeile sollte ein Mermaid-Schlüsselwort enthalten (z.B. 'flowchart TD', 'classDiagram').",
                    ValidationError.Severity.WARNING));
        }

        // Try rendering — Mermaid itself provides the best validation
        RenderResult result = renderer.renderDetailed(source);
        if (!result.success()) {
            String msg = result.errorMessage() != null
                    ? result.errorMessage()
                    : "Unbekannter Render-Fehler.";
            errors.add(new ValidationError(msg, ValidationError.Severity.ERROR));
        }

        return errors;
    }
}
