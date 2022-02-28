package com.robotdebris.ncaaps2scheduler;
import org.apache.log4j.Logger;

import java.awt.*;

public class FileChooser {
    private final Logger LOGGER = Logger.getLogger(FileChooser.class.getName());

    public String chooseFile(String title) {
        FileDialog dialog = new FileDialog((Frame) null, title);
        dialog.setFile("*.xlsx");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String file = dialog.getFile();
        String dir = dialog.getDirectory();
        LOGGER.info(file + " chosen.");
        if (file != null) {
            return dir + file;
        }
        return null;
    }
}