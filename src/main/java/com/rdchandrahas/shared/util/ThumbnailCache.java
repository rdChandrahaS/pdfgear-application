package com.rdchandrahas.shared.util;

import javafx.scene.image.Image;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThumbnailCache {

    private static final Map<String, Image> cache = new ConcurrentHashMap<>();

    // Default Limit: 500 MB (in bytes)
    private static long maxSizeBytes = 500L * 1024L * 1024L;
    private static long currentSizeBytes = 0;

    /**
     * Updates the maximum allowed cache size.
     * @param bytes The new limit in bytes.
     */
    public static void setMaxSizeBytes(long bytes) {
        maxSizeBytes = bytes;
        System.out.println("Cache limit updated to: " + (bytes / 1024 / 1024) + " MB");
        enforceLimit();
    }

    public static Image get(String path) {
        return cache.get(path);
    }

    public static void put(String path, Image image) {
        if (image == null) return;

        // Estimate memory size: Width * Height * 4 bytes (Standard 32-bit color depth)
        long imgSize = (long) (image.getWidth() * image.getHeight() * 4);

        // Safety check: If a single image is larger than the entire limit, do not cache it.
        if (imgSize > maxSizeBytes) return;

        // Add to cache
        cache.put(path, image);
        currentSizeBytes += imgSize;

        // Check if we exceeded the limit
        enforceLimit();
    }

    public static boolean contains(String path) {
        return cache.containsKey(path);
    }

    /**
     * Clears the entire cache to free memory.
     */
    public static void clear() {
        cache.clear();
        currentSizeBytes = 0;
        System.out.println("Cache limit exceeded or manual clear invoked. Cache flushed.");
    }

    /**
     * Checks if current usage exceeds the limit. 
     * If yes, it flushes the cache (Simple strategy).
     */
    private static void enforceLimit() {
        if (currentSizeBytes > maxSizeBytes) {
            clear();
        }
    }
}