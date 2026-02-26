package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RearrangePagesController extends BaseToolController {

    private static final Logger LOGGER = Logger.getLogger(RearrangePagesController.class.getName());
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
        fileListView.getItems().clear(); 
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

            FileItem firstItem = fileListView.getItems().get(0);

            try (PDDocument sourceDoc = loadDocumentSafe(firstItem.getPath());
                 PDDocument finalDoc = createDocumentSafe()) {

                int maxPages = sourceDoc.getNumberOfPages();
                
                // FIX: Removed unused 'maxPages' parameter to fix SonarQube warning
                List<Integer> newOrder = parsePageOrder(orderText);

                if (newOrder.isEmpty()) {
                    // FIX: Replaced generic Exception with IOException to match the ToolTask signature
                    throw new IOException("Invalid page order provided.");
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

    private List<Integer> parsePageOrder(String rangeText) {
        List<Integer> pages = new ArrayList<>();
        String normalizedText = rangeText.replaceAll("\\s+", ""); 
        String[] parts = normalizedText.split(",");

        for (String part : parts) {
            if (!part.isEmpty()) {
                processOrderPart(part, pages);
            }
        }
        return pages;
    }

    private void processOrderPart(String part, List<Integer> pages) {
        try {
            if (part.contains("-")) {
                String[] bounds = part.split("-");
                if (bounds.length == 2) {
                    int start = Integer.parseInt(bounds[0]);
                    int end = Integer.parseInt(bounds[1]);

                    if (start <= end) {
                        for (int i = start; i <= end; i++) pages.add(i);
                    } else {
                        for (int i = start; i >= end; i--) pages.add(i);
                    }
                }
            } else {
                pages.add(Integer.parseInt(part));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Ignored invalid page number format: {0}", part);
        }
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