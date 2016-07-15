package com.cygnet.ourdrive.upload;

import com.cygnet.ourdrive.settings.FolderSettings;

import java.io.File;

/**
 * Upload directory handler
 */
public class UploadDirectoryHandler implements DirectoryHandler {

    @Override
    public String handleDirectory(File dir, FolderSettings rootFolderSettings, String targetContainerId) throws Exception {
        UploadService uploadService = new UploadService(rootFolderSettings.getUsername(),
                rootFolderSettings.getPassword(),
                rootFolderSettings.getApiKey(),
                rootFolderSettings.getUploadUrl(),
                targetContainerId);

        return uploadService.uploadDirectory(dir);
    }
}
