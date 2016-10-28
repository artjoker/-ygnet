package com.cygnet.ourdrive.gui;

import com.cygnet.ourdrive.OurDriveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cygnet.ourdrive.swingTail.SwingTail;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA on 14/10/12.
 *
 * @author werneraltewischer
 */
public class SystrayItem {

    private static final Logger logger = LoggerFactory.getLogger(SystrayItem.class);

    private static SystrayItem instance;

    private final TrayIcon trayIcon;
    private final String version;

    private SystrayItem(TrayIcon trayIcon, String version) {
        this.trayIcon = trayIcon;
        this.version = version;
    }

    public static synchronized SystrayItem register(final String version) throws SytrayInitializationException {
        if (instance == null) {
            //Check the SystemTray is supported
            if (!SystemTray.isSupported()) {
                throw new SytrayInitializationException("System tray is not supported");
            }
            final PopupMenu popup = new PopupMenu();
            final Image image;
            try {
                image = ImageIO.read(SystrayItem.class.getResource("/images/ourdrive.png"));
            } catch (IOException e) {
                throw new SytrayInitializationException("Could not load tray icon image", e);
            }
            final TrayIcon trayIcon = new TrayIcon(image);
            trayIcon.setImageAutoSize(true);
            final SystemTray tray = SystemTray.getSystemTray();

            // Create a pop-up menu components
            MenuItem aboutItem = new MenuItem("About");
            MenuItem configureItem = new MenuItem("Settings");
            MenuItem logItem = new MenuItem("Show log");
            MenuItem exitItem = new MenuItem("Exit");

            aboutItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Icon icon = OurDriveGUI.getXLIcon();
                    String message = "Cygnet Ourdrive\nversion " + version + "\nwww.cygnetcloud.com";
                    JOptionPane.showConfirmDialog(null, message, "Cygnet Ourdrive",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            icon);
                }
            });

            configureItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GeneralSettingsDialog.showDialog();
                }
            });

            logItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    logger.info("Show log");

                    String logFile1 = "log" + File.separator + "ourdrive.log";
                    String logFile2 = "log" + File.separator + "ourdrive_upload.log";

                    SwingTail.show(new String[]{logFile2, logFile1});
                }
            });

            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    OurDriveService.getInstance().stop();
                }
            });

            //Add components to pop-up menu
            popup.add(aboutItem);
            popup.addSeparator();
            popup.add(configureItem);
            popup.add(logItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);
            trayIcon.setToolTip("Cygnet Ourdrive");
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        GeneralSettingsDialog.showDialog();
                    }
                }
            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                throw new SytrayInitializationException("Tray icon could not be added", e);
            }

            instance = new SystrayItem(trayIcon, version);
        }
        return instance;
    }

}
