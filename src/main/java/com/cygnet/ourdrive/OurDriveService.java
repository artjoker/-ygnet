package com.cygnet.ourdrive;

import com.cygnet.ourdrive.gui.GeneralSettingsDialog;
import com.cygnet.ourdrive.gui.OurDriveGUI;
import com.cygnet.ourdrive.gui.SystrayItem;
import com.cygnet.ourdrive.gui.SytrayInitializationException;
import com.cygnet.ourdrive.monitoring.FolderMonitor;
import com.cygnet.ourdrive.monitoring.FolderMonitorListener;
import com.cygnet.ourdrive.monitoring.FolderMonitorTask;
import com.cygnet.ourdrive.monitoring.SingleFileWatcher;
import com.cygnet.ourdrive.monitoring.exceptions.OurdriveException;
import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.upload.UploadDirectoryHandler;
import com.cygnet.ourdrive.upload.UploadFileHandler;
import com.cygnet.ourdrive.upload.UploadServiceException;
import com.cygnet.ourdrive.websocket.LocalWebServer;
import com.cygnet.ourdrive.websocket.WebSocketClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Timer;

/**
 * Application entry point!
 */
public final class OurDriveService implements GlobalSettings.SettingsListener<GlobalSettings>, FolderMonitorListener {

    public static final String VERSION = "3.0.0";

    private static String ourdriveId = "";

    private static final long TIMER_PERIOD = 1000L;

    private static final long INTERVAL_SECONDS = 3 * 1000L;

