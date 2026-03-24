package de.bund.zrb;

import de.bund.zrb.ui.MainViewController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the Mermaid Designer application.
 * <p>
 * A JavaFX-based Mermaid diagram editor with:
 * <ul>
 *   <li>Syntax-highlighted text editor (left)</li>
 *   <li>Live SVG preview (right)</li>
 *   <li>Toolbar with diagram templates, editing assistants, and export</li>
 *   <li>Undo/Redo support</li>
 *   <li>Click-on-preview → jump-to-source synchronisation</li>
 *   <li>Validation via Mermaid rendering engine</li>
 * </ul>
 * <p>
 * Architecture: Clean Architecture with Domain, Use Case, Infrastructure and UI layers.
 * Rendering is delegated to <a href="https://github.com/aresstack/mermaid-java">mermaid-java</a>.
 */
public class MermaidDesignerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // The MainViewController wires all layers together and builds the scene
        new MainViewController(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
