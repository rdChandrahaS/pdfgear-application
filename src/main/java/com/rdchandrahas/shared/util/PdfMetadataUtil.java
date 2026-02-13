package com.rdchandrahas.shared.util;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;

public class PdfMetadataUtil {

    public static int getPageCount(String path) {
        try (PDDocument doc = PDDocument.load(new File(path))) {
            return doc.getNumberOfPages();
        } catch (Exception e) {
            return 0;
        }
    }
}
