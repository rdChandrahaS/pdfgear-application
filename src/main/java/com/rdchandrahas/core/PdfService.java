package com.rdchandrahas.core;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.io.MemoryUsageSetting;
import java.io.File;
import java.util.List;

public class PdfService implements PdfProcessor {

    // Default limit: 1 GB (stored in bytes). -1 indicates Unrestricted.
    private static long memoryLimitBytes = 1024L * 1024L * 1024L;

    /**
     * Updates the global memory limit for PDF processing.
     * @param bytes Limit in bytes, or -1 for unrestricted.
     */
    public static void setMemoryLimit(long bytes) {
        memoryLimitBytes = bytes;
        System.out.println("Memory limit updated to: " + (bytes == -1 ? "Unrestricted" : bytes + " bytes"));
    }

    @Override
    public void merge(List<String> inputFiles, String outputFile) throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputFile);

        long totalInputSize = 0;
        
        // Add sources and calculate total size
        for (String path : inputFiles) {
            File f = new File(path);
            merger.addSource(f);
            if (f.exists()) {
                totalInputSize += f.length();
            }
        }

        MemoryUsageSetting settings;

        // Logic: If (Unrestricted) OR (Total Size < Limit) -> Use RAM
        // Otherwise -> Use Storage (Temp File)
        if (memoryLimitBytes == -1 || totalInputSize < memoryLimitBytes) {
            System.out.println("Processing in RAM. (Total Size: " + formatSize(totalInputSize) + ")");
            settings = MemoryUsageSetting.setupMainMemoryOnly();
        } else {
            System.out.println("Processing on Disk/Storage. (Total Size: " + formatSize(totalInputSize) + " exceeds limit)");
            settings = MemoryUsageSetting.setupTempFileOnly();
        }

        merger.mergeDocuments(settings);
    }
    
    // Helper to print readable file sizes (e.g., "10.5 MB")
    private String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}