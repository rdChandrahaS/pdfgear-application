package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * ProtectPdfTool provides the registration metadata for the PDF encryption utility.
 * This tool allows users to secure their documents by adding owner and user passwords,
 * as well as setting specific permission restrictions (e.g., printing or copying).
 */
public class ProtectPdfTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Protect PDF"
     */
    @Override
    public String getName() {
        return "Protect PDF";
    }

    /**
     * Provides a brief summary of the tool's function for UI tooltips.
     * @return A description of the security features.
     */
    @Override
    public String getDescription() {
        return "Secure your PDF files with passwords and restrict permissions like printing and copying.";
    }

    /**
     * Points to the specific FXML view for protection settings.
     * Note: This tool uses a specialized ProtectView instead of the generic ToolLayout.
     * @return The resource path for the ProtectView FXML.
     */
    @Override
    public String getFxmlPath() {
        return "/ui/ProtectView.fxml";
    }

    /**
     * Specifies the visual identifier for the tool's icon.
     * @return A lock icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() {
        return "fas-lock";
    }

    /**
     * Custom images for tool logo.
     * @return image path
     */
    @Override
    public String getIconPath() {
        return "/icons/tools/protect_pdf.png";
    }

    /**
     * Returns the specific controller class that manages the logic for protecting PDFs.
     * @return The ProtectController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.ProtectController.class;
    }
}