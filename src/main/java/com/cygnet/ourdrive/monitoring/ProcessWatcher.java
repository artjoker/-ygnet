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
//        this.processIds = processIds;
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
        this.interrupt();
    }

    private boolean uploadAsNewVersion(File modifiedFile, Boolean unlock) {

        Boolean isUploaded = false;
        if (!isStopped()) {
            GlobalSettings globalSettings = GlobalSettings.getInstance();

            Path downloadPath = Paths.get(globalSettings.getDownloadPath() + "/" + OurDriveService.getDownloadFolderName());

            try {

                isUploaded = this.socketClient.uploadAsNewVersionRequest(modifiedFile, unlock);
                if (isUploaded) {

                    if (modifiedFile.delete()) {

                        logger.info(modifiedFile.getName() + " is deleted!");
                        logger.info("Saved and unlocked file: " + modifiedFile.getName());

                        // and now delete the json file
                        try {
                            File jsonFile = new File(downloadPath.toAbsolutePath().toString() + File.separator + "." + modifiedFile.getName() + ".json");
                            if (jsonFile.delete()) {
                                logger.info(jsonFile.getName() + " has been deleted!");
//                                socketClient.disconnect();
                                isUploaded = true;
                            } else {
                                logger.error("Delete operation is failed for " + jsonFile.getName());
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        }
                    } else {
                        logger.error("Delete operation is failed for " + modifiedFile.getName());
                    }

                }

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.error("ProcessWatcher seems o be stopped");
        }

        return isUploaded;
    }


    @Override
    public void run() {

        logger.debug("is here something going on??????????????????????");
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
                    logger.debug("BRO CHEGGA: " + file.toString());
                    if (hasJsonBro(file)) if (uploadAsNewVersion(file, true)) {
                        this.stopThread();
//                        socketClient.disconnect();
                    }
                }

            }

            try {
                Thread.sleep(1000L);
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

        Path downloadPath = Paths.get(globalSettings.getDownloadPath() + "/" + OurDriveService.getDownloadFolderName());
        File testFile = new File(downloadPath.toAbsolutePath().toString() + File.separator + "." + toCheckFile.getName() + ".json");

        return testFile.exists();
    }

}
