package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * CompressPdfTool defines the registration metadata for the PDF Compression utility.
 * It implements the Tool interface to allow the dynamic registry to discover
 * and display this tool in the main application dashboard.
 */
public class CompressPdfTool implements Tool {

    /**
     * Returns the display name of the tool as shown in the UI.
     * @return "Compress PDF"
     */
    @Override
    public String getName() {
        return "Compress PDF";
    }

    /**
     * Provides a detailed description of the tool's capabilities for tooltips.
     * @return A string explaining compression options.
     */
    @Override
    public String getDescription() {
        return "Reduce PDF file size by percentage or to a specific target size (KB/MB).";
    }

    /**
     * Defines the standard UI shell used to host the compression tool's logic.
     * @return The resource path to the ToolLayout FXML.
     */
    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    /**
     * Specifies the FontAwesome/Ikonli code for the visual icon.
     * @return The compression icon identifier.
     */
    @Override
    public String getIconCode() {
        return "fas-compress-arrows-alt"; 
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/compress_pdf.png";
    }

    /**
     * Links the tool definition to its functional logic class.
     * This class will be instantiated and injected into the FXML during navigation.
     * @return The CompressController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.CompressController.class;
    }
}