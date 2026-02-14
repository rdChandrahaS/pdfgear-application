package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * ImageToPdfTool provides the registration metadata for the image-to-document conversion utility.
 * This tool allows users to aggregate various image formats (JPEG, PNG, etc.) into a 
 * single, organized PDF file.
 */
public class ImageToPdfTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Image to PDF"
     */
    @Override
    public String getName() {
        return "Image to PDF";
    }

    /**
     * Explains the purpose of the tool, focusing on image aggregation and conversion.
     * @return A brief summary of the tool's function.
     */
    @Override
    public String getDescription() {
        return "Convert PNG and JPEG images into a single, high-quality PDF document.";
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
     * @return An images icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() {
        return "fas-images"; 
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/image_to_pdf.png";
    }

    /**
     * Returns the specific controller class that manages the logic for converting images to PDF.
     * @return The ImageToPdfController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.ImageToPdfController.class;
    }
}