package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.upload.DirectoryHandler;

import java.util.List;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA on 12/10/12.
 *
 * @author werneraltewischer
 */
public class FolderMonitorTask extends TimerTask {

    private final FolderMonitor folderMonitor;
    private final FileHandler fileHandler;
    private final DirectoryHandler directoryHandler;

    public FolderMonitorTask(FolderMonitor folderMonitor,
                             FileHandler fileHandler,
                             DirectoryHandler directoryHandler,
                             List<FolderMonitorListener> listeners) {
        this.folderMonitor = folderMonitor;
        this.fileHandler = fileHandler;
        this.directoryHandler = directoryHandler;

        if (listeners != null) {
            for (FolderMonitorListener listener : listeners) {
                this.folderMonitor.addListener(listener);
            }
        }
    }

    @Override
    public void run() {
        this.folderMonitor.checkForModifications(fileHandler, directoryHandler);
    }
}
