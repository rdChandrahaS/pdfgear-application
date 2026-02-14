package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class DeletePagesTool implements Tool {

    @Override
    public String getName() {
        return "Delete Pages";
    }

    @Override
    public String getDescription() {
        return "Remove specific pages from your PDF document.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-minus-circle";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.DeletePagesController.class;
    }
}