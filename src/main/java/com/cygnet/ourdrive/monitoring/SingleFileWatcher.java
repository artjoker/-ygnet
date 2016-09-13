package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by casten on 5/12/16.
 */
public class SingleFileWatcher extends Thread {
    private final Path downloadPath;
    private final WebSocketClient socketClient;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    private File downloadedFile;

    boolean overflowTriggeredFlag = false;

    private static SingleFileWatcher instance;

    public static SingleFileWatcher getInstance(Path downloadPath, WebSocketClient socketClient, File downloadedFile)
    {
        if (instance == null) {
            instance = new SingleFileWatcher(downloadPath, socketClient, downloadedFile);
        }
        return instance;
    }

//    private SingleFileWatcher() {}

    /**
     * @param downloadPath
     * @param socketClient
     */
    public SingleFileWatcher(Path downloadPath, WebSocketClient socketClient, File downloadedFile) {
        this.downloadPath = downloadPath;
        this.socketClient = socketClient;
        this.downloadedFile = downloadedFile;
        this.setName("DownloadFileWatcher");
    }

    /**
     * @param file
     * @return
     */
    private boolean shouldIgnoreFile(File file) {

        if(file.isDirectory()) {
            return true;
        }

        if(file.isHidden()) {
            return true;
        }

        String filename = file.getName();
        String extension = "";
        extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());

        if(extension.equals("")) {
            return true;
        }

        boolean ignore = filename.toLowerCase().startsWith("#");
        ignore = ignore || filename.toLowerCase().startsWith("~");
        ignore = ignore || filename.toLowerCase().startsWith(".");
        ignore = ignore || filename.toLowerCase().startsWith("$");
        ignore = ignore || filename.toLowerCase().endsWith(".swp");
        ignore = ignore || filename.toLowerCase().endsWith(".swap");
        ignore = ignore || filename.toLowerCase().endsWith(".tmp");
        ignore = ignore || filename.toLowerCase().endsWith(".temp");
        ignore = ignore || filename.toLowerCase().endsWith(".json");
        ignore = ignore || filename.endsWith("#");
        return ignore;
    }

    /**
     * @param filename
     * @return
     */
    private boolean isJsonFile(String filename) {
        return filename.toLowerCase().endsWith(".json");
    }

    /**
     * @return
     */
    public boolean isStopped() {
        return stop.get();
    }

    /**
     *
     */
    public void stopThread() {
        stop.set(true);
    }

    /**
     * upload modified file as new version
     *
     * @param modifiedFile the current file with changes
     * @param unlock       unlock this file at Cygnet?
     */
    public boolean uploadAsNewVersion(File modifiedFile, Boolean unlock) {
        try {
            return socketClient.uploadAsNewVersionRequest(modifiedFile, unlock, this.getName());
        } catch (Exception e) {
            logger.error("Uploading as new version failed: "+e.getMessage());
        }
        return false;
    }

    @Override
    public void run() {

        try {

            FileSystem fs = FileSystems.getDefault();
            WatchService watcher = fs.newWatchService();

            Path path = fs.getPath(downloadPath.toAbsolutePath().toString());

            path.register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            );

            while (!isStopped()) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    File file = new File(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName() + "/" + ev.context());

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        logger.warn("File listener received an overflow event.  You should probably check into this");
                        overflowTriggeredFlag = true;
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                        if (!shouldIgnoreFile(file) && !isJsonFile(file.getName())) {
                            logger.info("Check if file has a JSON sibling: " + file.getAbsoluteFile().toString());
                            if (hasJsonBro(file)) {
                                logger.info("JSON sibling check OK for: " + file.getAbsoluteFile().toString());
                                logger.info("Trying to upload because of save action: " + file.getAbsoluteFile().toString());
                                Boolean uploadAsNewVersion = uploadAsNewVersion(file, false);
                                if (uploadAsNewVersion) {
                                    logger.info("Upload successful for: " + file.getAbsoluteFile().toString());
                                }
                            }

                        } else {
                            logger.info("No modify action for: " + file.getAbsoluteFile().toString());
                        }

                    } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
//                        logger.info("Created file: " + file.getAbsoluteFile().toString());
//                        this.stopThread();
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
//                        logger.info("Deleted file: " + file.getAbsoluteFile().toString());
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }

            }

        } catch (IOException e) {
            logger.error("Creating single file watcher process failed: "+e.getMessage());
        }

    }

    /**
     * Check if a given file has a JSON brother
     *
     * @param toCheckFile the file to check
     * @return boolean
     */
    private boolean hasJsonBro(File toCheckFile) {

        GlobalSettings globalSettings = GlobalSettings.getInstance();

        Path downloadPath = Paths.get(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName());
        File testFile = new File(downloadPath.toAbsolutePath().toString() + File.separator + "." + toCheckFile.getName() + ".json");
        logger.debug("check bro with: " + downloadPath.toAbsolutePath().toString() + File.separator + "." + toCheckFile.getName() + ".json");
        return testFile.exists();
    }

}
