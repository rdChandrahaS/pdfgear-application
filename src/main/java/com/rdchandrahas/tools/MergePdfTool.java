package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class MergePdfTool implements Tool {
    @Override
    public String getName() { return "Merge PDF"; }

    @Override
    public String getFxmlPath() { return "/ui/MergeView.fxml"; }

    @Override
    public String getIconCode() { return "fas-layer-group"; }
}