    private static OurDriveService instance;
    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);
    private static final String IMPORT_FOLDER_ID = "83541";

    private volatile boolean running = false;

    private final UploadEventLogger uploadEventLogger = new UploadEventLogger();

    private Map<File, Timer> timers = new LinkedHashMap<File, Timer>();

    private boolean dialogDismissedForever = false;

    private WebSocketClient socketClient;


    private static String download_folder_name = "ourdrive_downloads";

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Could not set Java Swing look and feel", e);
        }
    }

    public static synchronized OurDriveService getInstance() {
        if (instance == null) {
            instance = new OurDriveService();
        }
        return instance;
    }

    /**
     * @return
     */
    private static void createUUID() {
        String uniqueID = UUID.randomUUID().toString();
        ourdriveId = uniqueID;
    }

    /**
     * @return
     */
    public static String getOurdriveId() {
        return ourdriveId;
    }

    /**
     * Start this service instance
     */
    public void start(boolean configure) {
        if (!running) {
            run(configure);
        } else {
            logger.info("OurDrive service is already running");
        }
    }

    /**
     * Stop this service instance
     */
    public void stop() {

        logger.info("Closing application and cleanup "+getUserDataDirectory() + "/" + download_folder_name);
        deleteDownloadFolderContent(new File(getUserDataDirectory() + "/" + download_folder_name));

        running = false;
        synchronized (this) {
            notify();
        }
    }

    /**
     *
     * @param folder
     */
    private static void deleteDownloadFolderContent(File folder) {
        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(boolean configure) {
        GlobalSettings globalSettings = GlobalSettings.getInstance();
        restartTimers(globalSettings);
        globalSettings.addListener(this);
        running = true;
        logger.info("Loading systray item");
        try {
            SystrayItem.register(VERSION);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SytrayInitializationException e) {
            logger.warn("Could not initialize systray item", e);
        }

        checkDownloadFolder(globalSettings);

        logger.info("OurDrive service started");

        try {
            // open websocket
            socketClient = new WebSocketClient();
            socketClient.connect();

        } catch (InterruptedException ex) {
            logger.error("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            logger.error("URISyntaxException exception: " + ex.getMessage());
        } catch (Exception e) {
            logger.error("Websocket Exception: " + e.getMessage());
        }

        Path downloadPath = Paths.get(OurDriveService.getUserDataDirectory() + "/" + OurDriveService.getDownloadFolderName());

        // add file to watcher
        SingleFileWatcher sfw = new SingleFileWatcher(downloadPath, socketClient);
        sfw.start();

        // create and delete tmp file
        String tmpFileName = "initializer.tmp";
        File tmpFile = new File(downloadPath+"/"+tmpFileName);

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }

        boolean tmpDeleted = tmpFile.delete();


        // now configure
        if (configure) {
            GeneralSettingsDialog.showDialog();
        }
        while (running) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        globalSettings.removeListener(this);
        stopAllTimers();
        try {
            globalSettings.save();
        } catch (IOException e) {
            logger.warn("Could not save settings", e);
        }
        logger.info("OurDrive service stopped");
        System.exit(0);
    }

    private void restartTimers(GlobalSettings globalSettings) {
        stopAllTimers();
        for (FolderSettings folderSettings : globalSettings.getAllFolderSettings()) {
            //Start timer task for this folder
            startTimer(folderSettings);
        }
    }

    /**
     * @param settings
     */
    private void checkDownloadFolder(GlobalSettings settings) {
        Path userDirPath = Paths.get(getUserDataDirectory());
        Path downloadPath = Paths.get(getUserDataDirectory() + "/" + download_folder_name);

        logger.info("Checking download folder path: " + downloadPath.toAbsolutePath().toString());

        if (!Files.exists(userDirPath)) {
            try {
                Files.createDirectory(userDirPath);
                Files.createDirectory(downloadPath);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            if (!Files.exists(downloadPath)) {
                try {
                    Files.createDirectory(downloadPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }
    }

    @Override
    public void settingsChanged(GlobalSettings settings) {
    }

    @Override
    public void settingsSaved(GlobalSettings settings) {
        restartTimers(settings);
    }

    private void createInitialSettings(String username, String password, String serverBaseUrl, File importFolder, File intrayFolder) {
        GlobalSettings settings = GlobalSettings.getInstance();

        if (StringUtils.isEmpty(settings.getUsername()) && username != null) {
            settings.setUsername(username);
        }
        if (StringUtils.isEmpty(settings.getPassword()) && password != null) {
            settings.setPassword(password);
        }
        if (StringUtils.isEmpty(settings.getServerBaseUrl()) && serverBaseUrl != null) {
            settings.setServerBaseUrl(serverBaseUrl);
        }

        if (importFolder != null) {
            FolderSettings folderSettings = settings.getFolderSettings(importFolder);
            if (folderSettings == null) {
                folderSettings = new FolderSettings();
                folderSettings.setFolder(importFolder);
                folderSettings.setTargetContainer(IMPORT_FOLDER_ID);
                settings.addFolderSettings(folderSettings);
            }
        }

        if (intrayFolder != null) {
            FolderSettings folderSettings = settings.getFolderSettings(intrayFolder);
            if (folderSettings == null) {
                folderSettings = new FolderSettings();
                folderSettings.setFolder(intrayFolder);
                settings.addFolderSettings(folderSettings);
            }
        }
    }

    public static void main(String[] args) {

        createUUID();
        LocalWebServer localWebServer = new LocalWebServer();
        localWebServer.start();

        boolean configure = false;
        boolean start = true;

        String username = null;
        String password = null;
        String url = null;

        File importFolder = null;
        File intrayFolder = null;

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("/C")) {
                configure = true;
            } else if (arg.equals("stop")) {
                start = false;
            } else if (i < args.length - 1) {
                if (arg.equalsIgnoreCase("/u")) {
                    username = args[++i];
                } else if (arg.equalsIgnoreCase("/p")) {
                    password = args[++i];
                } else if (arg.equalsIgnoreCase("/o")) {
                    url = args[++i];
                } else if (arg.equalsIgnoreCase("/i")) {
                    importFolder = new File(args[++i]);
                } else if (arg.equalsIgnoreCase("/t")) {
                    intrayFolder = new File(args[++i]);
                }
            }
        }
        getInstance().createInitialSettings(username, password, url, importFolder, intrayFolder);

        if (start) {
            getInstance().start(configure);
        } else {
            getInstance().stop();
        }
    }

    @Override
    public void uploadFailed(FolderMonitor fm, File file, Exception e) {
        uploadEventLogger.logFailure(fm.getFolderSettings().getUsername(), file, e);
        if (e instanceof UploadServiceException || e instanceof OurdriveException) {
            showWarningDialog("Upload failed for folder '" + fm.getFolderSettings().getFolder() + "':\n" + e.getMessage());
        }
    }

    @Override
    public void uploadSucceeded(FolderMonitor fm, File file, String docId) {
        uploadEventLogger.logSuccess(fm.getFolderSettings().getUsername(), file, docId);
    }

    //Private methods

    public static String getUserDataDirectory() {
        return System.getProperty("user.home") + File.separator + "ourdrive" + File.separator;
    }

    private void startTimer(FolderSettings folderSettings) {
        if(isSettingsValid(folderSettings)) {
            final File folder = folderSettings.getFolder();
            Timer t = timers.get(folder);
            if (t == null && folder != null) {
                t = new Timer();
                folderSettings.clearQuietPeriods();
                FolderMonitor folderMonitor = new FolderMonitor(folderSettings);
                TimerTask timerTask = new FolderMonitorTask(folderMonitor,
                        new UploadFileHandler(),
                        new UploadDirectoryHandler(),
                        Collections.singletonList((FolderMonitorListener) this));

                t.schedule(timerTask, TIMER_PERIOD, INTERVAL_SECONDS);
                timers.put(folder, t);
                logger.info("Started monitoring folder: " + folder.getAbsolutePath());
            }
        } else {
            logger.info("Wrong configuration for folder: " + folderSettings.getFolder().getAbsolutePath());
        }
    }

    private void stopTimer(File folder) {
        Timer t = timers.remove(folder);
        if (t != null) {
            t.cancel();
        }
        logger.info("Stopped monitoring folder: " + folder.getAbsolutePath());
    }

    private void stopAllTimers() {
        Collection<File> monitoredFolders = new ArrayList<File>(timers.keySet());
        for (File monitoredFolder : monitoredFolders) {
            stopTimer(monitoredFolder);
        }
    }


    /**
     * Validates folder settings before upload
     *
     * @param settings folder settings
     * @return validation result
     */
    private boolean isSettingsValid(FolderSettings settings) {
        if(StringUtils.isBlank(settings.getServerBaseUrl())) {
            showWarningDialog(String.format("Folder '%s' configuration error.\n\"%s\" cannot be empty.", settings.getFolder(), "Server base url"));
            return false;
        }

        if(StringUtils.isBlank(settings.getUsername())) {
            showWarningDialog(String.format("Folder '%s' configuration error.\n\"%s\" cannot be empty.", settings.getFolder(), "Username"));
            return false;
        }

        if(StringUtils.isBlank(settings.getPassword())) {
            showWarningDialog(String.format("Folder '%s' configuration error.\n\"%s\" cannot be empty.", settings.getFolder(), "Password"));
            return false;
        }
        return true;
    }


    private void showWarningDialog(String message) {
        if (!dialogDismissedForever) {
            Icon icon = OurDriveGUI.getLargeIcon();

            int choice = JOptionPane.showOptionDialog(null, message, "Cygnet Ourdrive",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, icon, new Object[]{"Display settings", "Dismiss and don't show again", "Dismiss"}, null);

            switch (choice) {
                case 0:
                    GeneralSettingsDialog.showDialog();
                    break;
                case 1:
                    dialogDismissedForever = true;
                    break;
                case 2:
                    break;
                default:
            }

        }
    }

    /**
     * @return
     */
    public static String getDownloadFolderName() {
        return download_folder_name;
    }
}
