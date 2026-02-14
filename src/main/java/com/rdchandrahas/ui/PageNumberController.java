package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class PageNumberController extends BaseToolController {

    private ComboBox<String> positionCombo;
    private ComboBox<String> styleCombo;
    private ComboBox<String> langCombo;
    private ComboBox<String> fontCombo; 
    private ComboBox<String> sizeCombo;
    private ColorPicker colorPicker;

    @Override
    protected void onInitialize() {
        setTitle("Add Page Numbers");
        setActionText("Apply Numbers");

        positionCombo = new ComboBox<>();
        positionCombo.getItems().addAll("Bottom Center", "Bottom Right", "Bottom Left", "Top Center", "Top Right", "Top Left");
        positionCombo.getSelectionModel().selectFirst();

        styleCombo = new ComboBox<>();
        styleCombo.getItems().addAll("1, 2, 3...", "Page 1", "Page 1 of X");
        styleCombo.getSelectionModel().selectFirst();

        langCombo = new ComboBox<>();
        langCombo.getItems().addAll("English", "Bengali", "Hindi", "French", "Spanish");
        langCombo.getSelectionModel().selectFirst();

        sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll("10", "12", "14", "16", "18", "24");
        sizeCombo.getSelectionModel().select("12");
        sizeCombo.setEditable(true);

        fontCombo = new ComboBox<>();
        fontCombo.getItems().add("Standard (Helvetica)");
        loadAvailableFonts(); 
        fontCombo.getSelectionModel().selectFirst();

        colorPicker = new ColorPicker(javafx.scene.paint.Color.BLACK);

        // UI Layout using VBox to organize two rows of controls in the toolbar
        HBox row1 = new HBox(10, new Label("Position:"), positionCombo, new Label("Style:"), styleCombo);
        HBox row2 = new HBox(10, new Label("Language:"), langCombo, new Label("Font:"), fontCombo, new Label("Size:"), sizeCombo, colorPicker);
        
        addToolbarItem(new VBox(10, row1, row2));
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        // Validation for Bengali/Hindi
        String lang = langCombo.getValue();
        if ((lang.equals("Bengali") || lang.equals("Hindi")) && fontCombo.getValue().equals("Standard (Helvetica)")) {
            showAlert(Alert.AlertType.WARNING, "Font Required", "Please select a custom .ttf font from the list to display Bengali or Hindi characters.");
            return;
        }

        processWithSaveDialog("Save PDF", "numbered_document.pdf", (destination) -> {
            List<String> filePaths = fileListView.getItems().stream()
                    .map(item -> ((FileItem) item).getPath()).collect(Collectors.toList());

            // 1. Prepare the file (Merge if many, Copy if one)
            if (filePaths.size() > 1) {
                mergeDocumentsSafe(filePaths, destination);
            } else {
                Files.copy(new File(filePaths.get(0)).toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 2. Load and process
            try (PDDocument doc = loadDocumentSafe(destination.getAbsolutePath())) {
                PDFont font = loadSelectedFont(doc);
                int totalPages = doc.getNumberOfPages();
                float fontSize = Float.parseFloat(sizeCombo.getValue());
                javafx.scene.paint.Color fxColor = colorPicker.getValue();

                for (int i = 0; i < totalPages; i++) {
                    PDPage page = doc.getPage(i);
                    String text = formatPageText(i + 1, totalPages, styleCombo.getValue(), langCombo.getValue());
                    
                    try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                        cs.beginText();
                        cs.setFont(font, fontSize);
                        cs.setNonStrokingColor((float)fxColor.getRed(), (float)fxColor.getGreen(), (float)fxColor.getBlue());
                        
                        PDRectangle mediabox = page.getMediaBox();
                        // Calculate exact text width for perfect centering/alignment
                        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
                        
                        float x = calculateX(positionCombo.getValue(), mediabox, textWidth);
                        float y = calculateY(positionCombo.getValue(), mediabox);
                        
                        cs.newLineAtOffset(x, y);
                        cs.showText(text);
                        cs.endText();
                    }
                }
                doc.save(destination);
            }
        });
    }

    private PDFont loadSelectedFont(PDDocument doc) throws IOException {
        String selectedName = fontCombo.getValue();
        if (selectedName.equals("Standard (Helvetica)")) {
            return PDType1Font.HELVETICA;
        }
        
        File fontFile = new File("fonts", selectedName + ".ttf");
        if (!fontFile.exists()) return PDType1Font.HELVETICA; // Fallback
        
        return PDType0Font.load(doc, fontFile);
    }

    private void loadAvailableFonts() {
        File fontDir = new File("fonts");
        if (!fontDir.exists()) fontDir.mkdirs();
        File[] files = fontDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf"));
        if (files != null) {
            for (File file : files) {
                fontCombo.getItems().add(file.getName().replace(".ttf", ""));
            }
        }
    }

    private String formatPageText(int current, int total, String style, String lang) {
        String pageWord = switch (lang) {
            case "Bengali" -> "পৃষ্ঠা";
            case "Hindi" -> "पृष्ठ";
            case "French" -> "Page";
            case "Spanish" -> "Página";
            default -> "Page";
        };
        String ofWord = switch (lang) {
            case "Bengali" -> "এর";
            case "Hindi" -> "का";
            case "French" -> "sur";
            case "Spanish" -> "de";
            default -> "of";
        };

        return switch (style) {
            case "Page 1" -> pageWord + " " + current;
            case "Page 1 of X" -> pageWord + " " + current + " " + ofWord + " " + total;
            default -> String.valueOf(current);
        };
    }

    private float calculateX(String pos, PDRectangle box, float textWidth) {
        float margin = 30;
        if (pos.contains("Left")) return margin;
        if (pos.contains("Right")) return box.getWidth() - margin - textWidth;
        return (box.getWidth() - textWidth) / 2; // Center
    }

    private float calculateY(String pos, PDRectangle box) {
        float margin = 30;
        if (pos.contains("Top")) return box.getHeight() - margin;
        return margin; // Bottom
    }
}