package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by casten on 5/12/16.
 */
public class SingleFileWatcher extends Thread {
    private final Path downloadPath;
    private final WebSocketClient socketClient;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    boolean overflowTriggeredFlag = false;

    /**
     * @param downloadPath
     * @param socketClient
     */
    public SingleFileWatcher(Path downloadPath, WebSocketClient socketClient) {
        this.downloadPath = downloadPath;
        this.socketClient = socketClient;
    }

    /**
     * @param filename
     * @return
     */
    private boolean shouldIgnoreFile(String filename) {
        boolean ignore = filename.startsWith("#");
        ignore = ignore || filename.startsWith("~");
        ignore = ignore || filename.startsWith(".");
        ignore = ignore || filename.startsWith("$");
        ignore = ignore || filename.endsWith(".swp");
        ignore = ignore || filename.endsWith(".swap");
        ignore = ignore || filename.endsWith(".tmp");
        ignore = ignore || filename.endsWith(".temp");
        return ignore;
    }

    public boolean isStopped() {
        return stop.get();
    }

    public void stopThread() {
        stop.set(true);
    }

    /**
     * upload modified file as new version
     *
     * @param modifiedFile the current file with changes
     * @param unlock       unlock this file at Cygnet?
     */
    public void uploadAsNewVersion(File modifiedFile, Boolean unlock) {

        try {
            Boolean isUploaded = socketClient.uploadAsNewVersionRequest(modifiedFile, unlock);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void run() {

        try {

            FileSystem fs = FileSystems.getDefault();
            WatchService watcher = fs.newWatchService();

            Path path = fs.getPath(downloadPath.toAbsolutePath().toString());

            path.register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );

            while (!isStopped()) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {

                    Date date = new Date();
                    Timestamp ts = new Timestamp(date.getTime());

                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        logger.warn("File listener received an overflow event.  You should probably check into this");
                        overflowTriggeredFlag = true;
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                        logger.debug("CHECK THIS FILE: IGNORE/BRO -> " + filename.toFile().getAbsoluteFile().toString());

                        if (!shouldIgnoreFile(filename.toString())) {
                            if (hasJsonBro(filename.toFile())) {

                                logger.debug(filename.toFile().getAbsoluteFile().toString() + ": Trying to upload because of save action.");
                                uploadAsNewVersion(filename.toFile(), false);

                            }
                        }
                    }

                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }

            }

        } catch (IOException e) {
            logger.error(e.getMessage());
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
