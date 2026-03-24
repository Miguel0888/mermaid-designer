package de.bund.zrb.ui;

import de.bund.zrb.domain.*;
import de.bund.zrb.infrastructure.FileDocumentRepository;
import de.bund.zrb.infrastructure.MermaidJavaRenderer;
import de.bund.zrb.usecase.DiagramEditorUseCase;
import de.bund.zrb.usecase.DiagramPreviewService;
import de.bund.zrb.usecase.DiagramValidationService;
import de.bund.zrb.usecase.MermaidSnippetFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main controller that orchestrates the entire Mermaid Designer UI.
 * Wires together domain, use-case and infrastructure components
 * following Clean Architecture.
 */
public class MainViewController {

    private static final Logger LOG = Logger.getLogger(MainViewController.class.getName());

    // ── Domain & Use Cases ──────────────────────────────────────
    private DiagramDocument document;
    private final DiagramRenderer renderer;
    private final DocumentRepository repository;
    private final DiagramPreviewService previewService;
    private final DiagramValidationService validationService;
    private final MermaidSnippetFactory snippetFactory;
    private DiagramEditorUseCase editorUseCase;

    // ── UI Components ───────────────────────────────────────────
    private final Stage stage;
    private final DiagramToolbar toolbar;
    private final MermaidCodeEditor codeEditor;
    private final SvgPreviewPane previewPane;
    private final Label statusLabel;
    private final Label typeLabel;

    private boolean suppressEditorSync = false;

    public MainViewController(Stage stage) {
        this.stage = stage;

        // Infrastructure
        this.renderer = new MermaidJavaRenderer();
        this.repository = new FileDocumentRepository();

        // Use cases
        this.previewService = new DiagramPreviewService(renderer);
        this.validationService = new DiagramValidationService(renderer);
        this.snippetFactory = new MermaidSnippetFactory();

        // UI Components
        this.toolbar = new DiagramToolbar();
        this.codeEditor = new MermaidCodeEditor();
        this.previewPane = new SvgPreviewPane();
        this.statusLabel = new Label("Bereit");
        this.typeLabel = new Label("");

        // Create initial document with default template
        newDocument(DiagramTemplates.DEFAULT_TEMPLATE);

        // Build the scene
        Scene scene = buildScene();
        stage.setScene(scene);
        stage.setTitle("Mermaid Designer");
        stage.setWidth(1400);
        stage.setHeight(900);

        // Wire actions
        wireToolbarActions();
        wireEditorEvents();
        wirePreviewEvents();
        wireKeyboardShortcuts(scene);

        // Initial render
        triggerPreviewUpdate();
    }

    // ════════════════════════════════════════════════════════════
    //  Scene building
    // ════════════════════════════════════════════════════════════

    private Scene buildScene() {
        BorderPane root = new BorderPane();

        // ── Top: Toolbar ────────────────────────────────────────
        root.setTop(toolbar);

        // ── Center: SplitPane (Editor | Preview) ────────────────
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.45);

        // Left side: code editor with a header
        VBox editorBox = new VBox();
        Label editorHeader = new Label("  📝 Mermaid-Quelltext");
        editorHeader.setStyle("-fx-font-weight: bold; -fx-padding: 6 0 6 0; -fx-font-size: 13;");
        VBox.setVgrow(codeEditor, Priority.ALWAYS);
        editorBox.getChildren().addAll(editorHeader, codeEditor);

        // Right side: SVG preview with a header
        VBox previewBox = new VBox();
        Label previewHeader = new Label("  👁 Vorschau");
        previewHeader.setStyle("-fx-font-weight: bold; -fx-padding: 6 0 6 0; -fx-font-size: 13;");
        VBox.setVgrow(previewPane, Priority.ALWAYS);
        previewBox.getChildren().addAll(previewHeader, previewPane);

        splitPane.getItems().addAll(editorBox, previewBox);
        root.setCenter(splitPane);

        // ── Bottom: Status bar ──────────────────────────────────
        HBox statusBar = new HBox(10, typeLabel, new Separator(Orientation.VERTICAL), statusLabel);
        statusBar.setPadding(new Insets(4, 12, 4, 12));
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1 0 0 0;");
        root.setBottom(statusBar);

