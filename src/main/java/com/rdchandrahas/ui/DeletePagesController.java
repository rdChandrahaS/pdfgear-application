// src/main/java/com/rdchandrahas/ui/DeletePagesController.java
package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeletePagesController extends BaseToolController {

    private TextField pageRangeInput;

    @Override
    protected void onInitialize() {
        setTitle("Delete Pages");
        setActionText("Delete & Save");

        pageRangeInput = new TextField();
        pageRangeInput.setPromptText("Pages to delete (e.g., 1, 3, 5-10)");
        pageRangeInput.setPrefWidth(250);

        addToolbarItem(pageRangeInput);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        String rangeText = pageRangeInput.getText().trim();
        if (rangeText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please enter the pages you want to delete.");
            return;
        }

        processWithSaveDialog("Save PDF", "pages_deleted.pdf", (destination) -> {
            List<String> filePaths = fileListView.getItems().stream()
                    .map(item -> ((FileItem) item).getPath())
                    .collect(Collectors.toList());

            File tempMerged = null;
            String sourcePath;

            if (filePaths.size() > 1) {
                tempMerged = File.createTempFile("merged_temp", ".pdf");
                mergeDocumentsSafe(filePaths, tempMerged);
                sourcePath = tempMerged.getAbsolutePath();
            } else {
                sourcePath = filePaths.get(0);
            }

            try (PDDocument sourceDoc = loadDocumentSafe(sourcePath);
                 PDDocument finalDoc = createDocumentSafe()) {
                
                int maxPages = sourceDoc.getNumberOfPages();
                Set<Integer> pagesToDelete = parsePageRange(rangeText, maxPages);

                for (int i = 1; i <= maxPages; i++) {
                    if (!pagesToDelete.contains(i)) {
                        finalDoc.addPage(finalDoc.importPage(sourceDoc.getPage(i - 1)));
                    }
                }
                
                finalDoc.save(destination);
            } finally {
                if (tempMerged != null && tempMerged.exists()) {
                    tempMerged.delete();
                }
            }
        });
    }

    private Set<Integer> parsePageRange(String rangeText, int maxPages) {
        Set<Integer> pages = new HashSet<>();
        String normalizedText = rangeText.replaceAll("\\s+", ",");
        String[] parts = normalizedText.split(",");
        
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            try {
                if (part.contains("-")) {
                    String[] bounds = part.split("-");
                    int start = Integer.parseInt(bounds[0].trim());
                    int end = Integer.parseInt(bounds[1].trim());
                    for (int i = start; i <= end; i++) {
                        if (i <= maxPages) pages.add(i);
                    }
                } else {
                    int pageNum = Integer.parseInt(part);
                    if (pageNum <= maxPages) pages.add(pageNum);
                }
            } catch (NumberFormatException ignored) {}
        }
        return pages;
    }
}