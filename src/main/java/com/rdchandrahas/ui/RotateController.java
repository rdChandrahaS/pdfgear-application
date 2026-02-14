package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RotateController extends BaseToolController {

    private ComboBox<String> angleComboBox;
    private TextField pageRangeInput;
    private Button previewBtn;

    @Override
    protected void onInitialize() {
        setTitle("Rotate PDF");
        setActionText("Rotate & Save");

        angleComboBox = new ComboBox<>();
        angleComboBox.getItems().addAll("90° Clockwise", "180°", "90° Counter-Clockwise");
        angleComboBox.getSelectionModel().selectFirst();

        pageRangeInput = new TextField();
        pageRangeInput.setPromptText("Pages (e.g., 1, 3, 5-10) or blank for all");
        pageRangeInput.setPrefWidth(250);

        previewBtn = new Button("Preview");
        previewBtn.getStyleClass().add("button");
        previewBtn.setOnAction(e -> showPreview());

        addToolbarItem(angleComboBox, pageRangeInput, previewBtn);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save Rotated PDF", "rotated_document.pdf", (destination) -> {
            int rotationAngle = getSelectedAngle();
            String rangeText = pageRangeInput.getText().trim();

            List<String> filePaths = fileListView.getItems().stream()
                    .map(item -> ((FileItem) item).getPath())
                    .collect(Collectors.toList());

            if (filePaths.size() > 1) {
                mergeDocumentsSafe(filePaths, destination);
            } else {
                Files.copy(new File(filePaths.get(0)).toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            try (PDDocument doc = loadDocumentSafe(destination.getAbsolutePath())) {
                Set<Integer> pagesToRotate = parsePageRange(rangeText, doc.getNumberOfPages());
                
                int pageNum = 1;
                for (PDPage page : doc.getPages()) {
                    if (pagesToRotate.contains(pageNum)) {
                        page.setRotation(page.getRotation() + rotationAngle);
                    }
                    pageNum++;
                }
                doc.save(destination);
            }
        });
    }

    private void showPreview() {
        if (fileListView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Files", "Please add a PDF to preview.");
            return;
        }

        FileItem firstItem = (FileItem) fileListView.getItems().get(0);
        int rotationAngle = getSelectedAngle();
        String rangeText = pageRangeInput.getText().trim();

        setBusy(true, previewBtn);

        new Thread(() -> {
            try (PDDocument doc = loadDocumentSafe(firstItem.getPath())) {
                Set<Integer> pagesToRotate = parsePageRange(rangeText, doc.getNumberOfPages());
                
                int firstRequestedPage = pagesToRotate.isEmpty() ? 1 : pagesToRotate.iterator().next();
                final int pageToPreview = Math.min(firstRequestedPage, doc.getNumberOfPages());

                PDFRenderer renderer = new PDFRenderer(doc);
                BufferedImage bim = renderer.renderImageWithDPI(pageToPreview - 1, 100, ImageType.RGB);
                Image fxImage = SwingFXUtils.toFXImage(bim, null);

                Platform.runLater(() -> {
                    setBusy(false, previewBtn);
                    displayPreviewDialog(fxImage, rotationAngle, pageToPreview);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setBusy(false, previewBtn);
                    showAlert(Alert.AlertType.ERROR, "Preview Error", "Could not generate preview: " + e.getMessage());
                });
            }
        }).start();
    }

    private void displayPreviewDialog(Image image, int rotationAngle, int pageNum) {
        Stage previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.setTitle("Preview - Page " + pageNum);

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(500);
        imageView.setFitWidth(500);
        
        imageView.setRotate(rotationAngle);

        VBox layout = new VBox(15, imageView);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #252525;"); 

        Scene scene = new Scene(layout, 600, 600);
        previewStage.setScene(scene);
        previewStage.show();
    }

    private int getSelectedAngle() {
        return switch (angleComboBox.getValue()) {
            case "180°" -> 180;
            case "90° Counter-Clockwise" -> 270;
            default -> 90;
        };
    }

    private Set<Integer> parsePageRange(String rangeText, int maxPages) {
        Set<Integer> pages = new HashSet<>();
        if (rangeText == null || rangeText.trim().isEmpty()) {
            for (int i = 1; i <= maxPages; i++) pages.add(i);
            return pages;
        }

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
                    for (int i = start; i <= end; i++) pages.add(i);
                } else {
                    pages.add(Integer.parseInt(part));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return pages;
    }
}