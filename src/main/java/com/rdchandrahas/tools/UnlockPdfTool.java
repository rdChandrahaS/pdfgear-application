package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class UnlockPdfTool implements Tool {
    @Override
    public String getName() {
        return "Unlock PDF";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/UnlockView.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-unlock"; // Ikonli FontAwesome unlock icon
    }
}