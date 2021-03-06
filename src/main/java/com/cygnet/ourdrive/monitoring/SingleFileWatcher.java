package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.util.WmicProcesses;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by casten on 5/12/16.
 */
public class SingleFileWatcher extends Thread {
    private final Path downloadPath;
    private final WebSocketClient socketClient;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

//    private File downloadedFile;

    boolean overflowTriggeredFlag = false;

    boolean justDownloaded = false;

    private static SingleFileWatcher instance;

    public static SingleFileWatcher getInstance(Path downloadPath, WebSocketClient socketClient, Boolean justDownloaded) {
        if (instance == null) {
            instance = new SingleFileWatcher(downloadPath, socketClient, justDownloaded);
        }
        return instance;
    }

    public boolean isJustDownloaded() {
        return justDownloaded;
    }

    public void setJustDownloaded(boolean justDownloaded) {
        this.justDownloaded = justDownloaded;
    }

    //    private SingleFileWatcher() {}

    /**
     * @param downloadPath
     * @param socketClient
     */
    public SingleFileWatcher(Path downloadPath, WebSocketClient socketClient, Boolean justDownloaded) {
        this.downloadPath = downloadPath;
        this.socketClient = socketClient;
        this.justDownloaded = justDownloaded;
//        this.downloadedFile = downloadedFile;
        this.setName("SingleFileWatcher");
    }

    /**
     * @param file
     * @return
     */
    private boolean shouldIgnoreFile(File file) {

        if (file.isDirectory()) {
            return true;
        }

        if (file.isHidden()) {
            return true;
        }

        String filename = file.getName();
        String extension = "";
        extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());

        if (extension.equals("")) {
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
        ignore = ignore || !filename.contains(".");
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
//            return socketClient.uploadAsNewVersionRequest(modifiedFile, unlock, this.getName());
            return socketClient.uploadAsNewVersionRequest(modifiedFile, unlock, "DownloadFileWatcher");
        } catch (Exception e) {
            logger.error("Uploading as new version failed: "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

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
                    Thread.sleep(1000);
                    key = watcher.take();
                } catch (InterruptedException e) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    System.out.println(event.context() + ", count: " + event.count() + ", event: " + event.kind());


                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    File file = new File(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName() + "/" + ev.context());
                    if(shouldIgnoreFile(file)) continue;


                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        logger.warn("File listener received an overflow event.  You should probably check into this");
                        overflowTriggeredFlag = true;
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {


//                        Boolean hasAccessMask = false;
//                        ArrayList<String[]> processesList = WmicProcesses.GetSystemProcesses(file, "windows"); // all current processes
//                        for (String[] content: processesList) {
//                            File tmpFile = new File(content[2]+"."+content[1]);
//                            if(!shouldIgnoreFile(tmpFile) && tmpFile.getName().equals(file.getName())) {
//                                hasAccessMask = true;
//                            }
//                        }

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
                        logger.info("Created file: " + file.getAbsoluteFile().toString());
//                        this.stopThread();
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
            logger.error("Creating single file watcher process failed: " + e.getMessage());
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
