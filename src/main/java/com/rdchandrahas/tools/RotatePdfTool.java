package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class RotatePdfTool implements Tool {

    @Override
    public String getName() {
        return "Rotate PDF";
    }

    
    public String getDescription() {
        return "Rotate PDF pages by 90, 180, or 270 degrees.";
    }

    @Override
    public String getIconCode() {
        return "/icons/pdf.png"; 
    }
    
    public String getIconPath() {
        return "/icons/pdf.png"; 
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.RotateController.class;
    }
}