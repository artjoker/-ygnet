package com.cygnet.ourdrive.gui;

/**
 * Created by carsten on 4/17/16.
 * com.cygnet.ourdrive.upload
 * ourdrive
 */

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.monitoring.ProcessWatcher;
import com.cygnet.ourdrive.settings.ReadProperties;
import com.cygnet.ourdrive.util.Processes;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Desktop {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);

    private Properties properties;
    private Process process;
    private ProcessWatcher pwt;

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
        return os.contains("windows") || os.contains("nt");
    }

    /**
     * @return
     */
    public static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac");
    }

    /**
     * @return
     */
    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux");
    }

    /**
     * @return boolean
     */
    public static boolean isWindows9X() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.equals("windows 95") || os.equals("windows 98");
    }

    /**
     * Open a given file object depending on the current OS
     *
     * @param file the current file object
     * @throws IOException
     */
    public void open(File file) throws IOException {
        if (isLinux()) {

            List<String> args = new ArrayList<String>();
            args.add(properties.getProperty("linux.openAs")); // command name
            args.add(file.getAbsoluteFile().toString()); // optional args added as separate list items
            ProcessBuilder pb = new ProcessBuilder(args);
            Process process = pb.start();

            try {
                HashMap processIds = Processes.GetSystemProcesses("linux");// Processes.getProcessIdsByFile(file, "linux");
                this.pwt = new ProcessWatcher(processIds, process, this.socketClient);
                this.pwt.run();
            } catch (Exception e) {
                e.printStackTrace();
            }



        } else if (isMac()) {

            List<String> args = new ArrayList<String>();
            args.add(properties.getProperty("mac.openAs")); // command name
            args.add(String.valueOf(file.getAbsoluteFile().toURI())); // optional args added as separate list items
            ProcessBuilder pb = new ProcessBuilder(args);
            Process process = pb.start();

            Processes.GetSystemProcesses("mac");
            try {
                HashMap processIds = Processes.getProcessIdsByFile(file, "mac");
                this.pwt = new ProcessWatcher(processIds, process, this.socketClient);
                this.pwt.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (isWindows() && isWindows9X()) {

            String[] Commands = new String[]{
                    "command.com",
                    "/c",
                    "cd "+file.getParent()+" && start "+file.getName()
            };

            ProcessBuilder pb = new ProcessBuilder(Commands);
            Process process = pb.start();

            try {
                HashMap processIds = Processes.GetSystemProcesses("windows"); //Processes.getProcessIdsByFile(file, "windows");
                this.pwt = new ProcessWatcher(processIds, process, this.socketClient);
                this.pwt.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (isWindows()) {

            String[] Commands = new String[]{
                    "cmd.exe",
                    "/c",
                    "cd "+file.getParent()+" && start "+file.getName()
            };

            ProcessBuilder pb = new ProcessBuilder(Commands);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                HashMap processIds = Processes.GetSystemProcesses("windows"); //Processes.getProcessIdsByFile(file, "windows");
                this.pwt = new ProcessWatcher(processIds, process, this.socketClient);
                this.pwt.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

