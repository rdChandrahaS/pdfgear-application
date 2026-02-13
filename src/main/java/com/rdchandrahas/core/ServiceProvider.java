package com.rdchandrahas.core;

/**
 * Acts as a simple DI Container to provide shared service instances.
 */
public class ServiceProvider {
    private static final PdfProcessor pdfProcessor = new PdfService(); 

    public static PdfProcessor getPdfProcessor() {
        return pdfProcessor;
    }
}