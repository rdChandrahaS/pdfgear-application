package com.rdchandrahas.shared.util;

import com.rdchandrahas.shared.model.FileItem;

import java.util.List;

public class DuplicateDetector {

    public static boolean exists(List<FileItem> items, FileItem newItem) {

        return items.stream()
                .anyMatch(item ->
                        item.getPath().equals(newItem.getPath()));
    }
}
