package de.bund.zrb.domain;

/**
 * Supported Mermaid diagram types with their source-code keywords.
 */
public enum DiagramType {

    FLOWCHART("flowchart", "Flowchart"),
    GRAPH("graph", "Graph"),
    SEQUENCE("sequenceDiagram", "Sequenzdiagramm"),
    CLASS("classDiagram", "Klassendiagramm"),
    STATE("stateDiagram-v2", "Zustandsdiagramm"),
    ER("erDiagram", "ER-Diagramm"),
    PIE("pie", "Kreisdiagramm"),
    GANTT("gantt", "Gantt"),
    JOURNEY("journey", "User Journey"),
    MINDMAP("mindmap", "Mindmap"),
    GIT_GRAPH("gitGraph", "Git Graph"),
    SANKEY("sankey-beta", "Sankey"),
    BLOCK("block-beta", "Block"),
    ARCHITECTURE("architecture-beta", "Architektur"),
    C4_CONTEXT("C4Context", "C4 Kontext"),
    REQUIREMENT("requirementDiagram", "Anforderungsdiagramm"),
    UNKNOWN("", "Unbekannt");

    private final String keyword;
    private final String displayName;

    DiagramType(String keyword, String displayName) {
        this.keyword = keyword;
        this.displayName = displayName;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Detect diagram type from Mermaid source code by examining the first non-empty,
     * non-comment line.
     */
    public static DiagramType detect(String source) {
        if (source == null || source.isBlank()) {
            return UNKNOWN;
        }
        String firstLine = source.lines()
                .map(String::trim)
                .filter(l -> !l.isEmpty() && !l.startsWith("%%"))
                .findFirst()
                .orElse("");

        for (DiagramType type : values()) {
            if (type == UNKNOWN || type.keyword.isEmpty()) continue;
            if (firstLine.startsWith(type.keyword)) {
                return type;
            }
        }
        // "graph" is also used for flowcharts
        if (firstLine.startsWith("graph ")) {
            return GRAPH;
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
