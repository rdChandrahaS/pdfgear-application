package com.rdchandrahas.core;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetadataEditorService {

    private static final Logger LOGGER = Logger.getLogger(MetadataEditorService.class.getName());

    public void updateMetadata(String inputPath, String outputPath, Map<String, String> newMetadata) throws IOException,GeneralSecurityException {
        LOGGER.log(Level.INFO, "Updating metadata for {0}", inputPath);

        try (PDDocument document = PDDocument.load(new File(inputPath), PdfService.getGlobalMemorySetting())) {
            PDDocumentInformation info = document.getDocumentInformation();

            if (newMetadata.containsKey("Title")) {
                info.setTitle(newMetadata.get("Title"));
            }
            if (newMetadata.containsKey("Author")) {
                info.setAuthor(newMetadata.get("Author"));
            }
            if (newMetadata.containsKey("Subject")) {
                info.setSubject(newMetadata.get("Subject"));
            }
            if (newMetadata.containsKey("Keywords")) {
                info.setKeywords(newMetadata.get("Keywords"));
            }

            document.setDocumentInformation(info);
            document.save(outputPath);
        }
    }
}