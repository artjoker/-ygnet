package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.util.Processes;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by casten on 5/20/16.
 */
public class ProcessWatcher extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    //    private final WebSocketClient socketClient;
    private WebSocketClient socketClient = null;

    private String OS = "";

    private File file;

    private HashMap processIds;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private Process process;
    private SingleFileWatcher sfwThread;

    public ProcessWatcher() {

    }

    public ProcessWatcher(File file, HashMap processIds, Process process, WebSocketClient socketClient, SingleFileWatcher swf, String OS) {
        this.processIds = processIds;
        this.process = process;
        this.setName("ApplicationWatcher");
        this.socketClient = socketClient;
        this.OS = OS;
        this.file = file;
        sfwThread = swf;
    }

    public boolean isStopped() {
        return stop.get();
    }

    /**
     *
     */
    private void stopThread() {
        sfwThread.stopThread();
        logger.info("running garbage collector to clean application.");
        System.gc();

        stop.set(true);
        process.destroy();
    }

    /**
     *
     */
    public void setStop()
    {
        Thread.currentThread().interrupt();
        this.process.destroy();
    }


    /**
     *
     * @param modifiedFile
     * @param unlock
     * @return
     */
    private boolean uploadAsNewVersion(File modifiedFile, Boolean unlock) {

        Boolean uploadProcessSuccessful = false;
        try {
            Boolean fileUpload = socketClient.uploadAsNewVersionRequest(modifiedFile, unlock, this.getName());

            if (fileUpload) {

                try {
                    Boolean origFileDeleted = modifiedFile.delete();

                    if (origFileDeleted) {

                        logger.info(modifiedFile.getAbsolutePath() + " has been deleted!");
                        logger.info("Saved and unlocked file: " + modifiedFile.getName() + " on Cygnet");

                        File jsonFile = new File(modifiedFile.getParent() + File.separator + "." + modifiedFile.getName() + ".json");

                        try {
                            logger.info("Try to delete: " + jsonFile.getAbsolutePath() + " locally.");
                            boolean jsonFileDeleted = jsonFile.delete();

                            if (jsonFileDeleted) {
                                logger.info("Local file " + jsonFile.getAbsolutePath() + " has been deleted!");
                                uploadProcessSuccessful = true;
                            } else {
                                logger.error("Delete operation is failed for local " + jsonFile.getAbsolutePath());
                            }

                        } catch(Exception e) {
                            logger.error("Try to delete "+jsonFile.getAbsolutePath()+" with error: "+e.getMessage());
                        }

                    } else {
                        logger.error("Local delete operation is failed for " + modifiedFile.getAbsolutePath());
                    }

                } catch(Exception e) {
                    logger.error("Try to delete "+modifiedFile.getAbsolutePath()+" with error: "+e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Try to upload "+modifiedFile.getAbsolutePath()+" with error: "+e.getMessage());
        }

        return uploadProcessSuccessful;
    }


    @Override
    public void run() {

        Processes.setPid("0");

        while (!isStopped()) {

            // [pid][detailed title with file name]
            HashMap processesList = Processes.GetSystemProcesses(this.file, this.OS, false); // all current processes
            Boolean onlyFileHasClosed = false;

            // [pid][detailed title with file name]
            for (Object o : this.processIds.entrySet()) { // processIds is the small array
                Map.Entry pair = (Map.Entry) o;

                // contain only pid
                List<String> allpIds = new ArrayList<String>();

//                logger.info("Actual process list values:");
                for (Object processInfo : processesList.entrySet()) {
                    Map.Entry processPair = (Map.Entry) processInfo;
//                    logger.info("Key: "+processPair.getKey().toString()+", -> Value: "+processPair.getValue().toString());

                    allpIds.add(processPair.getKey().toString());

                }

                switch(this.OS) {
                    case "windows":
                        // 2015_08_04_IMG_0082-uuu
//                        logger.info("All Ids: "+allpIds.size()+" | First Process Id: "+Processes.getPid());
//                        logger.info("titleDocument: "+Processes.getTitleDocument());
//                        logger.info("titleNotAvailable: "+Processes.getTitleNotAvailable());
//                        logger.info("titleOnlyFileClosed: "+Processes.getTitleOnlyFileClosed());

                        // check also if process id is still there

                        if (allpIds.size() == 0 || !Processes.getTitleOnlyFileClosed().equals("")) {
                            File file = new File(pair.getValue().toString());
                            if (hasJsonBro(file)) {
                                if (this.uploadAsNewVersion(file, true)) {
                                    Processes.setTitleDocument("");
                                    Processes.setTitleNotAvailable("");
                                    Processes.setTitleOnlyFileClosed("");
                                    stopThread();
                                }
                            }
                        }
                        break;

                    case "mac":
                        break;

                    case "linux":
                        if (!allpIds.contains(pair.getKey().toString()) || allpIds.size() < this.processIds.size()) {
                            File file = new File(pair.getValue().toString());
                            if (hasJsonBro(file)) {
                                if (this.uploadAsNewVersion(file, true)) {
                                    this.stopThread();
                                }
                            }
                        }
                        break;
                }
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                logger.error("Pause ProcessWatcher thread for 0.2 sec failed: "+e.getMessage());
            }

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

        return testFile.exists();
    }

}
