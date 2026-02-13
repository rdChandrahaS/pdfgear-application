package com.rdchandrahas.core;

import java.util.List;

public interface PdfProcessor {
    void merge(List<String> inputFiles, String outputFile) throws Exception;
}