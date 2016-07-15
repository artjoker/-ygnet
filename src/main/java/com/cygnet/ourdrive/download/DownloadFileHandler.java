package com.cygnet.ourdrive.download;

import com.cygnet.ourdrive.monitoring.FileHandler;
import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.download.DownloadService;
import com.cygnet.ourdrive.upload.UploadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA on 12/10/12.
 *
 * @author werneraltewischer
 */
public class DownloadFileHandler implements FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileHandler.class);

    @Override
    public String handle(InputStream inputStream, String filename, FolderSettings folderSettings, String targetContainerId) throws UploadServiceException {
        DownloadService downloadService = new DownloadService(folderSettings.getUsername(), folderSettings.getPassword(),
                folderSettings.getApiKey(), folderSettings.getUploadUrl(), folderSettings.getTargetContainer());
        return downloadService.download(inputStream, filename);
    }
}
