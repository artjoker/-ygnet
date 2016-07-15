package com.cygnet.ourdrive.gui;

import com.cygnet.ourdrive.settings.AbstractSettings;
import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.settings.UploadMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA on 04/11/12.
 *
 * @author werneraltewischer
 */
public abstract class AbstractSettingsDialog<T extends AbstractSettings> extends JDialog implements AbstractSettings.SettingsListener<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final T settings;

    public AbstractSettingsDialog(T settings) {
        this.settings = settings;
        this.settings.addListener(this);
        setModified(false);
    }

    protected void onOK() {
        applySettings();
        dispose();
    }

    protected void onClose() {
        dispose();
    }

    protected void onApply() {
        applySettings();
    }

    @Override
    public void dispose() {
        super.dispose();
        this.settings.removeListener(this);
    }

    protected abstract void applySettings();

    protected abstract void populateForm();

    @Override
    public void settingsChanged(T settings) {
        setModified(true);
    }

    @Override
    public void settingsSaved(T settings) {
        setModified(false);
    }

    protected void setModified(boolean modified) {
    }

    protected class ModifiedActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            setModified(true);
        }
    }

    protected class ModifiedDocumentListener implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            setModified(true);
        }

        public void removeUpdate(DocumentEvent e) {
            setModified(true);
        }

        public void insertUpdate(DocumentEvent e) {
            setModified(true);
        }
    }
}
