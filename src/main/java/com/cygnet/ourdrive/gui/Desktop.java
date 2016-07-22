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

            Processes.CrunchifySystemProcess("linux");
            try {
                HashMap processIds = Processes.getProcessIdsByFile(file);
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

            Processes.CrunchifySystemProcess("mac");
            try {
                HashMap processIds = Processes.getProcessIdsByFile(file);
                this.pwt = new ProcessWatcher(processIds, process, this.socketClient);
                this.pwt.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (isWindows() && isWindows9X()) {

            String command = "command.com /C start " + file.getAbsolutePath();
            System.out.println("--------------------------------------------");
            System.out.println(command);
            System.out.println("--------------------------------------------");

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            Processes.CrunchifySystemProcess("windows");
            try {
                HashMap processIds = Processes.getProcessIdsByFile(file);
                this.pwt = new ProcessWatcher(processIds, process, this.socketClient);
                this.pwt.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (isWindows()) {

//            String command = "notepad " + file.getAbsolutePath();

            String[] MyCommands = new String[]{
                    "cmd.exe",
                    "/c",
                    "cd "+file.getPath(),
                    "&& start "+file.getName()
            };

//            String command = "cmd /C start c:\\Users\\carsten\\AppData\\Local\\Cygnet\\Ourdrive\\ourdrive_downloads\\invoice.jpg";
                    // + file.getAbsolutePath();
            System.out.println("--------------------------------------------");
            System.out.println(Arrays.toString(MyCommands));
            System.out.println(file.getAbsolutePath());
            System.out.println("--------------------------------------------");

            ProcessBuilder pb = new ProcessBuilder(MyCommands);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            Processes.CrunchifySystemProcess("windows");
            try {
                HashMap processIds = Processes.getProcessIdsByFile(file);
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

