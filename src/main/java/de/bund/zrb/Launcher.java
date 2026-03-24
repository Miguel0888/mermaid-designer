package de.bund.zrb;

/**
 * Indirection launcher that does NOT extend {@link javafx.application.Application}.
 * <p>
 * When the JVM sees a main class that extends Application, it checks for
 * JavaFX modules on the module-path. On the classpath (which is how Gradle
 * and IntelliJ typically run), that check fails with
 * "JavaFX runtime components are missing".
 * <p>
 * This launcher bypasses that check by being a plain class.
 */
public class Launcher {

    public static void main(String[] args) {
        MermaidDesignerApp.main(args);
    }
}
