package com.rdchandrahas.core;

import java.io.File;
import java.util.List;

public interface OSService {
    List<File> getSystemFontDirectories();
    void openBrowser(String url);
    String getOSName();
}