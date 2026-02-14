package com.rdchandrahas.shared.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class TempFileManager {

    private static Path appTempDir;

    /**
     * Creates a dedicated temp directory for this application run.
     * Location: C:\Users\You\AppData\Local\Temp\PDFGear_12345\
     */
    public static synchronized Path getTempDir() throws IOException {
        if (appTempDir == null) {
            // Create a temp folder with a prefix
            appTempDir = Files.createTempDirectory("PDFGear_");
            
            // Ensure it gets deleted when the app closes
            Runtime.getRuntime().addShutdownHook(new Thread(() -> cleanup(appTempDir)));
        }
        return appTempDir;
    }

    /**
     * Creates a temp file inside our dedicated folder.
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(getTempDir(), prefix, suffix).toFile();
    }

    /**
     * Deletes a directory and all its contents.
     */
    public static void cleanup(Path path) {
        try {
            Files.walk(path)
                .sorted(Comparator.reverseOrder()) // Delete files first, then folders
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("Could not clean up temp files: " + e.getMessage());
        }
    }
}