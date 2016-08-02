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

    private String OS = "";

    private File file;

    private HashMap processIds;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private Process process;

    public ProcessWatcher() {

    }

    public ProcessWatcher(File file, HashMap processIds, Process process, WebSocketClient socketClient, String OS) {
        this.processIds = processIds;
        this.process = process;
        this.setName("ApplicationWatcher");
        this.socketClient = socketClient;
        this.OS = OS;
        this.file = file;
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
//        if (!isStopped()) {
//            GlobalSettings globalSettings = GlobalSettings.getInstance();

//            Path downloadPath = Paths.get(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName());

            try {

                isUploaded = socketClient.uploadAsNewVersionRequest(modifiedFile, unlock);
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
//        } else {
//            logger.error("ProcessWatcher seems o be stopped");
//        }

        return isUploaded;
    }

    public void setStop()
    {
        Thread.currentThread().interrupt();
        this.process.destroy();
    }


    @Override
    public void run() {


        while (!isStopped()) {

            // [pid][detailed title with file name]
            HashMap processesList = Processes.GetSystemProcesses(this.file, this.OS, false); // all current processes


            // [pid][detailed title with file name]
            for (Object o : this.processIds.entrySet()) { // processIds is the small array
                Map.Entry pair = (Map.Entry) o;

                // contain only pid
                List<String> allpIds = new ArrayList<String>();
//                String[] allpIds = new String[2];

                for (Object processInfo : processesList.entrySet()) {
                    Map.Entry processPair = (Map.Entry) processInfo;
                    allpIds.add(processPair.getKey().toString());
                }

                System.out.println("all ids size: "+allpIds.size());
                System.out.println("process ids size: "+this.processIds.size());


                if (!allpIds.contains(pair.getKey().toString()) || allpIds.size() < this.processIds.size()) {
                    File file = new File(pair.getValue().toString());
                    if (hasJsonBro(file)) {
                        if (this.uploadAsNewVersion(file, true)) {
                            this.stopThread();
                        }
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
