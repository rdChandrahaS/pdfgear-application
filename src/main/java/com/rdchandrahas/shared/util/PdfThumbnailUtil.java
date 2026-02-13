package com.rdchandrahas.shared.util;

import com.rdchandrahas.core.ExecutionManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Set;
import java.util.function.Consumer;

public class PdfThumbnailUtil {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif"
    );

    public static Image generateThumbnail(File file) {
        String filename = file.getName().toLowerCase();

        if (isImage(filename)) {
            try (FileInputStream fis = new FileInputStream(file)) {
                return new Image(fis, 200, 0, true, true);
            } catch (Exception e) {
                return null;
            }
        }

        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 72);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            return null;
        }
    }

    public static void loadThumbnailAsync(String filePath, Consumer<Image> callback) {
        ExecutionManager.submit(() -> {
            File file = new File(filePath);
            
            // FIX: Use getAbsolutePath() for the cache key, not the File object
            Image thumbnail = ThumbnailCache.get(file.getAbsolutePath());

            if (thumbnail == null) {
                thumbnail = generateThumbnail(file);
                if (thumbnail != null) {
                    // FIX: Use getAbsolutePath() here too
                    ThumbnailCache.put(file.getAbsolutePath(), thumbnail);
                }
            }

            final Image result = thumbnail;
            Platform.runLater(() -> callback.accept(result));
        });
    }

    private static boolean isImage(String filename) {
        for (String ext : IMAGE_EXTENSIONS) {
            if (filename.endsWith(ext)) return true;
        }
        return false;
    }
}