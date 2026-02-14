package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import java.util.List;

public class SplitController extends BaseToolController {
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
            String base = dest.getAbsolutePath().replace(".pdf", "");
            try (PDDocument doc = loadDocumentSafe(fileListView.getItems().get(0).getPath())) {
                Splitter s = new Splitter();
                List<PDDocument> pages = s.split(doc);
                for (int i = 0; i < pages.size(); i++) {
                    try (PDDocument p = pages.get(i)) {
                        p.save(base + "_" + (i + 1) + ".pdf");
                    }
                }
            }
        });
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty()) {
            return false;
        }
        // Check if ALL files are actually PDFs
        for (FileItem item : fileListView.getItems()) {
            if (!item.getPath().toLowerCase().endsWith(".pdf")) {
                return false;
            }
        }
        return true;
    }
}