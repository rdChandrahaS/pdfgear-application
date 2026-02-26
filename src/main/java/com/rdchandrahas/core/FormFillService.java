package com.rdchandrahas.core;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FormFillService {

    private static final Logger LOGGER = Logger.getLogger(FormFillService.class.getName());

    public void fillForm(String inputPath, String outputPath, Map<String, String> formData) throws IOException {
        LOGGER.log(Level.INFO, "Starting Form Fill for {0}", inputPath);

        try (PDDocument document = PDDocument.load(new File(inputPath), PdfService.getGlobalMemorySetting())) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            if (acroForm != null) {
                for (Map.Entry<String, String> entry : formData.entrySet()) {
                    PDField field = acroForm.getField(entry.getKey());
                    if (field != null) {
                        field.setValue(entry.getValue());
                    } else {
                        LOGGER.log(Level.WARNING, "Form field {0} not found.", entry.getKey());
                    }
                }
                acroForm.flatten(); 
            } else {
                LOGGER.log(Level.WARNING, "No AcroForm found in the provided PDF.");
            }

            document.save(outputPath);
        }
    }
}