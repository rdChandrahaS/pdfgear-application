package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * RearrangePagesTool provides the registration metadata for the page sequencing utility.
 * This tool allows users to reorder existing pages within a single PDF document 
 * by defining a custom page sequence.
 */
public class RearrangePagesTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Rearrange Pages"
     */
    @Override
    public String getName() {
        return "Rearrange Pages";
    }

    /**
     * Explains the purpose of the tool, focusing on structural document modification.
     * @return A brief summary of the reordering capability.
     */
    @Override
    public String getDescription() {
        return "Change the order of pages in your PDF by specifying a new sequence.";
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
     * @return A random/shuffle icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() {
        return "fas-random"; 
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/rearrange_page_pdf.png";
    }

    /**
     * Returns the specific controller class that manages the logic for rearranging pages.
     * @return The RearrangePagesController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.RearrangePagesController.class;
    }
}