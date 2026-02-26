package com.rdchandrahas.core;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttachmentsManagerService {

    private static final Logger LOGGER = Logger.getLogger(AttachmentsManagerService.class.getName());

    public void addAttachment(String inputPath, String outputPath, String attachmentFilePath) throws IOException,GeneralSecurityException {
        LOGGER.log(Level.INFO, "Adding attachment to {0}", inputPath);

        try (PDDocument document = PDDocument.load(new File(inputPath), PdfService.getGlobalMemorySetting())) {
            PDDocumentNameDictionary namesDictionary = new PDDocumentNameDictionary(document.getDocumentCatalog());
            PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();
            
            if (efTree == null) {
                efTree = new PDEmbeddedFilesNameTreeNode();
            }

            File attachFile = new File(attachmentFilePath);
            
            PDComplexFileSpecification fs = new PDComplexFileSpecification();
            fs.setFile(attachFile.getName());

            try (InputStream is = new FileInputStream(attachFile)) {
                PDEmbeddedFile embeddedFile = new PDEmbeddedFile(document, is);
                embeddedFile.setSubtype("application/octet-stream");
                embeddedFile.setSize((int) attachFile.length());
                embeddedFile.setCreationDate(Calendar.getInstance());
                fs.setEmbeddedFile(embeddedFile);
            }

            Map<String, PDComplexFileSpecification> efMap = new HashMap<>();
            
            if (efTree.getNames() != null) {
                efMap.putAll(efTree.getNames());
            }
            
            efMap.put(attachFile.getName(), fs);
            efTree.setNames(efMap);
            
            namesDictionary.setEmbeddedFiles(efTree);
            document.getDocumentCatalog().setNames(namesDictionary);

            document.save(outputPath);
        }
    }
}