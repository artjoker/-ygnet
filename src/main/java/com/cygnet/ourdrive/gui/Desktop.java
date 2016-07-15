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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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

//            try {
//                process.waitFor();
//            } catch (InterruptedException e) {
//                logger.error(e.getMessage());
//            }

            // start process watcher onto application which opened the file
            Processes.CrunchifySystemProcess("linux");
//            try {
            HashMap processIds = Processes.getProcessIdsByFile(file);
            this.pwt = new ProcessWatcher(processIds, process, this.socketClient);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            if (this.pwt.isStopped()) {
                this.pwt.run();
            }


        } else if (isMac()) {
            process = Runtime.getRuntime().exec(String.format("%s %s", properties.getProperty("mac.openAs"), file.getAbsoluteFile().toURI()));

            try {
                if (process.waitFor() != 0) {
                    logger.warn("Couldn't open file");
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        } else if (isWindows() && isWindows9X()) {
//            process = Runtime.getRuntime().exec( String.format("command.com /C start %s", file.getAbsoluteFile()) );
            process = Runtime.getRuntime().exec(String.format("%s %s", properties.getProperty("windows95.openAs"), file.getAbsoluteFile()));

            try {
                if (process.waitFor() != 0) {
                    logger.warn("Couldn't open file");
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        } else if (isWindows()) {
//            process = Runtime.getRuntime().exec( String.format("cmd /C start %s", file.getAbsoluteFile()) );
            process = Runtime.getRuntime().exec(String.format("%s %s", properties.getProperty("windowsNew.openAs"), file.getAbsoluteFile()));

            try {
                if (process.waitFor() != 0) {
                    logger.warn("Couldn't open file");
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
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

