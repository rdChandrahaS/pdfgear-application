package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * MergePdfTool provides the registration metadata for the PDF merging utility.
 * This tool allows users to combine multiple PDF documents into a single file,
 * maintaining the order specified in the UI.
 */
public class MergePdfTool implements Tool {
    
    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Merge PDF"
     */
    @Override
    public String getName() { 
        return "Merge PDF"; 
    }

    /**
     * Provides a brief summary of the tool's function.
     * @return A description of the merging capability.
     */
    @Override
    public String getDescription() {
        return "Combine multiple PDF files into one single document in your preferred order.";
    }

    /**
     * Points to the specific FXML view for merging. 
     * Note: Unlike other tools using ToolLayout, Merge uses a specialized view.
     * @return The resource path for the MergeView FXML.
     */
    @Override
    public String getFxmlPath() { 
        return "/ui/MergeView.fxml"; 
    }

    /**
     * Specifies the visual identifier for the tool's icon.
     * @return A layer-group icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() { 
        return "fas-layer-group"; 
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/merge_pdf.png";
    }

    /**
     * Returns the specific controller class that manages the logic for merging PDFs.
     * @return The MergeController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.MergeController.class;
    }
}