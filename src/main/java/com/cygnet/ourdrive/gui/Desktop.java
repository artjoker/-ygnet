package com.cygnet.ourdrive.gui;

/**
 * Created by carsten on 4/17/16.
 * com.cygnet.ourdrive.upload
 * ourdrive
 */

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.monitoring.FolderMonitor;
import com.cygnet.ourdrive.monitoring.ProcessWatcher;
import com.cygnet.ourdrive.monitoring.SingleFileWatcher;
import com.cygnet.ourdrive.settings.ReadProperties;
import com.cygnet.ourdrive.util.Processes;
import com.cygnet.ourdrive.util.WmicProcesses;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Desktop extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    private Properties properties;
    private Process process;
    private final ThreadLocal<ProcessWatcher> pwt = new ThreadLocal<>();

    private final WebSocketClient socketClient;

    /**
     * @param socketClient
     */
    public Desktop(WebSocketClient socketClient) {
        this.socketClient = socketClient;
        ReadProperties readProperties = new ReadProperties();
        try {
            properties = readProperties.getProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @return
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.toLowerCase().contains("windows") || os.toLowerCase().contains("nt");
    }

    /**
     * @return
     */
    public static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.toLowerCase().contains("mac");
    }

    /**
     * @return
     */
    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.toLowerCase().contains("linux");
    }

    /**
     * @return boolean
     */
    public static boolean isWindows9X() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.toLowerCase().equals("windows 95") || os.equals("windows 98");
    }

    /**
     * Open a given file object depending on the current OS
     *
     * @param file the current file object
     * @throws IOException
     */
    public void open(File file) throws IOException {

        Path downloadPath = Paths.get(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName());

        // add file to watcher
        logger.info("Set file watcher service to: "+downloadPath.toString());

        Thread sfwThread = null;
        sfwThread = getThreadByName("SingleFileWatcher");
        SingleFileWatcher sfw = new SingleFileWatcher(downloadPath, socketClient, true);

        if(sfwThread == null) {
            sfw.start();
            logger.info("Started file watcher service: "+sfw.getName()+" with ID: "+sfw.getId());
        } else {
//            sfw.start();
            logger.info("Use current file watcher service: "+sfwThread.getName()+" with ID: "+sfwThread.getId());
        }



        if (isLinux()) {

            String[] Commands = new String[]{
                    properties.getProperty("linux.openAs"),
                    file.getAbsolutePath()
            };

            try {
                logger.info("Trying to start command: "+properties.getProperty("linux.openAs")+" "+file.getAbsolutePath());

                ProcessBuilder pb = new ProcessBuilder(Commands).redirectErrorStream(true);
                Process process = pb.start(); // Start the process.

                InputStream stderr = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                String errorMsg = null;

                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    errorMsg += line;
                }

                process.waitFor(); // Wait for the process to finish.++

                checkProcess(file, downloadPath, process, errorMsg);

                runProcessWatcher(file, "linux");

            } catch (Exception e) {
                logger.error("Open file with application failed: "+e.getMessage());
            }

            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }






