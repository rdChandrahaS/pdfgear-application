package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import org.apache.pdfbox.pdmodel.PDDocument;

// REMOVED: unused import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SplitController extends BaseToolController {

    private static final Logger LOGGER = Logger.getLogger(SplitController.class.getName());

    @Override
    protected void onInitialize() {
        setTitle("Split PDF");
        setActionText("Split & Save");
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save Split Files", "Split_Output.pdf", (dest) -> {

            String base = dest.getAbsolutePath().replaceAll("(?i)\\.pdf$", "");   

            try (PDDocument doc = loadDocumentSafe(fileListView.getItems().get(0).getPath())) {                
                
                int totalPages = doc.getNumberOfPages();
                LOGGER.log(Level.INFO, "Starting PDF split for {0} pages...", totalPages);
                
                // Extract, save, and instantly close one page at a time.
                // Keeps RAM usage completely flat regardless of PDF size.
                for (int i = 0; i < totalPages; i++) {
                    try (PDDocument singlePageDoc = createDocumentSafe()) {
                        singlePageDoc.addPage(singlePageDoc.importPage(doc.getPage(i)));
                        singlePageDoc.save(base + "_" + (i + 1) + ".pdf");
                    }
                    
                    // Log progress every 100 pages so we don't spam the console
                    if ((i + 1) % 100 == 0) {
                        LOGGER.log(Level.INFO, "Successfully split {0} pages...", (i + 1));
                    }
                }
                
                LOGGER.info("PDF split operation completed successfully.");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An error occurred during the split operation.", e);
                throw e; // Rethrow to let BaseToolController show the error popup to the user
            }
        });
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty()) {
            return false;
        }
        for (FileItem item : fileListView.getItems()) {
            if (!item.getPath().toLowerCase().endsWith(".pdf")) {
                return false;
            }
        }
        return true;
    }
}