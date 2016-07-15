package com.cygnet.ourdrive.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA on 12/11/12.
 *
 * @author werneraltewischer
 */
public class OurDriveGUI {

    private static final Logger logger = LoggerFactory.getLogger(OurDriveGUI.class);

    public static Icon getLargeIcon() {
        Icon icon = null;
        try {
            Image image = ImageIO.read(SystrayItem.class.getResource("/images/ourdrive-large.png"));
            icon = new ImageIcon(image);
        } catch (IOException e1) {
            logger.warn("Could not load icon image: " + e1);
        }
        return icon;
    }

    public static Icon getXLIcon() {
        Icon icon = null;
        try {
            Image image = ImageIO.read(SystrayItem.class.getResource("/images/ourdrive-xl.png"));
            icon = new ImageIcon(image);
        } catch (IOException e1) {
            logger.warn("Could not load icon image: " + e1);
        }
        return icon;
    }

    public static Icon getIcon() {
        Icon icon = null;
        try {
            Image image = ImageIO.read(SystrayItem.class.getResource("/images/ourdrive-xl.png"));
            icon = new ImageIcon(image);
        } catch (IOException e1) {
            logger.warn("Could not load icon image: " + e1);
        }
        return icon;
    }

}
