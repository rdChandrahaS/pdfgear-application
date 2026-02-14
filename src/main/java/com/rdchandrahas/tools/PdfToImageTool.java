package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class PdfToImageTool implements Tool {

    @Override
    public String getName() {
        return "PDF to Image";
    }

    @Override
    public String getDescription() {
        return "Convert PDF pages into high-quality PNG or JPEG images.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-file-image"; 
    }

    @Override
    public String getIconPath() {
        return null; 
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.PdfToImageController.class;
    }
}