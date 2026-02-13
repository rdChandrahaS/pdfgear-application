package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class SplitPdfTool implements Tool {
    @Override
    public String getName() { return "Split PDF"; }

    @Override
    public String getFxmlPath() { return "/ui/SplitView.fxml"; }

    @Override
    public String getIconCode() { 
        return "fas-cut";
    }
}