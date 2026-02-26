package com.rdchandrahas.shared.util;

import com.rdchandrahas.core.ExecutionManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class PdfThumbnailUtil {

    private PdfThumbnailUtil() {
        throw new IllegalStateException("Utility class");
    }
    
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif"
    );

    public static Image generateThumbnail(File file) {
        String filename = file.getName().toLowerCase();

        if (isImage(filename)) {
            try {
                // Attempt native fast-loading (works for JPG, PNG)
                Image img = new Image(file.toURI().toString(), 200, 0, true, true);
                if (!img.isError()) {
                    return img;
                }
                
                // Fallback for WEBP and others: Read using ImageIO
                BufferedImage bImg = ImageIO.read(file);
                if (bImg != null) {
                    // FIX: Force scale down the massive fallback image to 200px to save RAM
                    int targetWidth = 200;
                    int targetHeight = (int) (bImg.getHeight() * ((double) targetWidth / bImg.getWidth()));
                    
                    BufferedImage resizedImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = resizedImg.createGraphics();
                    g2d.drawImage(bImg.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_FAST), 0, 0, null);
                    g2d.dispose();
                    
                    bImg.flush(); // Instantly free the huge original image from RAM

                    return SwingFXUtils.toFXImage(resizedImg, null);
                }
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

    /**
     * Loads a thumbnail asynchronously with task cancellation support.
     * Prevents processing thumbnails for cells that have already scrolled off-screen.
     */
    public static void loadThumbnailAsync(String filePath, BooleanSupplier isCancelled, Consumer<Image> callback) {
        ExecutionManager.submit(() -> {
            // 1. Abort immediately if the user already scrolled past this cell
            if (isCancelled.getAsBoolean()) return; 
            
            File file = new File(filePath);
            Image thumbnail = ThumbnailCache.get(file.getAbsolutePath());

            if (thumbnail == null) {
                // 2. Abort before reading the file from the hard drive (Saves CPU/Disk I/O)
                if (isCancelled.getAsBoolean()) return; 
                
                thumbnail = generateThumbnail(file);
                
                if (thumbnail != null) {
                    ThumbnailCache.put(file.getAbsolutePath(), thumbnail);
                }
            }

            // 3. Abort before pushing to the UI thread
            if (isCancelled.getAsBoolean()) return; 

            final Image result = thumbnail;
            Platform.runLater(() -> callback.accept(result));
        });
    }

    /**
     * Overloaded fallback method. 
     * Keeps backward compatibility for other code that doesn't need task cancellation.
     */
    public static void loadThumbnailAsync(String filePath, Consumer<Image> callback) {
        loadThumbnailAsync(filePath, () -> false, callback);
    }

    private static boolean isImage(String filename) {
        for (String ext : IMAGE_EXTENSIONS) {
            if (filename.endsWith(ext)) return true;
        }
        return false;
    }
}