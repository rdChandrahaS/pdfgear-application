package com.rdchandrahas.shared.util;

import javafx.scene.image.Image;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ThumbnailCache provides an in-memory storage solution for generated thumbnails.
 * It uses a thread-safe ConcurrentHashMap with SoftReferences to allow the JVM 
 * to automatically free memory if the system runs out of RAM.
 */
public class ThumbnailCache {

    private static final Logger LOGGER = Logger.getLogger(ThumbnailCache.class.getName());

    private ThumbnailCache() {
        throw new IllegalStateException("Utility class");
    }
    
    private static final Map<String, SoftReference<Image>> cache = new ConcurrentHashMap<>();

    private static long maxSizeBytes = 500L * 1024L * 1024L;
    
    private static long currentSizeBytes = 0;

    public static void setMaxSizeBytes(long bytes) {
        maxSizeBytes = bytes;
        LOGGER.log(Level.INFO, "Cache limit updated to: {0} MB", (bytes / (1024 * 1024)));
        enforceLimit();
    }

    public static Image get(String path) {
        if (path == null) return null;
        SoftReference<Image> ref = cache.get(path);
        if (ref != null) {
            Image image = ref.get();
            if (image != null) {
                return image;
            } else {
                cache.remove(path);
            }
        }
        return null;
    }

    public static void put(String path, Image image) {
        if (image == null || path == null) return;

        long imgSize = (long) (image.getWidth() * image.getHeight() * 4);

        if (imgSize > maxSizeBytes) return;

        cache.put(path, new SoftReference<>(image));
        currentSizeBytes += imgSize;

        enforceLimit();
    }

    public static boolean contains(String path) {
        SoftReference<Image> ref = cache.get(path);
        return ref != null && ref.get() != null;
    }

    public static void clear() {
        cache.clear();
        currentSizeBytes = 0;
        LOGGER.log(Level.INFO, "Cache cleared to free system memory.");
    }

    private static void enforceLimit() {
        if (currentSizeBytes > maxSizeBytes) {
            clear();
        }
    }

    public static void remove(String path) {
        if (path == null) return;
        cache.remove(path);
    }
}