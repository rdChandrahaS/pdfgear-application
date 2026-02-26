package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.core.ImageToPdfService;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.List;

public class ImageToPdfController extends BaseToolController {
    private ComboBox<String> layout;

    @Override
    protected void onInitialize() {
        setTitle("Image to PDF");
        setActionText("Convert & Save");
        layout = new ComboBox<>();
        layout.getItems().addAll("Portrait", "Landscape", "Original");
        layout.setValue("Portrait");
        addToolbarItem(new Label("Layout:"), layout);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("Images", "*.jpg", "*.png", "*.webp", "*.jpeg");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save PDF", "Images.pdf", (dest) -> {
            List<String> imagePaths = fileListView.getItems().stream()
                    .map(FileItem::getPath)
                    .toList();
            
            ImageToPdfService service = new ImageToPdfService();
            try {
                service.convertImagesToPdf(imagePaths, dest.getAbsolutePath());
            } catch (Exception e) {
                throw new IOException("Failed to convert images to PDF: " + e.getMessage(), e);
            }
        });
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty()) {
            return false;
        }
        return fileListView.getItems().stream().allMatch(item -> {
            String path = item.getPath().toLowerCase();
            return path.endsWith(".jpg") || path.endsWith(".jpeg") || 
                   path.endsWith(".png") || path.endsWith(".webp");
        });
    }
}