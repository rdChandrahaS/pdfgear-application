// src/main/java/com/rdchandrahas/tools/RearrangePagesTool.java
package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class RearrangePagesTool implements Tool {

    @Override
    public String getName() {
        return "Rearrange Pages";
    }

    @Override
    public String getDescription() {
        return "Change the order of pages in your PDF by specifying a new sequence.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-random"; 
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.RearrangePagesController.class;
    }
}