package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
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
        ignore = ignore || filename.endsWith("#");
        return ignore;
    }

    /**
     * @param filename
     * @return
     */
    private boolean isJsonFile(String filename) {
        return filename.endsWith(".json");
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
            return socketClient.uploadAsNewVersionRequest(modifiedFile, unlock);
        } catch (Exception e) {
            logger.error(e.getMessage());
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

                        if (!shouldIgnoreFile(file.getName()) && !isJsonFile(file.getName())) {
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
                        logger.info("Created file: " + file.getAbsoluteFile().toString());
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        logger.info("Deleted file: " + file.getAbsoluteFile().toString());
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


    /**
     * @param filename
     * @return
     */
    private String getRealFilenameFromTmpFile(String filename) {

        //array to hold replacements
        String replacements[] = {"#", "~", "$", ".swp", ".swap", ".tmp", ".temp", "lock"};

        //loop over the array and replace
        String replacedFilename = filename;
        for (String replacement : replacements) {
            replacedFilename = replacedFilename.replace(replacement, "");
        }

        String arr[] = replacedFilename.split("\\.");
        ArrayList<String> returnArr = new ArrayList<String>();

        for (int i = 0; i < arr.length; i++) {
            if (!arr[i].equals("")) {
                returnArr.add(arr[i]);
            }
        }

        return StringUtils.join(returnArr, ".");

    }
}
