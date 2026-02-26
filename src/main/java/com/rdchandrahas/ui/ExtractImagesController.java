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
import java.io.IOException;
import java.security.GeneralSecurityException;

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
                    // Call helper method
                    totalExtracted += extractImagesFromPdf(new File(item.getPath()), destDir);
                }

                final int finalCount = totalExtracted;
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    if (finalCount > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Extracted " + finalCount + " images successfully!");
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "No Images",
                                "No images were found inside the provided PDF(s).");
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

    /**
     * Helper method to reduce complexity of handleAction.
     */
    private int extractImagesFromPdf(File sourceFile, File destDir) throws IOException,GeneralSecurityException {
        int count = 0;
        String baseName = sourceFile.getName().replaceFirst("[.][^.]+$", "");

        try (PDDocument doc = loadDocumentSafe(sourceFile.getAbsolutePath())) {
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
                            String format = pdImage.getSuffix();
                            if (format == null || format.isEmpty()) {
                                format = "png";
                            }

                            String fileName = baseName + "_p" + pageNum + "_img" + imageNum + "." + format;
                            File outputFile = new File(destDir, fileName);

                            ImageIO.write(bImage, format, outputFile);
                            imageNum++;
                            count++;
                        }
                    }
                }
                pageNum++;
            }
        }
        return count;
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