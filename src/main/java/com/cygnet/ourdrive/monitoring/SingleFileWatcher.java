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
import java.sql.Timestamp;
import java.util.ArrayList;
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
        ignore = ignore || filename.endsWith("#");
        return ignore;
    }

    /**
     *
     * @param filename
     * @return
     */
    private boolean isJsonFile(String filename) {
        return filename.endsWith(".json");
    }

    /**
     *
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

                    System.out.println(event.context() + ", count: "+ event.count() + ", event: "+ event.kind());

                    WatchEvent.Kind<?> kind = event.kind();

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        logger.warn("File listener received an overflow event.  You should probably check into this");
                        overflowTriggeredFlag = true;
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                        logger.info("CHECK THIS FILE: IGNORE/BRO -> " + filename.toFile().getAbsoluteFile().toString());

                        if (!shouldIgnoreFile(filename.toString()) && !isJsonFile(filename.toString())) {
                            if (hasJsonBro(filename.toFile())) {

                                logger.info(filename.toFile().getAbsoluteFile().toString() + ": Trying to upload because of save action.");
                                this.uploadAsNewVersion(filename.toFile(), false);

                            }
                        }
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {

//                        if (shouldIgnoreFile(filename.toString()) && ! isJsonFile(filename.toString())) {
//
//                            String realFilename = getRealFilenameFromTmpFile(filename.toFile().getName());
//                            Path newFilename = Paths.get(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName()+"/"+realFilename);
//
//                            System.out.println("File "+filename.toFile().getAbsolutePath()+" exists: "+filename.toFile().exists());
//                            System.out.println("File "+newFilename.toFile().getAbsolutePath()+" has bro: "+hasJsonBro(newFilename.toFile()));
//
//                            System.out.println("File "+newFilename.toFile()+" has bro: "+hasJsonBro(newFilename.toFile()));
//
//                            if (!filename.toFile().exists() && hasJsonBro(newFilename.toFile())) {
//
//                                System.exit(0);
//
//                                logger.info(newFilename.toFile().getAbsoluteFile().toString() + ": Trying to upload because of save action.");
//                                Boolean isUploaded = this.uploadAsNewVersion(newFilename.toFile(), true);
//
//                                // delete all files, because we assume that the file was closed
//                                if(isUploaded) {
//                                    boolean origFileDeleted = false;
//
//                                    try {
//                                        origFileDeleted = newFilename.toFile().delete();
//                                    } catch(Exception e) {
//                                        logger.error("Try to delete "+newFilename.toFile().getAbsolutePath()+" with error: "+e.getMessage());
//                                    }
//
//                                    if (origFileDeleted) {
//
//                                        logger.info(newFilename.toFile().getAbsolutePath() + " has been deleted!");
//                                        logger.info("Saved and unlocked file: " + newFilename.toFile().getName());
//
//                                        File jsonFile = new File(newFilename.toFile().getParent() + File.separator + "." + newFilename.toFile().getName() + ".json");
//
//                                        try {
//                                            logger.info("Try to delete: " + jsonFile.getAbsolutePath());
//                                            boolean jsonFileDeleted = jsonFile.delete();
//
//                                            if (jsonFileDeleted) {
//                                                logger.info(jsonFile.getAbsolutePath() + " has been deleted!");
//                                                isUploaded = true;
//                                            } else {
//                                                logger.error("Delete operation is failed for " + jsonFile.getAbsolutePath());
//                                            }
//
//                                        } catch(Exception e) {
//                                            logger.error("Try to delete "+jsonFile.getAbsolutePath()+" with error: "+e.getMessage());
//                                        }
//
//                                    } else {
//                                        logger.error("Delete operation is failed for " + filename.toFile().getAbsolutePath());
//                                    }
//                                }
//
//                            }
//                        }
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
     *
     * @param filename
     * @return
     */
    private String getRealFilenameFromTmpFile(String filename) {

        //array to hold replacements
        String replacements[] = {"#","~","$",".swp",".swap",".tmp",".temp","lock"};

        //loop over the array and replace
        String replacedFilename = filename;
        for(String replacement: replacements) {
            replacedFilename = replacedFilename.replace(replacement, "");
        }

        String arr[] = replacedFilename.split("\\.");
        ArrayList<String> returnArr = new ArrayList<String>();

        for(int i = 0; i < arr.length; i++) {
            if(!arr[i].equals("")) {
                returnArr.add(arr[i]);
            }
        }

        return StringUtils.join(returnArr, ".");

    }
}
