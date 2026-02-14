// src/main/java/com/rdchandrahas/ui/ExtractImagesController.java
package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ExtractImagesController extends BaseToolController {

    @Override
    protected void onInitialize() {
        setTitle("Extract Images");
        setActionText("Extract to Folder");
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        if (fileListView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Files", "Please add files to extract images from.");
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Destination Folder");
        File destDir = chooser.showDialog(actionBtn.getScene().getWindow());

        if (destDir == null) return;

        setBusy(true, actionBtn);

        new Thread(() -> {
            int totalExtracted = 0;
            try {
                for (Object obj : fileListView.getItems()) {
                    FileItem item = (FileItem) obj;
                    File sourceFile = new File(item.getPath());
                    String baseName = sourceFile.getName().replaceFirst("[.][^.]+$", "");

                    try (PDDocument doc = loadDocumentSafe(item.getPath())) {
                        int pageNum = 1;
                        for (PDPage page : doc.getPages()) {
                            PDResources resources = page.getResources();
                            if (resources == null) continue;

                            int imageNum = 1;
                            for (COSName name : resources.getXObjectNames()) {
                                PDXObject xObject = resources.getXObject(name);
                                
                                if (xObject instanceof PDImageXObject pdImage) {
                                    BufferedImage bImage = pdImage.getImage();
                                    if (bImage != null) {
                                        // Use native format suffix if available, default to png
                                        String format = pdImage.getSuffix();
                                        if (format == null || format.isEmpty()) {
                                            format = "png";
                                        }

                                        String fileName = baseName + "_p" + pageNum + "_img" + imageNum + "." + format;
                                        File outputFile = new File(destDir, fileName);
                                        
                                        ImageIO.write(bImage, format, outputFile);
                                        imageNum++;
                                        totalExtracted++;
                                    }
                                }
                            }
                            pageNum++;
                        }
                    }
                }

                final int finalCount = totalExtracted;
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    if (finalCount > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Extracted " + finalCount + " images successfully!");
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "No Images", "No images were found inside the provided PDF(s).");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.ERROR, "Error", "Extraction failed: " + e.getMessage());
                });
            }
        }).start();
    }
}