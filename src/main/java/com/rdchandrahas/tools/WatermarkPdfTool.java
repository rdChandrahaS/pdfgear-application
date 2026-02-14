package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class WatermarkPdfTool implements Tool {

    @Override
    public String getName() {
        return "Watermark PDF";
    }

    @Override
    public String getDescription() {
        return "Add a custom text watermark to all pages of your PDF document.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-stamp"; // A perfect icon for stamping watermarks
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.WatermarkController.class;
    }
}