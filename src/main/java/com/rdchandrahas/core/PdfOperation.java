package com.rdchandrahas.core;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.pdfbox.pdmodel.PDDocument;

@FunctionalInterface
public interface PdfOperation {
    void execute(PDDocument document) throws IOException,GeneralSecurityException;
}