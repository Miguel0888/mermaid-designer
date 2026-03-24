package de.bund.zrb.ui;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex-based syntax highlighter for Mermaid diagram code.
 * Returns {@link StyleSpans} compatible with RichTextFX CodeArea.
 */
public final class MermaidSyntaxHighlighter {

    private MermaidSyntaxHighlighter() {}

    // Language keywords (diagram type declarations)
    private static final String KEYWORD_PATTERN =
            "\\b(flowchart|graph|sequenceDiagram|classDiagram|stateDiagram-v2|stateDiagram|erDiagram"
                    + "|pie|gantt|journey|mindmap|gitGraph|sankey-beta|block-beta|architecture-beta"
                    + "|C4Context|C4Container|C4Component|C4Dynamic|C4Deployment"
                    + "|requirementDiagram|timeline)\\b";

    // Sub-keywords / structural
    private static final String STRUCTURE_PATTERN =
            "\\b(subgraph|end|participant|actor|activate|deactivate|loop|alt|else|opt|par|critical"
                    + "|break|rect|note|Note|over|right of|left of|class|section|title|dateFormat"
                    + "|commit|branch|checkout|merge|cherry-pick|state"
                    + "|requirement|functionalRequirement|interfaceRequirement|performanceRequirement"
                    + "|physicalRequirement|designConstraint|element"
                    + "|Person|System|System_Ext|Container|Container_Ext|Component|Rel|BiRel"
                    + "|Boundary|Enterprise_Boundary|System_Boundary)\\b";

    // Direction keywords
    private static final String DIRECTION_PATTERN =
            "\\b(TD|TB|BT|LR|RL)\\b";

    // Arrows and relation symbols
    private static final String ARROW_PATTERN =
            "(-->|---->|-.->|-\\.->|==>|--[>ox]|\\.\\.[>ox]|--\\|>|\\.\\.\\|>|--\\*|--o"
                    + "|<-->|<-.->|<-\\.->|<==>"
                    + "|->>|-->>|-)\\)|--\\)"
                    + "|\\|\\|--o\\{|\\}\\|--\\|\\{|\\|\\|--\\|\\{|\\}o--o\\{|\\|o--o\\|"
                    + "|-->|---|-\\.-|===)";

    // Strings in quotes
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";

    // Comments
    private static final String COMMENT_PATTERN = "%%[^\n]*";

    // Labels in brackets/parens:  [text], (text), {text}, ((text)), etc.
    private static final String LABEL_PATTERN = "\\[([^\\]]+)\\]|\\(([^)]+)\\)|\\{([^}]+)\\}";

    // Numeric values (for pie, gantt, etc.)
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";

    // Combined pattern
    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<STRUCTURE>" + STRUCTURE_PATTERN + ")"
                    + "|(?<DIRECTION>" + DIRECTION_PATTERN + ")"
                    + "|(?<ARROW>" + ARROW_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
    );

    /**
     * Compute syntax highlighting spans for the given Mermaid source text.
     */
    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
        int lastEnd = 0;

        while (matcher.find()) {
            String styleClass = determineStyleClass(matcher);
            builder.add(Collections.emptyList(), matcher.start() - lastEnd);
            builder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastEnd = matcher.end();
        }

        builder.add(Collections.emptyList(), text.length() - lastEnd);
        return builder.create();
    }

    private static String determineStyleClass(Matcher matcher) {
        if (matcher.group("COMMENT") != null) return "comment";
        if (matcher.group("STRING") != null) return "string";
        if (matcher.group("KEYWORD") != null) return "keyword";
        if (matcher.group("STRUCTURE") != null) return "structure";
        if (matcher.group("DIRECTION") != null) return "direction";
        if (matcher.group("ARROW") != null) return "arrow";
        if (matcher.group("NUMBER") != null) return "number";
        return "";
    }
}
