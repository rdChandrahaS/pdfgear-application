package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.shared.model.FileItem;
import javafx.scene.control.*;
import org.apache.pdfbox.pdmodel.PDDocument;

public class UnlockController extends BaseToolController {
    
    private PasswordField pass;

    @Override 
    protected void onInitialize() {
        setTitle("Unlock PDF");
        setActionText("Unlock & Save");
        
        pass = new PasswordField(); 
        pass.setPromptText("Enter Current Password");
        pass.setPrefWidth(200);
        
        // This makes the button update instantly as the user types the password
        pass.textProperty().addListener((obs, oldVal, newVal) -> updateActionBtnState());
        
        addToolbarItem(new Label("Password:"), pass);
    }

    @Override 
    protected void handleAddFiles() { 
        addFiles("PDF Files", "*.pdf"); 
    }

    @Override 
    protected void handleAction() {
        processWithSaveDialog("Save Unlocked PDF", "Unlocked.pdf", (dest) -> {
            String sourcePath = fileListView.getItems().get(0).getPath();
            
            try (PDDocument doc = loadDocumentSafe(sourcePath, pass.getText())) {
                doc.setAllSecurityToBeRemoved(true); 
                doc.save(dest);
            }
        });
    }

    @Override
    protected boolean isInputValid() {
        // 1. Must have at least one file AND password cannot be empty
        if (fileListView.getItems().isEmpty() || pass.getText().trim().isEmpty()) {
            return false;
        }
        
        // 2. Ensure all items are actually PDFs
        for (FileItem item : fileListView.getItems()) {
            if (!item.getPath().toLowerCase().endsWith(".pdf")) {
                return false;
            }
        }
        
        return true;
    }
}