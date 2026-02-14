package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * PageNumberTool provides the registration metadata for the pagination utility.
 * This tool allows users to insert dynamic page numbering into existing PDF 
 * documents with support for various positioning and styling options.
 */
public class PageNumberTool implements Tool {
    
    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Add Page Numbers"
     */
    @Override
    public String getName() { 
        return "Add Page Numbers"; 
    }

    /**
     * Explains the purpose of the tool, focusing on customization and localization.
     * @return A brief summary of the pagination features.
     */
    @Override
    public String getDescription() {
        return "Add customizable page numbers to your PDF in multiple languages and styles.";
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
     * @return An ordered list icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() { 
        return "fas-list-ol"; 
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/page_number_pdf.png";
    }

    /**
     * Returns the specific controller class that manages the logic for adding page numbers.
     * @return The PageNumberController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.PageNumberController.class;
    }
}