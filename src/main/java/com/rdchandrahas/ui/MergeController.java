package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import java.util.List;

public class MergeController extends BaseToolController {
    @Override protected void onInitialize() {
        setTitle("Merge PDFs");
        setActionText("Merge & Save");
    }
    @Override protected void handleAddFiles() { addFiles("PDF Files", "*.pdf"); }
    @Override protected void handleAction() {
        processWithSaveDialog("Save Merged PDF", "Merged.pdf", (dest) -> {
            List<String> paths = fileListView.getItems().stream().map(FileItem::getPath).toList();
            mergeDocumentsSafe(paths, dest);
        });
    }
}