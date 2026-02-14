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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class WatermarkController extends BaseToolController {

    private TextField watermarkInput;
    private ColorPicker colorPicker;
    private ComboBox<String> fontCombo;
    private CheckBox boldCheck;
    private CheckBox italicCheck;
    private ComboBox<String> sizeCombo;
    private Slider rotateSlider;
    private Slider opacitySlider;
    private Button previewBtn;

    private File manualCustomFile = null;

    @Override
    protected void onInitialize() {
        setTitle("Watermark PDF");
        setActionText("Apply Watermark");

        watermarkInput = new TextField("CONFIDENTIAL");
        watermarkInput.setPrefWidth(150);
        
        // This makes the button update instantly as the user types the watermark text
        watermarkInput.textProperty().addListener((obs, oldVal, newVal) -> updateActionBtnState());

        colorPicker = new ColorPicker(Color.DARKGRAY);

        fontCombo = new ComboBox<>();
        fontCombo.getItems().addAll("Helvetica", "Times Roman", "Courier", "Manual Select (.ttf)...");
        
        loadAvailableFonts(); // To load all local fonts
        
        fontCombo.getSelectionModel().selectFirst();

        fontCombo.setOnAction(e -> {
            if ("Manual Select (.ttf)...".equals(fontCombo.getValue())) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select TrueType Font");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TrueType Fonts", "*.ttf"));
                File selected = chooser.showOpenDialog(actionBtn.getScene().getWindow());
                
                if (selected != null) {
                    manualCustomFile = selected;
                    boldCheck.setDisable(true);
                    italicCheck.setDisable(true);
                } else {
                    fontCombo.getSelectionModel().selectFirst();
                }
            } else {
                manualCustomFile = null;
                // Only enable modifiers for Standard Fonts
                boolean isStandard = isStandardFont(fontCombo.getValue());
                boldCheck.setDisable(!isStandard);
                italicCheck.setDisable(!isStandard);
            }
        });

        boldCheck = new CheckBox();
        Label boldLabel = new Label("Bold");
        italicCheck = new CheckBox();
        Label italicLabel = new Label("Italic");

        sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll("24", "36", "48", "72", "96", "120", "150");
        sizeCombo.getSelectionModel().select("72");
        sizeCombo.setEditable(true);

        rotateSlider = new Slider(0, 360, 45);
        rotateSlider.setShowTickMarks(true);
        rotateSlider.setShowTickLabels(true);
        rotateSlider.setMajorTickUnit(45);
        rotateSlider.setPrefWidth(120);

        opacitySlider = new Slider(0.1, 1.0, 0.3);
        opacitySlider.setShowTickMarks(true);
        opacitySlider.setMajorTickUnit(0.2);
        opacitySlider.setPrefWidth(100);

        previewBtn = new Button("Preview");
        previewBtn.getStyleClass().add("button");
        previewBtn.setOnAction(e -> showPreview());

        HBox row1 = new HBox(10, watermarkInput, colorPicker, fontCombo, boldCheck, boldLabel, italicCheck, italicLabel);
        row1.setAlignment(Pos.CENTER_LEFT);
        
        HBox row2 = new HBox(10, new Label("Size:"), sizeCombo, new Label("Rotate:"), rotateSlider, new Label("Opacity:"), opacitySlider, previewBtn);
        row2.setAlignment(Pos.CENTER_LEFT);

        addToolbarItem(new VBox(10, row1, row2));
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        String watermarkText = watermarkInput.getText().trim();
        if (watermarkText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Text", "Please enter watermark text.");
            return;
        }

        WatermarkConfig config = getCurrentConfig();

        processWithSaveDialog("Save Watermarked PDF", "watermarked.pdf", (destination) -> {
            List<String> filePaths = fileListView.getItems().stream()
                    .map(item -> ((FileItem) item).getPath()).collect(Collectors.toList());

            if (filePaths.size() > 1) mergeDocumentsSafe(filePaths, destination);
            else Files.copy(new File(filePaths.get(0)).toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            try (PDDocument doc = loadDocumentSafe(destination.getAbsolutePath())) {
                PDFont font = loadSelectedFont(doc);
                for (PDPage page : doc.getPages()) {
                    applyWatermark(doc, page, config, font);
                }
                doc.save(destination);
            }
        });
    }

    private void showPreview() {
        WatermarkConfig config = getCurrentConfig();
        setBusy(true, previewBtn);
        new Thread(() -> {
            try (PDDocument doc = createDocumentSafe()) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                applyWatermark(doc, page, config, loadSelectedFont(doc));
                BufferedImage bim = new PDFRenderer(doc).renderImageWithDPI(0, 100, ImageType.RGB);
                Platform.runLater(() -> {
                    setBusy(false, previewBtn);
                    displayPreviewDialog(SwingFXUtils.toFXImage(bim, null));
                });
            } catch (Exception e) {
                Platform.runLater(() -> { setBusy(false, previewBtn); showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); });
            }
        }).start();
    }

    private void applyWatermark(PDDocument doc, PDPage page, WatermarkConfig config, PDFont font) throws IOException {
        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(config.opacity);
            cs.setGraphicsStateParameters(gs);
            cs.setNonStrokingColor(config.color);
            cs.beginText();
            cs.setFont(font, config.fontSize);
            PDRectangle box = page.getMediaBox();
            float txW = font.getStringWidth(config.text) / 1000 * config.fontSize;
            float txH = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * config.fontSize;
            Matrix matrix = Matrix.getRotateInstance(Math.toRadians(config.rotation), box.getWidth()/2, box.getHeight()/2);
            matrix.translate(-txW/2, -txH/2);
            cs.setTextMatrix(matrix);
            cs.showText(config.text);
            cs.endText();
        }
    }

    private PDFont loadSelectedFont(PDDocument doc) throws IOException {
        if (manualCustomFile != null) return PDType0Font.load(doc, manualCustomFile);
        
        String selection = fontCombo.getValue();
        // Check if it's a file from our /fonts folder
        File localFile = new File("fonts", selection + ".ttf");
        if (localFile.exists()) return PDType0Font.load(doc, localFile);

        boolean b = boldCheck.isSelected();
        boolean i = italicCheck.isSelected();
        return switch (selection) {
            case "Courier" -> (b && i) ? PDType1Font.COURIER_BOLD_OBLIQUE : b ? PDType1Font.COURIER_BOLD : i ? PDType1Font.COURIER_OBLIQUE : PDType1Font.COURIER;
            case "Times Roman" -> (b && i) ? PDType1Font.TIMES_BOLD_ITALIC : b ? PDType1Font.TIMES_BOLD : i ? PDType1Font.TIMES_ITALIC : PDType1Font.TIMES_ROMAN;
            default -> (b && i) ? PDType1Font.HELVETICA_BOLD_OBLIQUE : b ? PDType1Font.HELVETICA_BOLD : i ? PDType1Font.HELVETICA_OBLIQUE : PDType1Font.HELVETICA;
        };
    }

    private void loadAvailableFonts() {
        File fontDir = new File("fonts");
        if (!fontDir.exists()) fontDir.mkdirs();
        File[] files = fontDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf"));
        if (files != null) {
            for (File file : files) fontCombo.getItems().add(file.getName().replace(".ttf", ""));
        }
    }

    private boolean isStandardFont(String name) {
        return List.of("Helvetica", "Times Roman", "Courier").contains(name);
    }

    private WatermarkConfig getCurrentConfig() {
        Color fx = colorPicker.getValue();
        return new WatermarkConfig(watermarkInput.getText(), Float.parseFloat(sizeCombo.getValue()),
                new java.awt.Color((float)fx.getRed(), (float)fx.getGreen(), (float)fx.getBlue()),
                (float)rotateSlider.getValue(), (float)opacitySlider.getValue());
    }

    private void displayPreviewDialog(Image img) {
        Stage s = new Stage(); s.initModality(Modality.APPLICATION_MODAL);
        ImageView iv = new ImageView(img); iv.setPreserveRatio(true); iv.setFitHeight(600);
        VBox v = new VBox(iv); v.setAlignment(Pos.CENTER); v.setPadding(new Insets(20)); v.setStyle("-fx-background-color:#252525;");
        s.setScene(new Scene(v, 500, 650)); s.show();
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty() || watermarkInput.getText().trim().isEmpty()) {
            return false; 
        }
        for (FileItem item : fileListView.getItems()) {
            if (!item.getPath().toLowerCase().endsWith(".pdf")) {
                return false;
            }
        }
        return true; 
    }

    private record WatermarkConfig(String text, float fontSize, java.awt.Color color, float rotation, float opacity) {}
}