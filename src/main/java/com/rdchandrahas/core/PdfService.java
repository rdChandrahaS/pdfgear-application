package com.rdchandrahas.core;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.io.MemoryUsageSetting;
import java.io.File;
import java.util.List;

public class PdfService implements PdfProcessor {

    @Override
    public void merge(List<String> inputFiles, String outputFile) throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputFile);

        for (String path : inputFiles) {
            merger.addSource(new File(path));
        }

        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    }
}