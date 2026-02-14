package com.rdchandrahas;

public class Launcher {
    
    // This main method simply calls the main method of your JavaFX Application class.
    // This "trick" prevents the "JavaFX runtime components are missing" error
    // when running as a non-modular application.
    public static void main(String[] args) {
        MainApp.main(args);
    }
}