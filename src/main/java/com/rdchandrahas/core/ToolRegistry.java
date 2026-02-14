package com.rdchandrahas.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ToolRegistry {
    private static final List<Tool> tools = new ArrayList<>();

    static {
        ServiceLoader<Tool> loader = ServiceLoader.load(Tool.class);
        for (Tool tool : loader) {
            tools.add(tool);
        }
    }

    public static List<Tool> getTools() { return tools; }
}