package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;
import java.util.List;

public class RearrangePagesController extends BaseToolController {

    private TextField orderInput;

    @Override
    protected void onInitialize() {
        setTitle("Rearrange Pages");
        setActionText("Rearrange & Save");

        orderInput = new TextField();
        orderInput.setPromptText("New order (e.g., 3, 1, 2, 4-10)");
        orderInput.setPrefWidth(250);

        addToolbarItem(orderInput);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        String orderText = orderInput.getText().trim();
        if (orderText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please enter the new page order.");
            return;
        }

        processWithSaveDialog("Save Rearranged PDF", "rearranged_document.pdf", (destination) -> {
            
            // Note: If multiple files are added, this processes only the first one to avoid logic conflicts.
            // If you want it to merge them all first, you can reuse the merge logic from previous controllers.
            FileItem firstItem = (FileItem) fileListView.getItems().get(0);

            try (PDDocument sourceDoc = loadDocumentSafe(firstItem.getPath());
                 PDDocument finalDoc = createDocumentSafe()) {

                int maxPages = sourceDoc.getNumberOfPages();
                List<Integer> newOrder = parsePageOrder(orderText, maxPages);

                if (newOrder.isEmpty()) {
                    throw new Exception("Invalid page order provided.");
                }

                for (int pageNum : newOrder) {
                    if (pageNum >= 1 && pageNum <= maxPages) {
                        finalDoc.addPage(finalDoc.importPage(sourceDoc.getPage(pageNum - 1)));
                    }
                }

                finalDoc.save(destination);
            }
        });
    }

    private List<Integer> parsePageOrder(String rangeText, int maxPages) {
        List<Integer> pages = new ArrayList<>();
        String normalizedText = rangeText.replaceAll("\\s+", ""); // Remove all spaces
        String[] parts = normalizedText.split(",");

        for (String part : parts) {
            if (part.isEmpty()) continue;
            try {
                if (part.contains("-")) {
                    String[] bounds = part.split("-");
                    int start = Integer.parseInt(bounds[0]);
                    int end = Integer.parseInt(bounds[1]);

                    // Advanced feature: Supports reversing order (e.g., 10-5 adds pages 10,9,8,7,6,5)
                    if (start <= end) {
                        for (int i = start; i <= end; i++) pages.add(i);
                    } else {
                        for (int i = start; i >= end; i--) pages.add(i);
                    }
                } else {
                    pages.add(Integer.parseInt(part));
                }
            } catch (NumberFormatException ignored) {}
        }
        return pages;
    }
}