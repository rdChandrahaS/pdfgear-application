package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class CompressPdfTool implements Tool {
    @Override
    public String getName() { return "Compress PDF"; }

    @Override
    public String getFxmlPath() { return "/ui/CompressView.fxml"; }

    @Override
    public String getIconCode() { 
        return "fas-compress-arrows-alt"; 
    }
}