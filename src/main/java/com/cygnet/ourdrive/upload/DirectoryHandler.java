package com.cygnet.ourdrive.upload;

import com.cygnet.ourdrive.settings.FolderSettings;

import java.io.File;

/**
 * Interface for handling upload directories
 */
public interface DirectoryHandler {
    /**
     * Handler directory for upload
     * @param dir directory
     * @param rootFolderSettings folder setting
     * @param targetContainerId container for upload
     * @return created target id
     * @throws Exception
     */
    public String handleDirectory(File dir, FolderSettings rootFolderSettings, String targetContainerId) throws Exception;
}
