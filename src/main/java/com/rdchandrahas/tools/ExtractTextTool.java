package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * ExtractTextTool provides the registration metadata for the text extraction utility.
 * This tool enables users to convert PDF content into plain text format (.txt),
 * facilitating content repurposing and accessibility.
 */
public class ExtractTextTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Extract Text"
     */
    @Override
    public String getName() {
        return "Extract Text";
    }

    /**
     * Explains the purpose of the tool, focusing on document-to-text conversion.
     * @return A brief summary of the tool's function.
     */
    @Override
    public String getDescription() {
        return "Instantly extract all readable text from a PDF into a clean .txt file.";
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
     * @return A file-alt icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() {
        return "fas-file-alt"; 
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/extract_text_pdf.png";
    }

    /**
     * Returns the specific controller class that manages the logic for text extraction.
     * @return The ExtractTextController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.ExtractTextController.class;
    }
}