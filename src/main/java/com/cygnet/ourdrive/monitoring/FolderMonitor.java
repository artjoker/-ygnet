package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.monitoring.visitor.FileVisitor;
import com.cygnet.ourdrive.monitoring.visitor.FilesTreeWalker;
import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.upload.DirectoryHandler;
import com.cygnet.ourdrive.util.DirUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA on 11/10/12.
 *
 * @author werneraltewischer
 */
public class FolderMonitor implements FileVisitor {

    private static final Logger logger = LoggerFactory.getLogger(FolderMonitor.class);
    private final FolderSettings folderSettings;
    private final List<FolderMonitorListener> listeners = new ArrayList<FolderMonitorListener>();

    public FolderMonitor(FolderSettings folderSettings) {
        this.folderSettings = folderSettings;
    }

    public FolderSettings getFolderSettings() {
        return folderSettings;
    }

    private boolean shouldIgnoreFile(String filename) {
        boolean ignore = filename.startsWith("#");
        ignore = ignore || filename.startsWith("~");
        ignore = ignore || filename.startsWith(".");
        ignore = ignore || filename.startsWith("$");
        ignore = ignore || filename.endsWith(".swp");
        ignore = ignore || filename.endsWith(".swap");
        ignore = ignore || filename.endsWith(".tmp");
        ignore = ignore || filename.endsWith(".temp");
        ignore = ignore || filename.endsWith("#");
        ignore = ignore || filename.indexOf('.') < 0;
        return ignore;
    }

    public void checkForModifications(FileHandler fileHandler, DirectoryHandler directoryHandler) {
        boolean isInTray = StringUtils.isBlank(folderSettings.getTargetContainer());

        FilesTreeWalker walker = new FilesTreeWalker(this, isInTray, fileHandler, directoryHandler);
        File root = folderSettings.getFolder();
        walker.walk(root, folderSettings.getTargetContainer());

        DirUtils.removeEmptyDirs(root);

        String[] files = root.list();
        if (files == null || files.length == 0) {
            //All containers uploaded
            folderSettings.clearSavedContainerIds();
        }
    }

    @Override
    public void visitFile(File file, FileHandler handler, String targetContainerId) {
        final String fileAbsolutePath = file.getAbsolutePath();
        if (shouldIgnoreFile(file.getName())) {
            logger.warn(file.getName() + " is ignored!");
            return;
        }

        Date modifiedDate = new Date(file.lastModified());
        Date existingModifiedDate = folderSettings.getModificationDate(fileAbsolutePath);

        if (folderSettings.isQuietPeriodExpired(fileAbsolutePath)) {
            if (existingModifiedDate == null || existingModifiedDate.before(modifiedDate)) {

                logger.info("Found modified file: " + fileAbsolutePath);
                if (uploadFile(file, handler, targetContainerId)) {
                    if (file.delete()) {
                        folderSettings.setModificationDate(fileAbsolutePath, null);
                    } else {
                        folderSettings.setModificationDate(fileAbsolutePath, modifiedDate);
                        logger.warn("Could not delete file: " + file);
                    }
                } else {
                    logger.info("Failed uploading file: " + fileAbsolutePath + ", retrying in " +
                            FolderSettings.QUIET_PERIOD / 1000L + " seconds");
                    folderSettings.setQuietPeriod(fileAbsolutePath);
                }
            }
        }
    }

    private boolean uploadFile(File file, FileHandler handler, String targetContainerId) {
        boolean handled = false;
        FileLock fileLock = null;
        try {
            fileLock = FileLock.tryLock(file);
            if (fileLock != null) {
                //We succeeded in locking the file so go ahead and upload it if modified
                InputStream is = new BufferedInputStream(new FileInputStream(file));
                try {
                    String docId = handler.handle(is, file.getName(), folderSettings, targetContainerId);
                    handled = true;
                    notifyUploadSuccess(file, docId);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            } else {
                throw new IOException("Could not obtain write lock");
            }
        } catch (Exception ex) {
            logger.warn("Failed uploading file '" + file.getAbsolutePath() + "': " + ex);
            notifyUploadFailed(file, ex);
        } finally {
            if (fileLock != null) {
                fileLock.unlock();
            }
        }
        return handled;
    }

    @Override
    public String visitDirectory(File dir, DirectoryHandler handler, String targetContainerId) {
        try {
            String key = dir.getAbsolutePath();
            String existingTargetId = folderSettings.getSavedContainerId(key);
            if(StringUtils.isNotEmpty(existingTargetId)) {
                logger.info("Found saved target id " + existingTargetId + " for folder " + key);
                return existingTargetId;
            }

            String targetId = handler.handleDirectory(dir, folderSettings, targetContainerId);
            folderSettings.setSavedContainerId(key, targetId);

            notifyUploadSuccess(dir, targetId);
            return targetId;
        } catch (Exception ex) {
            logger.warn("Failed uploading directory '" + dir.getAbsolutePath() + "': " + ex);
            notifyUploadFailed(dir, ex);
        }
        return null;
    }

    @Override
    public void visitFailed(File file, Exception ex) {
        for (FolderMonitorListener listener : getListeners()) {
            listener.uploadFailed(this, file, ex);
        }
    }

    public List<FolderMonitorListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public void addListener(FolderMonitorListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(FolderMonitorListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies when upload is failed
     *
     * @param file failed content
     * @param ex raised exception
     */
    private void notifyUploadFailed(File file, Exception ex) {
        for (FolderMonitorListener listener : getListeners()) {
            listener.uploadFailed(this, file, ex);
        }
    }


    /**
     * Notifies when upload is success
     *
     * @param file failed content
     * @param docId created document/target id
     */
    private void notifyUploadSuccess(File file, String docId) {
        for (FolderMonitorListener listener : getListeners()) {
            listener.uploadSucceeded(this, file, docId);
        }
    }

    /**
     * File lock implementation
     */
    private static class FileLock {

        private final RandomAccessFile randomAccessFile;

        private FileLock(File file) throws IOException {
            if (file.canWrite()) {
                this.randomAccessFile = new RandomAccessFile(file, "rw");
            } else {
                throw new IOException("Could not obtain lock");
            }
        }

        public static FileLock tryLock(File file) {
            try {
                return new FileLock(file);
            } catch (IOException e) {
                return null;
            }
        }

        public void unlock() {
            try {
                this.randomAccessFile.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
