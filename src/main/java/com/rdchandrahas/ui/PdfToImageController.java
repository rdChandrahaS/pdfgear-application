package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.DirectoryChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class PdfToImageController extends BaseToolController {

    private ComboBox<String> formatCombo;
    private ComboBox<String> dpiCombo;

    @Override
    protected void onInitialize() {
        setTitle("PDF to Image");
        setActionText("Convert & Save");

        formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("PNG", "JPEG");
        formatCombo.getSelectionModel().selectFirst();

        dpiCombo = new ComboBox<>();
        dpiCombo.getItems().addAll("150 DPI (Standard)", "300 DPI (High Quality)");
        dpiCombo.getSelectionModel().selectLast();

        addToolbarItem(formatCombo, dpiCombo);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        if (fileListView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Files", "Please add files to convert.");
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Destination Folder");
        File destDir = chooser.showDialog(actionBtn.getScene().getWindow());

        if (destDir == null)
            return;

        String format = formatCombo.getValue().toLowerCase();
        int dpi = dpiCombo.getValue().contains("300") ? 300 : 150;

        ImageType imageType = format.equals("png") ? ImageType.ARGB : ImageType.RGB;

        setBusy(true, actionBtn);

        new Thread(() -> {
            try {
                for (Object obj : fileListView.getItems()) {
                    FileItem item = (FileItem) obj;
                    File sourceFile = new File(item.getPath());
                    String baseName = sourceFile.getName().replaceFirst("[.][^.]+$", "");

                    try (PDDocument doc = loadDocumentSafe(item.getPath())) {
                        PDFRenderer renderer = new PDFRenderer(doc);

                        for (int i = 0; i < doc.getNumberOfPages(); i++) {
                            BufferedImage image = renderer.renderImageWithDPI(i, dpi, imageType);
                            File outputFile = new File(destDir, baseName + "_page_" + (i + 1) + "." + format);
                            ImageIO.write(image, format, outputFile);
                        }
                    }
                }

                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "All pages converted to images successfully!");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.ERROR, "Error", "Conversion failed: " + e.getMessage());
                });
            }
        }).start();
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