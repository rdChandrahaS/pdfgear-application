package com.rdchandrahas.core;

import java.awt.image.BufferedImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class ImageToPdfService {

    private static final Logger LOGGER = Logger.getLogger(ImageToPdfService.class.getName());

    public void convertImagesToPdf(List<String> imagePaths, String outputPath) throws Exception {
        LOGGER.log(Level.INFO, "Starting PARALLEL Image to PDF conversion for {0} images.", imagePaths.size());

        List<String> tempPdfPaths = imagePaths.parallelStream().map(imagePath -> {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) return null;

            try {
                BufferedImage bImage = ImageIO.read(imageFile);
                if (bImage == null) {
                    LOGGER.log(Level.WARNING, "Failed to decode image: {0}", imageFile.getName());
                    return null;
                }

                // Create a lightweight, isolated PDDocument for this specific image
                try (PDDocument tempDoc = new PDDocument(PdfService.getGlobalMemorySetting())) {
                    
                    // High-speed JPEG compression
                    PDImageXObject pdImage = JPEGFactory.createFromImage(tempDoc, bImage, 0.85f);
                    
                    // Flush immediately to keep thread memory footprint tiny
                    bImage.flush(); 
                    
                    PDRectangle pageSize = new PDRectangle(pdImage.getWidth(), pdImage.getHeight());
                    PDPage page = new PDPage(pageSize);
                    tempDoc.addPage(page);
                    
                    try (PDPageContentStream contentStream = new PDPageContentStream(tempDoc, page)) {
                        contentStream.drawImage(pdImage, 0, 0);
                    }
                    
                    // Save to a temporary file
                    File tempPdf = File.createTempFile("parallel_img_", ".pdf");
                    tempPdf.deleteOnExit();
                    tempDoc.save(tempPdf.getAbsolutePath());
                    
                    return tempPdf.getAbsolutePath();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing image: " + imagePath, e);
                return null;
            }
        })
        .filter(Objects::nonNull) // Remove any failed conversions from the list
        .collect(Collectors.toList());

        // Final Step: Merge all the parallel-generated 1-page PDFs into the final document
        if (!tempPdfPaths.isEmpty()) {
            LOGGER.log(Level.INFO, "Merging {0} parallel-processed pages into final PDF...", tempPdfPaths.size());
            
            PdfService pdfService = new PdfService();
            pdfService.merge(tempPdfPaths, outputPath);
            
            // Clean up the temporary hard drive files
            for (String path : tempPdfPaths) {
                File tempFile = new File(path);
                if (!tempFile.delete()) {
                    LOGGER.log(Level.WARNING, "Failed to delete temp file: {0}", path);
                }
            }
            LOGGER.log(Level.INFO, "Parallel conversion and merge complete!");
        }
    }
}