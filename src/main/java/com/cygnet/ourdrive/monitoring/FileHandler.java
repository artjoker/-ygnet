package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.settings.FolderSettings;

import java.io.File;
import java.io.InputStream;

/**
* Created by IntelliJ IDEA on 11/10/12.
*
* @author werneraltewischer
*/
public interface FileHandler {
    String handle(InputStream inputStream, String filename, FolderSettings folderSettings, String targetContainerId) throws Exception;
}
