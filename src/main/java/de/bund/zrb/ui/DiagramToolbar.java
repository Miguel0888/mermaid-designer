package de.bund.zrb.ui;

import de.bund.zrb.domain.DiagramType;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Toolbar component with file operations, diagram type selection,
 * structured editing actions, and export options.
 */
public class DiagramToolbar extends VBox {

    // File operations
    private final Button btnNew = new Button("📄 Neu");
    private final Button btnOpen = new Button("📂 Öffnen");
    private final Button btnSave = new Button("💾 Speichern");
    private final Button btnSaveAs = new Button("Speichern als…");

    // Edit operations
    private final Button btnUndo = new Button("↩ Rückgängig");
    private final Button btnRedo = new Button("↪ Wiederholen");

    // Diagram type
    private final ComboBox<DiagramType> cboType = new ComboBox<>(
            FXCollections.observableArrayList(
                    DiagramType.FLOWCHART, DiagramType.SEQUENCE, DiagramType.CLASS,
                    DiagramType.STATE, DiagramType.ER, DiagramType.MINDMAP,
                    DiagramType.PIE, DiagramType.GANTT, DiagramType.JOURNEY,
                    DiagramType.GIT_GRAPH, DiagramType.C4_CONTEXT, DiagramType.REQUIREMENT
            ));

    // Insert operations
    private final Button btnAddNode = new Button("＋ Knoten");
    private final Button btnAddEdge = new Button("🔗 Verbindung");

    // Zoom
    private final Button btnZoomIn = new Button("🔍+");
    private final Button btnZoomOut = new Button("🔍−");
    private final Button btnZoomReset = new Button("1:1");

    // Export
    private final MenuButton btnExport = new MenuButton("📤 Export");
    private final MenuItem miExportSvg = new MenuItem("Als SVG…");
    private final MenuItem miExportPng = new MenuItem("Als PNG…");
    private final MenuItem miExportMermaid = new MenuItem("Als Mermaid (.mmd)…");
    private final MenuItem miExportMarkdown = new MenuItem("Als Markdown (.md)…");

    // Validate
    private final Button btnValidate = new Button("✓ Prüfen");

    // Status label
    private final Label lblStatus = new Label("Bereit");

    public DiagramToolbar() {
        // Configure
        cboType.setPromptText("Diagramm-Typ…");
        cboType.setTooltip(new Tooltip("Vorlage für neuen Diagramm-Typ laden"));
        btnExport.getItems().addAll(miExportSvg, miExportPng, miExportMermaid, miExportMarkdown);

        // Tooltips
        btnNew.setTooltip(new Tooltip("Neues Diagramm (Ctrl+N)"));
        btnOpen.setTooltip(new Tooltip("Datei öffnen (Ctrl+O)"));
        btnSave.setTooltip(new Tooltip("Speichern (Ctrl+S)"));
        btnUndo.setTooltip(new Tooltip("Rückgängig (Ctrl+Z)"));
        btnRedo.setTooltip(new Tooltip("Wiederholen (Ctrl+Y)"));
        btnAddNode.setTooltip(new Tooltip("Neuen Knoten hinzufügen"));
        btnAddEdge.setTooltip(new Tooltip("Neue Verbindung hinzufügen"));
        btnValidate.setTooltip(new Tooltip("Mermaid-Syntax prüfen"));

        // Main toolbar
        ToolBar mainBar = new ToolBar();
        mainBar.getItems().addAll(
                btnNew, btnOpen, btnSave, btnSaveAs,
                new Separator(),
                btnUndo, btnRedo,
                new Separator(),
                new Label("Vorlage:"), cboType,
                new Separator(),
                btnAddNode, btnAddEdge,
                new Separator(),
                btnZoomIn, btnZoomOut, btnZoomReset,
                new Separator(),
                btnValidate,
                btnExport
        );

        // Status bar
        HBox statusBar = new HBox(lblStatus);
        statusBar.setPadding(new Insets(4, 8, 4, 8));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().add(mainBar);
    }

    // ────────────────────────────────────────────────────────────
    //  Action bindings
    // ────────────────────────────────────────────────────────────

    public void onNew(Runnable action)           { btnNew.setOnAction(e -> action.run()); }
    public void onOpen(Runnable action)          { btnOpen.setOnAction(e -> action.run()); }
    public void onSave(Runnable action)          { btnSave.setOnAction(e -> action.run()); }
    public void onSaveAs(Runnable action)        { btnSaveAs.setOnAction(e -> action.run()); }
    public void onUndo(Runnable action)          { btnUndo.setOnAction(e -> action.run()); }
    public void onRedo(Runnable action)          { btnRedo.setOnAction(e -> action.run()); }
    public void onAddNode(Runnable action)       { btnAddNode.setOnAction(e -> action.run()); }
    public void onAddEdge(Runnable action)       { btnAddEdge.setOnAction(e -> action.run()); }
    public void onZoomIn(Runnable action)        { btnZoomIn.setOnAction(e -> action.run()); }
    public void onZoomOut(Runnable action)       { btnZoomOut.setOnAction(e -> action.run()); }
    public void onZoomReset(Runnable action)     { btnZoomReset.setOnAction(e -> action.run()); }
    public void onValidate(Runnable action)      { btnValidate.setOnAction(e -> action.run()); }
    public void onExportSvg(Runnable action)     { miExportSvg.setOnAction(e -> action.run()); }
    public void onExportPng(Runnable action)     { miExportPng.setOnAction(e -> action.run()); }
    public void onExportMermaid(Runnable action)  { miExportMermaid.setOnAction(e -> action.run()); }
    public void onExportMarkdown(Runnable action) { miExportMarkdown.setOnAction(e -> action.run()); }

    public void onTypeSelected(Consumer<DiagramType> handler) {
        cboType.setOnAction(e -> {
            DiagramType selected = cboType.getValue();
            if (selected != null) {
                handler.accept(selected);
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    //  State updates
    // ────────────────────────────────────────────────────────────

    public void setUndoEnabled(boolean enabled) { btnUndo.setDisable(!enabled); }
    public void setRedoEnabled(boolean enabled) { btnRedo.setDisable(!enabled); }

    public void setStatus(String text) {
        lblStatus.setText(text);
    }

    public void setDiagramType(DiagramType type) {
        cboType.setValue(type);
    }

    public Label getStatusLabel() {
        return lblStatus;
    }
}
