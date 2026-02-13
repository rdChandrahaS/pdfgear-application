package com.rdchandrahas.core;

import com.rdchandrahas.tools.*;
import java.util.List;

public class ToolRegistry {
    private static final List<Tool> tools = List.of(
            new MergePdfTool(),
            new SplitPdfTool(),
            new CompressPdfTool(),
            new ImageToPdfTool(),
            new ProtectPdfTool(),
            new UnlockPdfTool()
    );

    public static List<Tool> getTools() { return tools; }
}