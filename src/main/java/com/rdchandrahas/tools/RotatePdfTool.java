package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * RotatePdfTool provides the registration metadata for the page orientation utility.
 * This tool allows users to correct the orientation of PDF pages by applying 
 * clockwise or counter-clockwise rotations in 90-degree increments.
 */
public class RotatePdfTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Rotate PDF"
     */
    @Override
    public String getName() {
        return "Rotate PDF";
    }

    /**
     * Explains the purpose of the tool, focusing on orientation correction.
     * @return A brief summary of the rotation capabilities.
     */
    @Override
    public String getDescription() {
        return "Rotate PDF pages by 90, 180, or 270 degrees.";
    }

    /**
     * Specifies the visual identifier for the tool's icon.
     * @return The icon code (Note: Using a path here for specific branding).
     */
    @Override
    public String getIconCode() {
        return "/icons/pdf.png"; 
    }
    
    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/rotate_pdf.png";
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
     * Returns the specific controller class that manages the rotation logic.
     * @return The RotateController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.RotateController.class;
    }
}