package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * CompressController manages the logic for reducing PDF file sizes.
 * It features an iterative compression engine that progressively adjust image
 * quality and dimensions to meet a specific target size or percentage.
 */
public class CompressController extends BaseToolController {

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
        modeComboBox.getItems().addAll("By Percentage", "By Target Size");
        modeComboBox.getSelectionModel().selectFirst();

        valueInput = new TextField();
        valueInput.setPromptText("Enter percentage (e.g., 50)");
        valueInput.setPrefWidth(180);

        unitComboBox = new ComboBox<>();
        unitComboBox.getItems().addAll("%");
        unitComboBox.getSelectionModel().selectFirst();

        // Dynamically change the UI behavior based on the mode selected
        modeComboBox.setOnAction(e -> {
            if (modeComboBox.getValue().equals("By Percentage")) {
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
            if (modeComboBox.getValue().equals("By Percentage")) {
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
     */
    private void executeIterativeCompression(File sourceFile, File destination, long targetSizeBytes) throws Exception {
        byte[] bestResult = null;

        // Strategy matrix: { JPEG Quality (0.0-1.0), Image Scale Factor (0.0-1.0) }
        float[][] strategies = {
                { 0.8f, 1.0f }, // High quality, original dimensions
                { 0.6f, 1.0f }, // Medium quality, original dimensions
                { 0.4f, 0.8f }, // Low quality, 80% dimensions
                { 0.2f, 0.5f }, // Very low quality, 50% dimensions
                { 0.1f, 0.3f } // Extreme compression
        };

        for (float[] strategy : strategies) {
            float quality = strategy[0];
            float scale = strategy[1];

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PDDocument doc = loadDocumentSafe(sourceFile.getAbsolutePath())) {

                compressImagesInDocument(doc, quality, scale);
                doc.save(baos);

                bestResult = baos.toByteArray();

                // Exit early if we hit the user's target size
                if (bestResult.length <= targetSizeBytes) {
                    break;
                }
            }
        }

        if (bestResult != null) {
            Files.write(destination.toPath(), bestResult);
        } else {
            throw new Exception("Failed to process document.");
        }
    }

    /**
     * Traverses the PDF resources to find and re-encode images with new quality and
     * scale.
     */
    private void compressImagesInDocument(PDDocument doc, float quality, float scaleFactor) throws Exception {
        for (PDPage page : doc.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null)
                continue;

            for (COSName name : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(name);

                if (xObject instanceof PDImageXObject pdImage) {
                    BufferedImage bImage = pdImage.getImage();
                    if (bImage == null)
                        continue;

                    // Calculate new dimensions
                    int newWidth = (int) (bImage.getWidth() * scaleFactor);
                    int newHeight = (int) (bImage.getHeight() * scaleFactor);

                    if (newWidth < 10 || newHeight < 10)
                        continue;

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