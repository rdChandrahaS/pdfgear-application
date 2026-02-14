package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class ProtectPdfTool implements Tool {
    @Override
    public String getName() {
        return "Protect PDF";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ProtectView.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-lock";
    }
    
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.ProtectController.class;
    }
}