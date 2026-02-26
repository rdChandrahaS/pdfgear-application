package com.rdchandrahas.core;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookmarkManagerService {

    private static final Logger LOGGER = Logger.getLogger(BookmarkManagerService.class.getName());

    public static class BookmarkEntry {
        public final int pageNumber;
        public final String title;

        public BookmarkEntry(int pageNumber, String title) {
            this.pageNumber = pageNumber;
            this.title = title;
        }
    }

    public void addBookmarks(String inputPath, String outputPath, List<BookmarkEntry> bookmarks) throws IOException,GeneralSecurityException {
        LOGGER.log(Level.INFO, "Adding bookmarks to {0}", inputPath);

        try (PDDocument document = PDDocument.load(new File(inputPath), PdfService.getGlobalMemorySetting())) {
            PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
            
            if (outline == null) {
                outline = new PDDocumentOutline();
                document.getDocumentCatalog().setDocumentOutline(outline);
            }

            int totalPages = document.getNumberOfPages();

            for (BookmarkEntry entry : bookmarks) {
                int pageIndex = entry.pageNumber - 1; 
                
                if (pageIndex >= 0 && pageIndex < totalPages) {
                    PDPage page = document.getPage(pageIndex);
                    
                    PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
                    dest.setPage(page);

                    PDOutlineItem bookmarkItem = new PDOutlineItem();
                    bookmarkItem.setTitle(entry.title);
                    bookmarkItem.setDestination(dest);
                    
                    outline.addLast(bookmarkItem);
                } else {
                    LOGGER.log(Level.WARNING, "Invalid page number {0} for bookmark '{1}'", new Object[]{entry.pageNumber, entry.title});
                }
            }

            document.save(outputPath);
        }
    }
}