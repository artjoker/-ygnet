package com.cygnet.ourdrive.monitoring;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.util.DirUtils;
import com.cygnet.ourdrive.util.Processes;
import com.cygnet.ourdrive.util.WmicProcesses;
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

//    private  Path downloadPath;

    private ArrayList processIds;
    private AtomicBoolean stop = new AtomicBoolean(false);
    private Process process;
//    private Long sfwThreadId;

    public ProcessWatcher() {

    }

    public ProcessWatcher(File file, ArrayList processIds, Process process, WebSocketClient socketClient, String OS) {
//    public ProcessWatcher(File file, HashMap processIds, Process process, WebSocketClient socketClient, String OS, Path downloadPath) {
        this.processIds = processIds;
        this.process = process;
        this.setName("ApplicationWatcher");
        this.socketClient = socketClient;
        this.OS = OS;
        this.file = file;
//        this.downloadPath = downloadPath;
//        this.sfwThreadId = sfwThreadId;
    }

    public boolean isStopped() {
        return stop.get();
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
     *
     */
    private void stopThread() {
//        sfwThread.stopThread();
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
//            Boolean fileUpload = socketClient.uploadAsNewVersionRequest(modifiedFile, unlock);

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
            ArrayList<String[]> processesList = WmicProcesses.GetSystemProcesses(this.file, this.OS); // all current processes
            // String filenameWithoutExtension = FilenameUtils.removeExtension(this.file.getName());

            Boolean hasAccessMask = false;

            /*
            1 extension
            2 filename
            4 path
             */

            for (String[] content: processesList) {
                File accessMaskFile = new File(content[2]+"."+content[1]);
//                System.out.println(accessMaskFile.getName());
//                System.out.println(this.file.getName());
                if(!shouldIgnoreFile(accessMaskFile) && accessMaskFile.getName().equals(this.file.getName())) {
                    hasAccessMask = true;
                }
            }

            switch(this.OS) {
                case "windows":

                   if(isFileLocked(this.file)) {
                        deleteCurrentFile(this.file);
                        stopThread();
                    }

                    if (hasJsonBro(this.file) && hasAccessMask) {
                        if (this.uploadAsNewVersion(this.file, true)) {
                            Processes.setTitleDocument("");
                            Processes.setTitleNotAvailable("");
                            Processes.setTitleOnlyFileClosed("");
//                                    Thread fileWatcher = getThreadById(sfwThreadId);
                            stopThread();
                        }
                    }
                    break;

                case "mac":
                    break;

                case "linux":
                    if (hasJsonBro(file)) {
                        if (this.uploadAsNewVersion(file, true)) {
                            this.stopThread();
                        }
                    }
                    break;
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                logger.error("Pause ProcessWatcher thread for 0.2 sec failed: "+e.getMessage());
            }

        }
    }

    private void deleteCurrentFile(File file) {

        File jsonFile = new File(file.getParent() + File.separator + "." + file.getName() + ".json");
        DirUtils.deleteFile(jsonFile);
        logger.info(jsonFile.getAbsoluteFile()+" has been deleted!");
        DirUtils.deleteFile(file);
        logger.info(file.getAbsoluteFile()+" has been deleted!");

    }

    private boolean isFileLocked(File file) {
        return file.renameTo(file);
    }
    /**
     *
     * @param threadId
     * @return
     */
    private Thread getThreadById(Long threadId) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getId() == threadId) {
                return t;
            }
        }
        return null;
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
