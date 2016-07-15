package com.cygnet.ourdrive;

import com.cygnet.ourdrive.gui.GeneralSettingsDialog;
import com.cygnet.ourdrive.gui.OurDriveGUI;
import com.cygnet.ourdrive.gui.SystrayItem;
import com.cygnet.ourdrive.gui.SytrayInitializationException;
import com.cygnet.ourdrive.monitoring.FolderMonitor;
import com.cygnet.ourdrive.monitoring.FolderMonitorListener;
import com.cygnet.ourdrive.monitoring.FolderMonitorTask;
import com.cygnet.ourdrive.monitoring.exceptions.FileVisitException;
import com.cygnet.ourdrive.monitoring.exceptions.OurdriveException;
import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.upload.UploadDirectoryHandler;
import com.cygnet.ourdrive.upload.UploadFileHandler;
import com.cygnet.ourdrive.upload.UploadServiceException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Timer;

/**
 * Application entry point!
 */
public final class OurDriveService implements GlobalSettings.SettingsListener<GlobalSettings>, FolderMonitorListener {

    public static final String VERSION = "2.0.2";

    private static final long TIMER_PERIOD = 1000L;

    private static final long INTERVAL_SECONDS = 3 * 1000L;

    private static OurDriveService instance;
    private static final Logger logger = LoggerFactory.getLogger(OurDriveService.class);
    private static final String IMPORT_FOLDER_ID = "83541";

    private volatile boolean running = false;

    private final UploadEventLogger uploadEventLogger = new UploadEventLogger();

    private Map<File, Timer> timers = new LinkedHashMap<File, Timer>();

    private boolean dialogDismissedForever = false;

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
        running = false;
        synchronized (this) {
            notify();
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
        } catch (SytrayInitializationException e) {
            logger.warn("Could not initialize systray item", e);
        }
        logger.info("OurDrive service started");
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

    private static String getUserDataDirectory() {
        return System.getProperty("user.home") + File.separator + ".ourdrive" + File.separator;
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
}
