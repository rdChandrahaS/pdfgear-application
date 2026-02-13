package com.rdchandrahas.shared.model;

import java.io.File;

public class FileItem {

    private final String path;
    private final String name;
    private final long size;
    private int pageCount;

    public FileItem(String path) {
        File file = new File(path);

        this.path = path;
        this.name = file.getName();
        this.size = file.length();
        this.pageCount = -1; // unknown initially
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FileItem other)) return false;
        return path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
