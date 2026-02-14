package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class ExtractTextTool implements Tool {

    @Override
    public String getName() {
        return "Extract Text";
    }

    @Override
    public String getDescription() {
        return "Instantly extract all readable text from a PDF into a clean .txt file.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-file-alt"; 
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.ExtractTextController.class;
    }
}