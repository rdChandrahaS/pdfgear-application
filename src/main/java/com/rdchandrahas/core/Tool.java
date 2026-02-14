package com.rdchandrahas.core;

public interface Tool {
    String getName();
    String getFxmlPath();
    Class<?> getControllerClass();
    
    default String getIconCode() { return null; }
    default String getIconPath() { return null; }
    default String getDescription() { return ""; }
    
}