package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * WatermarkPdfTool provides the registration metadata for the document branding utility.
 * This tool allows users to overlay custom text or stamps onto their PDF pages 
 * to indicate ownership, confidentiality, or status.
 */
public class WatermarkPdfTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Watermark PDF"
     */
    @Override
    public String getName() {
        return "Watermark PDF";
    }

    /**
     * Provides a brief summary of the tool's function for UI tooltips.
     * @return A description of the custom text watermarking capabilities.
     */
    @Override
    public String getDescription() {
        return "Add a custom text watermark to all pages of your PDF document.";
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
     * @return A 'stamp' icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() {
        return "fas-stamp"; 
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/watermark_pdf.png";
    }

    /**
     * Returns the specific controller class that manages the logic for applying watermarks.
     * @return The WatermarkController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.WatermarkController.class;
    }
}