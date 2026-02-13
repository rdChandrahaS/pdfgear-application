package com.rdchandrahas.shared.util;

import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThumbnailCache {

    private static final Map<String, Image> cache =
            new ConcurrentHashMap<>();

    public static Image get(String path) {
        return cache.get(path);
    }

    public static void put(String path, Image image) {
        cache.put(path, image);
    }

    public static boolean contains(String path) {
        return cache.containsKey(path);
    }
}
