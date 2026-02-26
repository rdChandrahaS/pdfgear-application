package com.rdchandrahas.core;

import com.rdchandrahas.shared.util.TempFileManager; // ADDED IMPORT
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.io.MemoryUsageSetting;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PdfService implements PdfProcessor {

    private static final Logger LOGGER = Logger.getLogger(PdfService.class.getName());
    
    private static long memoryLimitBytes = 1024L * 1024L * 1024L; // Default 1GB
    private static final int MAX_OPEN_FILES_BATCH = 500;

    public static void setMemoryLimit(long bytes) {
        memoryLimitBytes = bytes;
        LOGGER.log(Level.INFO, "Global memory limit updated to: {0} bytes", bytes);
    }

    public static MemoryUsageSetting getGlobalMemorySetting() {
        MemoryUsageSetting setting = (memoryLimitBytes == -1) ? 
            MemoryUsageSetting.setupMainMemoryOnly() : 
            MemoryUsageSetting.setupMixed(memoryLimitBytes);
            
        // FIX 3: Force PDFBox to put its overflow buffers into our managed Temp folder
        try {
            setting = setting.setTempDir(TempFileManager.getTempDir().toFile());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not set custom temp dir for PDFBox", e);
        }
        return setting;
    }

    @Override
    public void merge(List<String> inputFiles, String outputFile) throws IOException, GeneralSecurityException {
        List<String> tempFilePaths = new ArrayList<>();
        List<String> currentChunk = new ArrayList<>();
        
        long currentChunkSizeBytes = 0;
        int batchCount = 1;

        LOGGER.log(Level.INFO, "Initiating mass merge for {0} files.", inputFiles.size());

        try {
            for (String inputFile : inputFiles) {
                File f = new File(inputFile);
                if (!f.exists()) continue;

                long fileSize = f.length();

                boolean limitExceeded = (memoryLimitBytes != -1 && (currentChunkSizeBytes + fileSize) > memoryLimitBytes);
                boolean handleLimitReached = (currentChunk.size() >= MAX_OPEN_FILES_BATCH);

                if (!currentChunk.isEmpty() && (limitExceeded || handleLimitReached)) {
                    String reason = limitExceeded ? "RAM limit" : "File handle limit";
                    batchCount = processFullBatch(currentChunk, tempFilePaths, batchCount, reason);
                    currentChunkSizeBytes = 0;
                }

                currentChunk.add(f.getAbsolutePath());
                currentChunkSizeBytes += fileSize;
            }

            // Handle the final batch
            if (!currentChunk.isEmpty()) {
                if (tempFilePaths.isEmpty()) {
                    executeMergeInternal(currentChunk, outputFile);
                    LOGGER.log(Level.INFO, "Merge completed in a single batch.");
                    return;
                } else {
                    // FIX 1: Use TempFileManager instead of OS default temp
                    File tempPdf = TempFileManager.createTempFile("merge_batch_final_", ".pdf");
                    executeMergeInternal(currentChunk, tempPdf.getAbsolutePath());
                    tempFilePaths.add(tempPdf.getAbsolutePath());
                }
            }

            LOGGER.log(Level.INFO, "Combining {0} temporary batches into final file: {1}", new Object[]{tempFilePaths.size(), outputFile});
            executeMergeInternal(tempFilePaths, outputFile);
            LOGGER.info("Massive merge operation successful.");
            
        } finally {
            // FIX 2: Guaranteed Cleanup! 
            // Even if executeMergeInternal throws an error midway, the temporary 500-file batches will be deleted.
            for (String tempPath : tempFilePaths) {
                File tempFile = new File(tempPath);
                if (tempFile.exists() && !tempFile.delete()) {
                    LOGGER.log(Level.WARNING, "Failed to delete temporary file: {0}", tempPath);
                }
            }
        }
    }

    private int processFullBatch(List<String> currentChunk, List<String> tempFilePaths, int batchCount, String reason) throws IOException {
        LOGGER.log(Level.INFO, "Batch {0} full ({1}). Merging to temp storage.", new Object[]{batchCount, reason});
        
        // FIX 1: Use TempFileManager
        File tempPdf = TempFileManager.createTempFile("merge_batch_" + batchCount + "_", ".pdf");
        
        executeMergeInternal(currentChunk, tempPdf.getAbsolutePath());
        tempFilePaths.add(tempPdf.getAbsolutePath());
        
        currentChunk.clear();
        return batchCount + 1;
    }

    private void executeMergeInternal(List<String> filesToMerge, String outputPath) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputPath);
        
        for (String path : filesToMerge) {
            merger.addSource(new File(path));
        }
        
        merger.mergeDocuments(getGlobalMemorySetting());
    }
}