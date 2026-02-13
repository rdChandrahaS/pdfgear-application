package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.*;
import org.apache.pdfbox.pdmodel.PDDocument;

public class UnlockController extends BaseToolController {
    private PasswordField pass;
    @Override protected void onInitialize() {
        setTitle("Unlock PDF");
        setActionText("Unlock & Save");
        pass = new PasswordField(); pass.setPromptText("Password");
        addToolbarItem(new Label("Password:"), pass);
    }
    @Override protected void handleAddFiles() { addFiles("PDF Files", "*.pdf"); }
    @Override protected void handleAction() {
        processWithSaveDialog("Save Unlocked", "Unlocked.pdf", (dest) -> {
            try (PDDocument doc = loadDocumentSafe(fileListView.getItems().get(0).getPath(), pass.getText())) {
                doc.setAllSecurityToBeRemoved(true); doc.save(dest);
            }
        });
    }
}