package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class ImageToPdfTool implements Tool {

    @Override
    public String getName() {
        return "Image to PDF";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ImageToPdfView.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-image";
    }
}