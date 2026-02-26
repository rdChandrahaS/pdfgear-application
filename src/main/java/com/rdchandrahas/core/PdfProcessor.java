package com.rdchandrahas.core;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * PdfProcessor defines the interface for core PDF manipulation tasks.
 * It provides a blueprint for various implementations (e.g., using Apache PDFBox)
 * to perform operations like merging multiple documents into a single file.
 */
public interface PdfProcessor {
    
    /**
     * Merges multiple PDF files into a single destination PDF.
     * * @param inputFiles A list of absolute file paths to the source PDF documents.
     * @param outputFile The absolute file path where the merged PDF will be saved.
     * @throws Exception If a file is missing, encrypted, or a write error occurs during processing.
     */
    void merge(List<String> inputFiles, String outputFile) throws IOException,GeneralSecurityException;
}