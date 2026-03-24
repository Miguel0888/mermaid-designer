package de.bund.zrb.domain;

import java.util.Objects;

/**
 * Immutable value object representing a range in the Mermaid source text.
 * Lines and columns are 1-based.
 */
public final class SourceRange {

    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    public SourceRange(int startLine, int startColumn, int endLine, int endColumn) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    /** Convenience: single-line range. */
    public static SourceRange ofLine(int line) {
        return new SourceRange(line, 1, line, Integer.MAX_VALUE);
    }

    public int getStartLine() { return startLine; }
    public int getStartColumn() { return startColumn; }
    public int getEndLine() { return endLine; }
    public int getEndColumn() { return endColumn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceRange)) return false;
        SourceRange that = (SourceRange) o;
        return startLine == that.startLine && startColumn == that.startColumn
                && endLine == that.endLine && endColumn == that.endColumn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startLine, startColumn, endLine, endColumn);
    }

    @Override
    public String toString() {
        return String.format("[%d:%d – %d:%d]", startLine, startColumn, endLine, endColumn);
    }
}
