package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.io.File;

public class ImageToPdfController extends BaseToolController {
    private ComboBox<String> layout;
    @Override protected void onInitialize() {
        setTitle("Image to PDF");
        setActionText("Convert & Save");
        layout = new ComboBox<>(); layout.getItems().addAll("Portrait", "Landscape", "Original");
        layout.setValue("Portrait");
        addToolbarItem(new Label("Layout:"), layout);
    }
    @Override protected void handleAddFiles() { addFiles("Images", "*.jpg", "*.png", "*.webp"); }
    @Override protected void handleAction() {
        processWithSaveDialog("Save PDF", "Images.pdf", (dest) -> {
            try (PDDocument doc = createDocumentSafe()) {
                for (FileItem item : fileListView.getItems()) {
                    PDImageXObject img = PDImageXObject.createFromFile(item.getPath(), doc);
                    PDPage page = new PDPage(PDRectangle.A4); doc.addPage(page);
                    try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                        cs.drawImage(img, 20, 20, 550, 800);
                    }
                }
                doc.save(dest);
            }
        });
    }
}