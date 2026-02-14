package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * ExtractImagesTool provides the registration metadata for the image extraction utility.
 * This tool allows users to scan PDF documents and save all internal raster images 
 * as separate files (JPG/PNG).
 */
public class ExtractImagesTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Extract Images"
     */
    @Override
    public String getName() {
        return "Extract Images";
    }

    /**
     * Explains the purpose of the tool, specifically focusing on media extraction.
     * @return A brief summary of the tool's function.
     */
    @Override
    public String getDescription() {
        return "Extract all embedded images from your PDF files into a folder.";
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
        return "/icons/tools/extract_image_pdf.png";
    }
    /**
     * Returns the specific controller class that manages the logic for image extraction.
     * @return The ExtractImagesController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.ExtractImagesController.class;
    }
}