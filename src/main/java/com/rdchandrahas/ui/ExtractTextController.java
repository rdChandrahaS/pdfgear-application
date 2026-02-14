package com.rdchandrahas.ui;

import com.rdchandrahas.core.ExecutionManager;
import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileWriter;

public class ExtractTextController extends BaseToolController {

    @Override
    protected void onInitialize() {
        setTitle("Extract Text from PDF");
        setActionText("Extract & Save");
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        if (fileListView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Files", "Please add a PDF file first.");
            return;
        }

        // Custom FileChooser because we are saving a .txt file, not a .pdf
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Extracted Text");
        chooser.setInitialFileName("extracted_text.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File dest = chooser.showSaveDialog(actionBtn.getScene().getWindow());

        if (dest == null) return;

        setBusy(true, actionBtn);

        ExecutionManager.submit(() -> {
            try (FileWriter writer = new FileWriter(dest)) {
                
                PDFTextStripper textStripper = new PDFTextStripper();
                
                for (Object obj : fileListView.getItems()) {
                    FileItem item = (FileItem) obj;
                    
                    // Add a header if merging multiple files
                    if (fileListView.getItems().size() > 1) {
                        writer.write("\n\n--- Document: " + new File(item.getPath()).getName() + " ---\n\n");
                    }

                    // Enterprise memory safety: use BaseToolController's safe loader
                    try (PDDocument doc = loadDocumentSafe(item.getPath())) {
                        String text = textStripper.getText(doc);
                        writer.write(text);
                    }
                }

                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Text extracted and saved successfully!");
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.ERROR, "Error", "Extraction failed: " + e.getMessage());
                });
            }
        });
    }
}