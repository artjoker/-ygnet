package com.cygnet.ourdrive.monitoring;

import java.io.File;

/**
 * Created by IntelliJ IDEA on 11/11/12.
 *
 * @author werneraltewischer
 */
public interface FolderMonitorListener {

    void uploadFailed(FolderMonitor fm, File file, Exception e);

    void uploadSucceeded(FolderMonitor fm, File file, String docId);
}