//            List<String> args = new ArrayList<String>();
//            args.add(properties.getProperty("linux.openAs")); // command name
//            args.add(file.getAbsoluteFile().toString()); // optional args added as separate list items
//
//            try {
//                logger.info("Trying to start command: linux.openAs "+file.getAbsolutePath());
//
//                ProcessBuilder pb = new ProcessBuilder(args);
//                Process process = pb.start(); // Start the process.
//                process.waitFor(); // Wait for the process to finish.
//                logger.info("Successfully opened file with an application");
//            } catch (Exception e) {
//                logger.error("Open file with application failed: "+e.getMessage());
//            }
//
//            try {
//                HashMap processIds = Processes.GetSystemProcesses(file, "linux", false);// Processes.getProcessIdsByFile(file, "linux");
//                this.pwt.set(new ProcessWatcher(file, processIds, process, this.socketClient, sfw, "linux"));
//                this.pwt.get().run();
//            } catch (Exception e) {
//                logger.warn("Get process information failed: ProcessWatcher has been stopped");
//            }



        } else if (isMac()) {

            List<String> args = new ArrayList<String>();
            args.add(properties.getProperty("mac.openAs")); // command name
            args.add(String.valueOf(file.getAbsoluteFile().toURI())); // optional args added as separate list items

            try {
                logger.info("Trying to start command: mac.openAs "+String.valueOf(file.getAbsoluteFile().toURI()));

                ProcessBuilder pb = new ProcessBuilder(args);
                Process process = pb.start(); // Start the process.
                process.waitFor(); // Wait for the process to finish.
                logger.info("Successfully opened file with an application");
            } catch (Exception e) {
                logger.error("Open file with application failed: "+e.getMessage());
            }

            runProcessWatcher(file, "mac");

        } else if (isWindows() && isWindows9X()) {

            String[] Commands = new String[]{
                    "command.com",
                    "/c",
                    "cd "+file.getParent()+" && start "+file.getName()
            };

            try {
                logger.info("Trying to start command: command.com /c cd "+file.getParent()+" && start "+file.getName());

                ProcessBuilder pb = new ProcessBuilder(Commands).redirectErrorStream(true);
                Process process = pb.start(); // Start the process.

                InputStream stderr = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                String errorMsg = null;

                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    errorMsg += line;
                }

                process.waitFor(); // Wait for the process to finish.

                checkProcess(file, downloadPath, process, errorMsg);

            } catch (Exception e) {
                logger.error("Open file with application failed: "+e.getMessage());
            }

//            try {
//                Thread.sleep(3000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            runProcessWatcher(file, "windows");

        } else if (isWindows()) {

            String[] Commands = new String[]{
                    "cmd.exe",
                    "/c",
                    "cd "+file.getParent()+" && start "+file.getName()
            };

            try {
                logger.info("Trying to start command: cmd.exe /c cd "+file.getParent()+" && start "+file.getName());

                ProcessBuilder pb = new ProcessBuilder(Commands).redirectErrorStream(true);
                Process process = pb.start(); // Start the process.

                InputStream stderr = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                String errorMsg = null;

                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    errorMsg += line;
                }

                process.waitFor(); // Wait for the process to finish.

                checkProcess(file, downloadPath, process, errorMsg);

            } catch (Exception e) {
                logger.error("Open file with application failed: "+e.getMessage());
            }

/*            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            runProcessWatcher(file, "windows");

        } else {
            logger.warn("No OS given?");
        }
    }

    private void runProcessWatcher(File file, String os) {
        try {

            ArrayList processIds = WmicProcesses.GetSystemProcesses(file, os); //Processes.getProcessIdsByFile(file, "windows");
            this.pwt.set(new ProcessWatcher(file, processIds, process, this.socketClient, os));
//                this.pwt.set(new ProcessWatcher(file, null, process, this.socketClient, os));
            this.pwt.get().run();
        } catch (Exception e) {
            logger.warn("Get process information failed: ProcessWatcher has been stopped");
        }
    }

    private void checkProcess(File file, Path downloadPath, Process process, String errorMsg) {
        if(process.exitValue() != 0) {
            logger.info("The file with application is closed!");
            File jsonFile = new File(downloadPath.toAbsolutePath().toString() + File.separator + "." + file.getName() + ".json");
            deleteFile(jsonFile);
            deleteFile(file);
        }
    }

    private void deleteFile(File file) {
        if(file.exists()) {
            Boolean isDeleted = file.getAbsoluteFile().delete();
            if(isDeleted) {
                logger.info(file.getAbsoluteFile()+" has been deleted!");
            } else {
                logger.error("Could not delete "+file.getAbsoluteFile()+"!");
            }
        } else {
            logger.info("Could not find "+file.getAbsoluteFile()+"!");
        }
    }

    private Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }


    public Process getProcess() {
        return process;
    }

    /**
     * @param file the current file object
     */
    public void imaginaryAction(File file) {
        throw new UnsupportedOperationException();
    }

}

