package de.bund.zrb.domain;

/**
 * Represents a validation problem found in Mermaid source code.
 */
public final class ValidationError {

    public enum Severity { ERROR, WARNING, INFO }

    private final String message;
    private final Severity severity;
    private final SourceRange range;   // nullable — not every error has a location

    public ValidationError(String message, Severity severity, SourceRange range) {
        this.message = message;
        this.severity = severity;
        this.range = range;
    }

    public ValidationError(String message, Severity severity) {
        this(message, severity, null);
    }

    public String getMessage() { return message; }
    public Severity getSeverity() { return severity; }
    public SourceRange getRange() { return range; }

    @Override
    public String toString() {
        String loc = range != null ? " " + range : "";
        return severity + loc + ": " + message;
    }
}
