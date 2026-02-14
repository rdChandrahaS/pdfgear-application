package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import java.util.List;

public class MergeController extends BaseToolController {
    @Override
    protected void onInitialize() {
        setTitle("Merge PDFs");
        setActionText("Merge & Save");
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save Merged PDF", "Merged.pdf", (dest) -> {
            List<String> paths = fileListView.getItems().stream().map(FileItem::getPath).toList();
            mergeDocumentsSafe(paths, dest);
        });
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().size() < 2) {
            return false; // Needs at least 2 files
        }
        for (FileItem item : fileListView.getItems()) {
            if (!item.getPath().toLowerCase().endsWith(".pdf")) {
                return false;
            }
        }
        return true;
    }

}