package com.cygnet.ourdrive.upload;

import com.cygnet.ourdrive.monitoring.FileHandler;
import com.cygnet.ourdrive.settings.FolderSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA on 12/10/12.
 *
 * @author werneraltewischer
 */
public class UploadFileHandler implements FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileHandler.class);

    @Override
    public String handle(InputStream inputStream,
                         String filename,
                         FolderSettings folderSettings,
                         String targetContainerId) throws UploadServiceException {
        UploadService uploadService = new UploadService(folderSettings.getUsername(), folderSettings.getPassword(),
                folderSettings.getApiKey(), folderSettings.getUploadUrl(), targetContainerId);
        return uploadService.uploadFile(inputStream, filename);
    }
}
