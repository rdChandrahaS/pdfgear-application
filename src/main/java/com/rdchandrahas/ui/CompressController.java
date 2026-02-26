package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.util.TempFileManager;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CompressController manages the logic for reducing PDF file sizes.
 * It features an iterative compression engine that progressively adjust image
 * quality and dimensions to meet a specific target size or percentage.
 */
public class CompressController extends BaseToolController {

    private static final Logger LOGGER = Logger.getLogger(CompressController.class.getName());
    private static final String MODE_PERCENTAGE = "By Percentage";

    private ComboBox<String> modeComboBox;
    private TextField valueInput;
    private ComboBox<String> unitComboBox;

    /**
     * Configures the compression-specific toolbar UI, including mode selection
     * (Percentage vs Target Size) and dynamic input prompts.
     */
    @Override
    protected void onInitialize() {
        setTitle("Compress PDF");
        setActionText("Compress & Save");

        // --- UI Component Initialization ---
        modeComboBox = new ComboBox<>();
        modeComboBox.getItems().addAll(MODE_PERCENTAGE, "By Target Size");
        modeComboBox.getSelectionModel().selectFirst();

        valueInput = new TextField();
        valueInput.setPromptText("Enter percentage (e.g., 50)");
        valueInput.setPrefWidth(180);

        unitComboBox = new ComboBox<>();
        unitComboBox.getItems().addAll("%");
        unitComboBox.getSelectionModel().selectFirst();

        // Dynamically change the UI behavior based on the mode selected
        modeComboBox.setOnAction(e -> {
            if (modeComboBox.getValue().equals(MODE_PERCENTAGE)) {
                unitComboBox.getItems().setAll("%");
                unitComboBox.getSelectionModel().selectFirst();
                valueInput.setPromptText("Enter percentage (e.g., 50)");
            } else {
                unitComboBox.getItems().setAll("KB", "MB");
                unitComboBox.getSelectionModel().selectLast(); // Default to MB
                valueInput.setPromptText("Target size (e.g., 5)");
            }
        });

        // Add components to the BaseToolController's custom toolbar area
        addToolbarItem(modeComboBox, valueInput, unitComboBox);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    /**
     * Prepares the compression task by calculating the target size and
     * initiating the iterative processing engine via the save dialog workflow.
     */
    @Override
    protected void handleAction() {
        String input = valueInput.getText().trim();
        if (input.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please enter a compression value.");
            return;
        }

        double inputValue;
        try {
            inputValue = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
            return;
        }

        processWithSaveDialog("Save Compressed PDF", "compressed_document.pdf", (destination) -> {
            // Note: Currently processes the first selected file for precision targeting
            FileItem firstItem = fileListView.getItems().get(0);
            File sourceFile = new File(firstItem.getPath());

            long originalSizeBytes = sourceFile.length();
            long targetSizeBytes;

            // --- Target Calculation ---
            if (modeComboBox.getValue().equals(MODE_PERCENTAGE)) {
                if (inputValue <= 0 || inputValue >= 100) {
                    throw new IllegalArgumentException("Percentage must be between 1 and 99.");
                }
                double factor = 1.0 - (inputValue / 100.0);
                targetSizeBytes = (long) (originalSizeBytes * factor);
            } else {
                targetSizeBytes = unitComboBox.getValue().equals("MB")
                        ? (long) (inputValue * 1024 * 1024)
                        : (long) (inputValue * 1024);
            }

            LOGGER.log(Level.INFO, "Starting compression. Original size: {0} bytes, Target size: {1} bytes", 
                    new Object[]{originalSizeBytes, targetSizeBytes});

            // Optimization: If the file is already under the target, just copy it
            if (targetSizeBytes >= originalSizeBytes) {
                Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Notice",
                        "The original file is already smaller than your target size. File copied as-is."));
                return;
            }

            // Launch the iterative engine
            executeIterativeCompression(sourceFile, destination, targetSizeBytes);
        });
    }

    /**
     * Progressively applies more aggressive compression strategies until
     * the target size is reached or strategies are exhausted.
     * Uses Temporary Disk files to prevent RAM exhaustion.
     */
    /**
     * Progressively applies more aggressive compression strategies until
     * the target size is reached or strategies are exhausted.
     * Uses Temporary Disk files to prevent RAM exhaustion.
     */
    private void executeIterativeCompression(File sourceFile, File destination, long targetSizeBytes) throws IOException {
        File bestResultFile = null;

        float[][] strategies = {
                { 0.8f, 1.0f }, // High quality, original dimensions
                { 0.6f, 1.0f }, // Medium quality, original dimensions
                { 0.4f, 0.8f }, // Low quality, 80% dimensions
                { 0.2f, 0.5f }, // Very low quality, 50% dimensions
                { 0.1f, 0.3f }  // Extreme compression
        };

        // ADDED THE TRY BLOCK HERE
        try {
            for (int i = 0; i < strategies.length; i++) {
                float quality = strategies[i][0];
                float scale = strategies[i][1];
                
                LOGGER.log(Level.INFO, "Attempting compression strategy {0}: Quality={1}, Scale={2}", 
                        new Object[]{i + 1, quality, scale});

                File tempAttempt = TempFileManager.createTempFile("compress_attempt_", ".pdf");

                try (PDDocument doc = loadDocumentSafe(sourceFile.getAbsolutePath())) {
                    compressImagesInDocument(doc, quality, scale);
                    doc.save(tempAttempt);
                }

                long attemptSize = tempAttempt.length();
                LOGGER.log(Level.INFO, "Strategy {0} resulted in size: {1} bytes", new Object[]{i + 1, attemptSize});
                
                // Track the smallest file in case we never hit the target
                if (bestResultFile == null || attemptSize < bestResultFile.length()) {
                    if (bestResultFile != null && !bestResultFile.delete()) {
                        LOGGER.log(Level.WARNING, "Failed to delete old best result file");
                    }
                    bestResultFile = tempAttempt;
                } else {
                    if (!tempAttempt.delete()) {
                        LOGGER.log(Level.WARNING, "Failed to delete temporary attempt file");
                    }
                }

                if (attemptSize <= targetSizeBytes) {
                    LOGGER.info("Target size reached! Stopping iterative compression.");
                    break; // Target reached
                }
            }

            if (bestResultFile != null) {
                Files.copy(bestResultFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Compression complete. Final file saved.");
            } else {
                throw new IOException("Failed to process document.");
            }
            
        } finally {
            // THE FINALLY BLOCK GUARANTEES CLEANUP!
            // If the code crashes at any point, or finishes successfully, we ensure the temp file is deleted.
            if (bestResultFile != null && bestResultFile.exists()) {
                if (!bestResultFile.delete()) {
                    LOGGER.log(Level.WARNING, "Failed to clean up best result temp file from the temp directory.");
                }
            }
        }
    }

    /**
     * Traverses the PDF resources to find and re-encode images with new quality and scale.
     */
    private void compressImagesInDocument(PDDocument doc, float quality, float scaleFactor) throws IOException {
        for (PDPage page : doc.getPages()) {
            PDResources resources = page.getResources();
            if (resources != null) {
                processPageResources(doc, resources, quality, scaleFactor);
            }
        }
    }

    /**
     * Extracted helper method to reduce cognitive complexity.
     */
    private void processPageResources(PDDocument doc, PDResources resources, float quality, float scaleFactor) throws IOException {
        for (COSName name : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(name);
            if (xObject instanceof PDImageXObject pdImage) {
                compressSingleImage(doc, resources, name, pdImage, quality, scaleFactor);
            }
        }
    }

    /**
     * Extracted helper method to isolate the image processing logic and eliminate continue statements.
     */
    private void compressSingleImage(PDDocument doc, PDResources resources, COSName name, PDImageXObject pdImage, float quality, float scaleFactor) throws IOException {
        BufferedImage bImage = pdImage.getImage();
        if (bImage != null) {
            // Calculate new dimensions
            int newWidth = (int) (bImage.getWidth() * scaleFactor);
            int newHeight = (int) (bImage.getHeight() * scaleFactor);

            if (newWidth >= 10 && newHeight >= 10) {
                // Resize using AWT
                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resizedImage.createGraphics();
                g2d.drawImage(bImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST), 0, 0, null);
                g2d.dispose();

                // Re-encode as a compressed JPEG XObject
                PDImageXObject compressedImage = JPEGFactory.createFromImage(doc, resizedImage, quality);

                // Replace the original resource in the PDF page
                resources.put(name, compressedImage);
            }
        }
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty()) {
            return false;
        }
        // Check if ALL files are actually PDFs
        for (FileItem item : fileListView.getItems()) {
            if (!item.getPath().toLowerCase().endsWith(".pdf")) {
                return false;
            }
        }
        return true;
    }
}