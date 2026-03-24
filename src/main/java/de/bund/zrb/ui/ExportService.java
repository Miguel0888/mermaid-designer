package de.bund.zrb.ui;

import com.aresstack.Mermaid;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles exporting diagrams to various file formats.
 */
public class ExportService {

    private static final Logger LOG = Logger.getLogger(ExportService.class.getName());

    /**
     * Export the rendered SVG to a .svg file via FileChooser.
     */
    public boolean exportSvg(Window owner, String svg) {
        if (svg == null || svg.isBlank()) return false;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("SVG exportieren");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SVG Dateien", "*.svg"));
        chooser.setInitialFileName("diagramm.svg");

        File file = chooser.showSaveDialog(owner);
        if (file == null) return false;

        try {
            Files.writeString(file.toPath(), svg, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "SVG export failed", e);
            return false;
        }
    }

    /**
     * Export the diagram as a PNG file using mermaid-java's image rendering.
     */
    public boolean exportPng(Window owner, String mermaidSource) {
        if (mermaidSource == null || mermaidSource.isBlank()) return false;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("PNG exportieren");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Dateien", "*.png"));
        chooser.setInitialFileName("diagramm.png");

        File file = chooser.showSaveDialog(owner);
        if (file == null) return false;

        try {
            BufferedImage img = Mermaid.renderToImage(mermaidSource, 2400);
            if (img == null) {
                LOG.warning("PNG export: renderToImage returned null");
                return false;
            }
            ImageIO.write(img, "png", file);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "PNG export failed", e);
            return false;
        }
    }

    /**
     * Export the Mermaid source text to a .mmd file.
     */
    public boolean exportMermaid(Window owner, String source) {
        if (source == null || source.isBlank()) return false;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Mermaid-Datei exportieren");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Mermaid Dateien", "*.mmd", "*.mermaid"),
                new FileChooser.ExtensionFilter("Markdown", "*.md"),
                new FileChooser.ExtensionFilter("Alle Dateien", "*.*"));
        chooser.setInitialFileName("diagramm.mmd");

        File file = chooser.showSaveDialog(owner);
        if (file == null) return false;

        try {
            String content = source;
            // If exporting as Markdown, wrap in mermaid code block
            if (file.getName().endsWith(".md")) {
                content = "```mermaid\n" + source + "\n```\n";
            }
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Mermaid export failed", e);
            return false;
        }
    }
}
