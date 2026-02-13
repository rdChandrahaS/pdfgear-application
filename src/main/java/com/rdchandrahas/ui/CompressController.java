package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.*;
import org.apache.pdfbox.pdmodel.PDDocument;

public class CompressController extends BaseToolController {
    private TextField dpi, qual;
    @Override protected void onInitialize() {
        setTitle("Compress PDF");
        setActionText("Compress & Save");
        dpi = new TextField("150"); dpi.setPrefWidth(60);
        qual = new TextField("60"); qual.setPrefWidth(50);
        addToolbarItem(new Label("DPI:"), dpi, new Label("Qual:"), qual);
    }
    @Override protected void handleAddFiles() { addFiles("PDF Files", "*.pdf"); }
    @Override protected void handleAction() {
        processWithSaveDialog("Save Compressed", "Compressed.pdf", (dest) -> {
            try (PDDocument doc = loadDocumentSafe(fileListView.getItems().get(0).getPath())) {
                doc.save(dest); // Add custom image compression logic here if needed
            }
        });
    }
}