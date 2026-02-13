package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

public class ProtectController extends BaseToolController {
    private PasswordField pass;
    @Override protected void onInitialize() {
        setTitle("Protect PDF");
        setActionText("Encrypt & Save");
        pass = new PasswordField(); pass.setPromptText("Password");
        addToolbarItem(new Label("Password:"), pass);
    }
    @Override protected void handleAddFiles() { addFiles("PDF Files", "*.pdf"); }
    @Override protected void handleAction() {
        String p = pass.getText();
        if (p.isEmpty()) return;
        processWithSaveDialog("Save Protected", "Protected.pdf", (dest) -> {
            try (PDDocument doc = loadDocumentSafe(fileListView.getItems().get(0).getPath())) {
                StandardProtectionPolicy spp = new StandardProtectionPolicy(p, p, new AccessPermission());
                doc.protect(spp); doc.save(dest);
            }
        });
    }
}