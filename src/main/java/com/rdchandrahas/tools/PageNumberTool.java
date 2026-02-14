package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class PageNumberTool implements Tool {
    @Override
    public String getName() { return "Add Page Numbers"; }

    @Override
    public String getDescription() {
        return "Add customizable page numbers to your PDF in multiple languages and styles.";
    }

    @Override
    public String getFxmlPath() { return "/ui/ToolLayout.fxml"; }

    @Override
    public String getIconCode() { return "fas-list-ol"; }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.PageNumberController.class;
    }
}