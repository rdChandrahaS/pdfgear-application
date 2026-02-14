package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * PdfToImageTool provides the registration metadata for the PDF-to-raster conversion utility.
 * This tool allows users to transform document pages into individual image files, 
 * useful for presentations, social media sharing, or archiving.
 */
public class PdfToImageTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "PDF to Image"
     */
    @Override
    public String getName() {
        return "PDF to Image";
    }

    /**
     * Explains the purpose of the tool, focusing on output format variety.
     * @return A brief summary of the conversion features.
     */
    @Override
    public String getDescription() {
        return "Convert PDF pages into high-quality PNG or JPEG images.";
    }

    /**
     * Points to the shared FXML container that hosts the tool's specific logic.
     * @return The resource path for the tool shell.
     */
    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    /**
     * Specifies the visual identifier for the tool's icon.
     * @return A file-image icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() {
        return "fas-file-image"; 
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/pdf_to_image.png";
    }

    /**
     * Returns the specific controller class that manages the logic for converting PDFs to images.
     * @return The PdfToImageController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.PdfToImageController.class;
    }
}