package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by casten on 5/20/16.
 */
public class ProcessWatcher extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    //    private final WebSocketClient socketClient;
    private WebSocketClient socketClient = null;

    private HashMap processIds;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private Process process;

    public ProcessWatcher() {

    }

    public ProcessWatcher(HashMap processIds, Process process, WebSocketClient socketClient) {
        this.processIds = processIds;
        this.process = process;
        this.setName("ApplicationWatcher");
        this.socketClient = socketClient;
    }

    public boolean isStopped() {
        return stop.get();
    }

    private void stopThread() {
        stop.set(true);
        process.destroy();
//        this.interrupt();
    }

    private boolean uploadAsNewVersion(File modifiedFile, Boolean unlock) {

        Boolean isUploaded = false;
        if (!isStopped()) {
            GlobalSettings globalSettings = GlobalSettings.getInstance();

            Path downloadPath = Paths.get(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName());

            try {

                isUploaded = this.socketClient.uploadAsNewVersionRequest(modifiedFile, unlock);
                if (isUploaded) {

                    boolean origFileDeleted = false;

                    try {
                        origFileDeleted = modifiedFile.delete();
                    } catch(Exception e) {
                        logger.error("Try to delete "+modifiedFile.getAbsolutePath()+" with error: "+e.getMessage());
                    }

                    if (origFileDeleted) {

                        logger.info(modifiedFile.getAbsolutePath() + " has been deleted!");
                        logger.info("Saved and unlocked file: " + modifiedFile.getName());

                        File jsonFile = new File(modifiedFile.getParent() + File.separator + "." + modifiedFile.getName() + ".json");

                        try {
                            logger.info("Try to delete: " + jsonFile.getAbsolutePath());
                            boolean jsonFileDeleted = jsonFile.delete();

                            if (jsonFileDeleted) {
                                logger.info(jsonFile.getAbsolutePath() + " has been deleted!");
                                isUploaded = true;
                            } else {
                                logger.error("Delete operation is failed for " + jsonFile.getAbsolutePath());
                            }

                        } catch(Exception e) {
                            logger.error("Try to delete "+jsonFile.getAbsolutePath()+" with error: "+e.getMessage());
                        }

                    } else {
                        logger.error("Delete operation is failed for " + modifiedFile.getAbsolutePath());
                    }

                }

            } catch (Exception e) {
                logger.error("Try to upload "+modifiedFile.getAbsolutePath()+" with error: "+e.getMessage());
            }
        } else {
            logger.error("ProcessWatcher seems o be stopped");
        }

        return isUploaded;
    }


    @Override
    public void run() {

        while (!isStopped()) {

            for (Object o : processIds.entrySet()) {
                Map.Entry pair = (Map.Entry) o;

                List<ProcessInfo> processesList = JProcesses.getProcessList();
                List<Integer> allpIds = new ArrayList<Integer>();

                for (ProcessInfo processInfo : processesList) {
                    allpIds.add(Integer.parseInt(processInfo.getPid()));
                }


                if (!allpIds.contains(pair.getKey())) {
                    File file = new File(pair.getValue().toString());
                    if (hasJsonBro(file)) if (uploadAsNewVersion(file, true)) {
                        this.stopThread();
                    }
                }

            }

            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
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
