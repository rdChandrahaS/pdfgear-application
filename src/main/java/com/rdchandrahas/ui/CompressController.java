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

public class CompressController extends BaseToolController {

    private ComboBox<String> modeComboBox;
    private TextField valueInput;
    private ComboBox<String> unitComboBox;

    @Override
    protected void onInitialize() {
        setTitle("Compress PDF");
        setActionText("Compress & Save");

        modeComboBox = new ComboBox<>();
        modeComboBox.getItems().addAll("By Percentage", "By Target Size");
        modeComboBox.getSelectionModel().selectFirst();

        valueInput = new TextField();
        valueInput.setPromptText("Enter percentage (e.g., 50)");
        valueInput.setPrefWidth(180);

        unitComboBox = new ComboBox<>();
        unitComboBox.getItems().addAll("%");
        unitComboBox.getSelectionModel().selectFirst();

        // Dynamically change the UI based on the mode selected
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

        addToolbarItem(modeComboBox, valueInput, unitComboBox);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        if (valueInput.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please enter a compression value.");
            return;
        }

        double inputValue;
        try {
            inputValue = Double.parseDouble(valueInput.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
            return;
        }

        processWithSaveDialog("Save Compressed PDF", "compressed_document.pdf", (destination) -> {
            // Processing only the first file in this example for exact size targeting.
            // If there are multiple, they are merged first.
            FileItem firstItem = (FileItem) fileListView.getItems().get(0);
            File sourceFile = new File(firstItem.getPath());

            long originalSizeBytes = sourceFile.length();
            long targetSizeBytes;

            if (modeComboBox.getValue().equals("By Percentage")) {
                // E.g., user enters 40%. The target is 60% of original size.
                if (inputValue <= 0 || inputValue >= 100) {
                    throw new IllegalArgumentException("Percentage must be between 1 and 99.");
                }
                double factor = 1.0 - (inputValue / 100.0);
                targetSizeBytes = (long) (originalSizeBytes * factor);
            } else {
                // By Target Size
                if (unitComboBox.getValue().equals("MB")) {
                    targetSizeBytes = (long) (inputValue * 1024 * 1024);
                } else {
                    targetSizeBytes = (long) (inputValue * 1024);
                }
            }

            if (targetSizeBytes >= originalSizeBytes) {
                // If target is larger than original, just copy it to save time
                Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Notice", 
                    "The original file is already smaller than your target size. File copied as-is."));
                return;
            }

            // Execute Iterative Compression Engine
            executeIterativeCompression(sourceFile, destination, targetSizeBytes);
        });
    }

    /**
     * Iterative engine that attempts to hit the target size by progressively 
     * lowering image quality and scaling down dimensions.
     */
    private void executeIterativeCompression(File sourceFile, File destination, long targetSizeBytes) throws Exception {
        byte[] bestResult = null;
        
        // Define our compression steps: { jpegQuality, imageScaleFactor }
        float[][] strategies = {
            {0.8f, 1.0f}, // High quality, original size
            {0.6f, 1.0f}, // Medium quality, original size
            {0.4f, 0.8f}, // Low quality, slightly shrunk dimensions
            {0.2f, 0.5f}, // Very low quality, half dimensions
            {0.1f, 0.3f}  // Extreme compression, tiny dimensions
        };

        for (float[] strategy : strategies) {
            float quality = strategy[0];
            float scale = strategy[1];

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 PDDocument doc = loadDocumentSafe(sourceFile.getAbsolutePath())) {
                
                compressImagesInDocument(doc, quality, scale);
                doc.save(baos);
                
                bestResult = baos.toByteArray();
                
                // If we successfully hit the target size, stop iterating!
                if (bestResult.length <= targetSizeBytes) {
                    break;
                }
            }
        }

        // Write the best achievable result to the destination
        if (bestResult != null) {
            Files.write(destination.toPath(), bestResult);
        } else {
            throw new Exception("Failed to process document.");
        }
    }

    private void compressImagesInDocument(PDDocument doc, float quality, float scaleFactor) throws Exception {
        for (PDPage page : doc.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) continue;

            for (COSName name : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(name);
                
                if (xObject instanceof PDImageXObject pdImage) {
                    BufferedImage bImage = pdImage.getImage();
                    if (bImage == null) continue;

                    // Scale image dimensions down if needed
                    int newWidth = (int) (bImage.getWidth() * scaleFactor);
                    int newHeight = (int) (bImage.getHeight() * scaleFactor);
                    
                    if (newWidth < 10 || newHeight < 10) continue; // Prevent collapsing to zero

                    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = resizedImage.createGraphics();
                    
                    // Fast scaling for performance
                    g2d.drawImage(bImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST), 0, 0, null);
                    g2d.dispose();

                    // Re-encode as highly compressed JPEG
                    PDImageXObject compressedImage = JPEGFactory.createFromImage(doc, resizedImage, quality);
                    
                    // Replace the bloated image with the compressed one in the document
                    resources.put(name, compressedImage);
                }
            }
        }
    }
}