package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * SplitPdfTool provides the registration metadata for the PDF splitting utility.
 * This tool allows users to break down a single PDF into multiple smaller files, 
 * either by page ranges, fixed page counts, or individual pages.
 */
public class SplitPdfTool implements Tool {
    
    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Split PDF"
     */
    @Override
    public String getName() { 
        return "Split PDF"; 
    }

    /**
     * Provides a brief summary of the tool's function for UI tooltips.
     * @return A description of the document extraction and splitting features.
     */
    @Override
    public String getDescription() {
        return "Divide a single PDF document into multiple files based on page ranges or specific criteria.";
    }

    /**
     * Points to the specialized FXML view for splitting configurations.
     * @return The resource path for the SplitView FXML.
     */
    @Override
    public String getFxmlPath() { 
        return "/ui/SplitView.fxml"; 
    }

    /**
     * Specifies the visual identifier for the tool's icon.
     * @return A 'cut' or scissors icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() { 
        return "fas-cut";
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/split_pdf.png";
    }
    
    /**
     * Returns the specific controller class that manages the logic for splitting PDFs.
     * @return The SplitController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.SplitController.class;
    }
}