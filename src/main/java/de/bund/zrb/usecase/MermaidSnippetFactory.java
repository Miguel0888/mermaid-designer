package de.bund.zrb.usecase;

import de.bund.zrb.domain.DiagramType;

/**
 * Generates Mermaid code snippets for common editing operations.
 * Used by toolbar actions and assistants to produce structurally correct code.
 */
public class MermaidSnippetFactory {

    // ────────────────────────────────────────────────────────────
    //  Flowchart / Graph snippets
    // ────────────────────────────────────────────────────────────

    public String flowchartNode(String id, String label) {
        return String.format("    %s[\"%s\"]", id, label);
    }

    public String flowchartEdge(String fromId, String toId, String label) {
        if (label != null && !label.isBlank()) {
            return String.format("    %s -->|\"%s\"| %s", fromId, label, toId);
        }
        return String.format("    %s --> %s", fromId, toId);
    }

    public String flowchartSubgraph(String id, String title) {
        return String.format("    subgraph %s [\"%s\"]\n        \n    end", id, title);
    }

    // ────────────────────────────────────────────────────────────
    //  Class Diagram snippets
    // ────────────────────────────────────────────────────────────

    public String classDefinition(String name) {
        return String.format("    class %s {\n        \n    }", name);
    }

    public String classAttribute(String visibility, String type, String name) {
        return String.format("        %s%s %s", visibility, type, name);
    }

    public String classMethod(String visibility, String returnType, String name) {
        return String.format("        %s%s() %s", visibility, name, returnType);
    }

    public String classRelation(String from, String to, String type, String label) {
        // type: "--|>", "..|>", "--*", "--o", "--", ".."
        String rel = String.format("    %s %s %s", from, type, to);
        if (label != null && !label.isBlank()) {
            rel += " : " + label;
        }
        return rel;
    }

    // ────────────────────────────────────────────────────────────
    //  Sequence Diagram snippets
    // ────────────────────────────────────────────────────────────

    public String sequenceParticipant(String alias, String name) {
        return String.format("    participant %s as %s", alias, name);
    }

    public String sequenceMessage(String from, String to, String message, boolean isReply) {
        String arrow = isReply ? "-->>" : "->>";
        return String.format("    %s%s%s: %s", from, arrow, to, message);
    }

    public String sequenceNote(String position, String actor, String text) {
        // position: "over", "right of", "left of"
        return String.format("    Note %s %s: %s", position, actor, text);
    }

    // ────────────────────────────────────────────────────────────
    //  Mindmap snippets
    // ────────────────────────────────────────────────────────────

    public String mindmapNode(int depth, String text) {
        String indent = "    ".repeat(depth);
        return indent + text;
    }

    // ────────────────────────────────────────────────────────────
    //  State Diagram snippets
    // ────────────────────────────────────────────────────────────

    public String stateDefinition(String name, String description) {
        if (description != null && !description.isBlank()) {
            return String.format("    state \"%s\" as %s", description, name);
        }
        return String.format("    %s", name);
    }

    public String stateTransition(String from, String to, String label) {
        String trans = String.format("    %s --> %s", from, to);
        if (label != null && !label.isBlank()) {
            trans += " : " + label;
        }
        return trans;
    }

    // ────────────────────────────────────────────────────────────
    //  ER Diagram snippets
    // ────────────────────────────────────────────────────────────

    public String erEntity(String name) {
        return String.format("    %s {\n        \n    }", name);
    }

    public String erRelation(String from, String cardinality, String to, String label) {
        // cardinality: "||--o{", "}|--|{", etc.
        return String.format("    %s %s %s : \"%s\"", from, cardinality, to, label);
    }

    // ────────────────────────────────────────────────────────────
    //  Generic insert for any diagram type
    // ────────────────────────────────────────────────────────────

    /**
     * Generate an appropriate "add node" snippet based on the current diagram type.
     */
    public String addNodeSnippet(DiagramType type, String id, String label) {
        return switch (type) {
            case FLOWCHART, GRAPH -> flowchartNode(id, label);
            case CLASS -> classDefinition(label);
            case SEQUENCE -> sequenceParticipant(id, label);
            case MINDMAP -> mindmapNode(1, label);
            case STATE -> stateDefinition(id, label);
            case ER -> erEntity(label);
            default -> "    %% TODO: " + label;
        };
    }

    /**
     * Generate an appropriate "add edge/relation" snippet for the current diagram type.
     */
    public String addEdgeSnippet(DiagramType type, String fromId, String toId, String label) {
        return switch (type) {
            case FLOWCHART, GRAPH -> flowchartEdge(fromId, toId, label);
            case CLASS -> classRelation(fromId, toId, "--|>", label);
            case SEQUENCE -> sequenceMessage(fromId, toId, label, false);
            case STATE -> stateTransition(fromId, toId, label);
            case ER -> erRelation(fromId, "||--o{", toId, label);
            default -> "    %% TODO: " + fromId + " -> " + toId;
        };
    }
}