        return new Scene(root);
    }

    // ════════════════════════════════════════════════════════════
    //  Toolbar actions
    // ════════════════════════════════════════════════════════════

    private void wireToolbarActions() {
        toolbar.onNew(this::handleNew);
        toolbar.onOpen(this::handleOpen);
        toolbar.onSave(this::handleSave);
        toolbar.onSaveAs(this::handleSaveAs);
        toolbar.onUndo(this::handleUndo);
        toolbar.onRedo(this::handleRedo);
        toolbar.onAddNode(this::handleAddNode);
        toolbar.onAddEdge(this::handleAddEdge);
        toolbar.onZoomIn(() -> previewPane.zoomIn());
        toolbar.onZoomOut(() -> previewPane.zoomOut());
        toolbar.onZoomReset(() -> previewPane.resetZoom());
        toolbar.onValidate(this::handleValidate);
        toolbar.onTypeSelected(this::handleTypeSelected);

        // Export
        ExportService exportService = new ExportService();
        toolbar.onExportSvg(() -> exportService.exportSvg(stage, previewPane.getLastSvg()));
        toolbar.onExportPng(() -> exportService.exportPng(stage, document.getSource()));
        toolbar.onExportMermaid(() -> exportService.exportMermaid(stage, document.getSource()));
        toolbar.onExportMarkdown(() -> exportService.exportMermaid(stage, document.getSource()));
    }

    // ════════════════════════════════════════════════════════════
    //  Editor events
    // ════════════════════════════════════════════════════════════

    private void wireEditorEvents() {
        // Debounced text change → update document & preview
        codeEditor.onTextChanged(500, newText -> {
            if (suppressEditorSync) return;
            document.updateSource(newText);
            triggerPreviewUpdate();
            updateStatusBar();
        });
    }

    private void wirePreviewEvents() {
        // Click on SVG element → try to find in source and highlight
        previewPane.setOnElementClicked(elementId -> {
            if (elementId == null || elementId.isBlank()) return;

            String source = document.getSource();
            String[] lines = source.split("\\n");
            for (int i = 0; i < lines.length; i++) {
                // Simple heuristic: check if the element ID appears in this line
                if (lines[i].contains(elementId)) {
                    codeEditor.goToLine(i);
                    setStatus("Element gefunden: " + elementId + " (Zeile " + (i + 1) + ")");
                    return;
                }
            }
            setStatus("Element: " + elementId);
        });
    }

    private void wireKeyboardShortcuts(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                this::handleNew);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                this::handleOpen);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                this::handleSave);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                this::handleSaveAs);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                this::handleUndo);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
                this::handleRedo);
    }

    // ════════════════════════════════════════════════════════════
    //  Action handlers
    // ════════════════════════════════════════════════════════════

    private void handleNew() {
        if (document.isModified()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Es gibt ungespeicherte Änderungen. Trotzdem fortfahren?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Neues Diagramm");
            alert.setHeaderText(null);
            if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }
        newDocument(DiagramTemplates.DEFAULT_TEMPLATE);
        syncEditorFromDocument();
        triggerPreviewUpdate();
    }

    private void handleOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Mermaid-Datei öffnen");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Mermaid Dateien", "*.mmd", "*.mermaid"),
                new FileChooser.ExtensionFilter("Alle Dateien", "*.*"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            document = repository.load(file.toPath());
            rewireDocument();
            syncEditorFromDocument();
            triggerPreviewUpdate();
            setStatus("Geöffnet: " + file.getName());
        } catch (Exception e) {
            showError("Fehler beim Öffnen", e.getMessage());
        }
    }

    private void handleSave() {
        if (document.getFilePath() != null) {
            try {
                repository.save(document, document.getFilePath());
                setStatus("Gespeichert: " + document.getFilePath().getFileName());
                updateTitle();
            } catch (Exception e) {
                showError("Fehler beim Speichern", e.getMessage());
            }
        } else {
            handleSaveAs();
        }
    }

    private void handleSaveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Speichern als…");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Mermaid Dateien", "*.mmd"),
                new FileChooser.ExtensionFilter("Alle Dateien", "*.*"));
        chooser.setInitialFileName("diagramm.mmd");
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;

        try {
            repository.save(document, file.toPath());
            setStatus("Gespeichert: " + file.getName());
            updateTitle();
        } catch (Exception e) {
            showError("Fehler beim Speichern", e.getMessage());
        }
    }

    private void handleUndo() {
        if (document.canUndo()) {
            document.undo();
            syncEditorFromDocument();
            triggerPreviewUpdate();
            setStatus("Rückgängig gemacht");
        }
    }

    private void handleRedo() {
        if (document.canRedo()) {
            document.redo();
            syncEditorFromDocument();
            triggerPreviewUpdate();
            setStatus("Wiederholt");
        }
    }

    private void handleAddNode() {
        TextInputDialog dialog = new TextInputDialog("NeuerKnoten");
        dialog.setTitle("Knoten hinzufügen");
        dialog.setHeaderText("Neuen Knoten zum Diagramm hinzufügen");
        dialog.setContentText("Knoten-ID:");

        dialog.showAndWait().ifPresent(id -> {
            if (id.isBlank()) return;

            TextInputDialog labelDialog = new TextInputDialog(id);
            labelDialog.setTitle("Knoten-Label");
            labelDialog.setHeaderText("Anzeigename für den Knoten");
            labelDialog.setContentText("Label:");

            labelDialog.showAndWait().ifPresent(label -> {
                editorUseCase.addNode(id.trim(), label.trim());
                syncEditorFromDocument();
                triggerPreviewUpdate();
                setStatus("Knoten hinzugefügt: " + id);
            });
        });
    }

    private void handleAddEdge() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Verbindung hinzufügen");
        dialog.setHeaderText("Neue Verbindung zwischen Knoten");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fromField = new TextField();
        fromField.setPromptText("Von (ID)");
        TextField toField = new TextField();
        toField.setPromptText("Nach (ID)");
        TextField labelField = new TextField();
        labelField.setPromptText("Label (optional)");

        VBox content = new VBox(8, new Label("Von:"), fromField,
                new Label("Nach:"), toField,
                new Label("Label:"), labelField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new String[]{fromField.getText(), toField.getText(), labelField.getText()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result[0].isBlank() || result[1].isBlank()) return;
            editorUseCase.addEdge(result[0].trim(), result[1].trim(), result[2].trim());
            syncEditorFromDocument();
            triggerPreviewUpdate();
            setStatus("Verbindung hinzugefügt: " + result[0] + " → " + result[1]);
        });
    }

    private void handleValidate() {
        setStatus("Validierung läuft…");
        CompletableFuture.runAsync(() -> {
            List<ValidationError> errors = editorUseCase.validate();
            Platform.runLater(() -> {
                if (errors.isEmpty()) {
                    setStatus("✓ Diagramm ist valide");
                    showInfo("Validierung", "Das Diagramm ist syntaktisch korrekt.");
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (ValidationError err : errors) {
                        sb.append(err.getSeverity()).append(": ").append(err.getMessage()).append("\n");
                    }
                    setStatus("✗ " + errors.size() + " Problem(e) gefunden");
                    showWarning("Validierungsergebnis", sb.toString());
                }
            });
        });
    }

    private void handleTypeSelected(DiagramType type) {
        if (document.isModified()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Soll die aktuelle Vorlage ersetzt werden? Ungespeicherte Änderungen gehen verloren.",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Vorlage laden");
            if (alert.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }
        String template = DiagramTemplates.getTemplate(type);
        newDocument(template);
        syncEditorFromDocument();
        triggerPreviewUpdate();
        setStatus("Vorlage geladen: " + type.getDisplayName());
    }

    // ════════════════════════════════════════════════════════════
    //  Preview rendering
    // ════════════════════════════════════════════════════════════

    private void triggerPreviewUpdate() {
        String source = document.getSource();
        if (source.isBlank()) {
            previewPane.showSvg(null);
            return;
        }

        setStatus("Rendering…");

        CompletableFuture.supplyAsync(() -> previewService.renderDetailed(source))
                .thenAcceptAsync(result -> {
                    if (result.success()) {
                        previewPane.showSvg(result.svg());
                        setStatus("✓ Vorschau aktualisiert — " + document.getDiagramType().getDisplayName());
                    } else {
                        previewPane.showError(result.errorMessage());
                        setStatus("✗ Render-Fehler");
                    }
                    updateStatusBar();
                }, Platform::runLater);
    }

    // ════════════════════════════════════════════════════════════
    //  Document management
    // ════════════════════════════════════════════════════════════

    private void newDocument(String initialSource) {
        document = new DiagramDocument(initialSource);
        rewireDocument();
    }

    private void rewireDocument() {
        editorUseCase = new DiagramEditorUseCase(document, snippetFactory, validationService);
        document.addChangeListener(doc -> Platform.runLater(this::updateTitle));
        updateTitle();
        updateStatusBar();
    }

    private void syncEditorFromDocument() {
        suppressEditorSync = true;
        codeEditor.setText(document.getSource());
        suppressEditorSync = false;
    }

    // ════════════════════════════════════════════════════════════
    //  UI helpers
    // ════════════════════════════════════════════════════════════

    private void updateTitle() {
        stage.setTitle("Mermaid Designer — " + document.getTitle());
    }

    private void updateStatusBar() {
        Platform.runLater(() -> {
            toolbar.setUndoEnabled(document.canUndo());
            toolbar.setRedoEnabled(document.canRedo());
            toolbar.setDiagramType(document.getDiagramType());
            typeLabel.setText("Typ: " + document.getDiagramType().getDisplayName());
        });
    }

    private void setStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
