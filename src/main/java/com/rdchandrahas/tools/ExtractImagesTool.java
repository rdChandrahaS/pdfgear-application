// src/main/java/com/rdchandrahas/tools/ExtractImagesTool.java
package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class ExtractImagesTool implements Tool {

    @Override
    public String getName() {
        return "Extract Images";
    }

    @Override
    public String getDescription() {
        return "Extract all embedded images from your PDF files into a folder.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-images"; 
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.ExtractImagesController.class;
    }
}