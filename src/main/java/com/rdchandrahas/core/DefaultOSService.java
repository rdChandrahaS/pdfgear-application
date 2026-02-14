package com.rdchandrahas.core;

import javafx.application.HostServices;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultOSService implements OSService {
    private final HostServices hostServices;

    public DefaultOSService(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public List<File> getSystemFontDirectories() {
        List<File> dirs = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            dirs.add(new File("C:\\Windows\\Fonts"));
        } else if (os.contains("mac")) {
            dirs.add(new File("/Library/Fonts"));
            dirs.add(new File("/System/Library/Fonts"));
        } else {
            dirs.add(new File("/usr/share/fonts"));
            dirs.add(new File("/usr/local/share/fonts"));
            dirs.add(new File("fonts")); // Your custom local folder for .deb distribution
        }
        return dirs;
    }

    @Override
    public void openBrowser(String url) {
        if (hostServices != null) {
            hostServices.showDocument(url);
        }
    }

    @Override
    public String getOSName() {
        return System.getProperty("os.name");
    }
}