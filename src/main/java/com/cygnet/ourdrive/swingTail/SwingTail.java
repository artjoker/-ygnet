package com.cygnet.ourdrive.swingTail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

public class SwingTail extends JDialog implements WindowListener, MouseListener, KeyListener {

    private static final long serialVersionUID = 1712459908855365803L;

    private static final Logger logger = LoggerFactory.getLogger(SwingTail.class);

    private static SwingTail instance = null;

    private final Container cont = getContentPane();
    private final JTabbedPane pane = new JTabbedPane();
    private final JMenuBar menuBar = new JMenuBar();

    private final static SettingHandler settingHandler = new SettingHandler();

    public SwingTail() {
        setTitle("Ourdrive logs");
        this.addKeyListener(this);
        pane.addKeyListener(this);
        cont.addKeyListener(this);

        setJMenuBar(menuBar);
        cont.add(pane);

        setSize(1000, 800);
        addWindowListener(this);
        setVisible(true);
    }

    public static void show(String[] args) {

        logger.info("Showing swingtail");

        if (instance == null) {
            instance = new SwingTail();

            for (String filePath : args) {
                instance.openFile(new File(filePath));
            }

            instance.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            instance.setVisible(true);

        } else {
            instance.setVisible(true);
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    instance.toFront();
                    instance.repaint();
                }
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        instance = null;
    }

    public void openFile(File file) {
        try {
            new SwingTailTab(pane, file, settingHandler, this);
        } catch (FileNotFoundException e1) {
            logger.warn("Could not open file", e1);
        }
    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {

    }

    public void windowClosing(WindowEvent arg0) {
    }

    public void windowDeactivated(WindowEvent arg0) {
    }

    public void windowDeiconified(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
    }

    public void windowOpened(WindowEvent arg0) {
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {

            int currentTab = pane.getSelectedIndex();

            if (currentTab != -1) {

                SwingTailTab tab = ((SwingTailTab) pane.getTabComponentAt(currentTab));

                tab.getHtml().append("<BR>");
                tab.getHtml().setCaretPosition(((HTMLDocument) tab.getHtml().getDocument()).getLength());
            }
        }
    }

    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }
}