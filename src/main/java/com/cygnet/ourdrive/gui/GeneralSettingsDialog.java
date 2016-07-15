package com.cygnet.ourdrive.gui;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.AbstractSettings;
import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.settings.UploadMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class GeneralSettingsDialog extends JDialog implements AbstractSettings.SettingsListener<GlobalSettings> {

    private static final Logger logger = LoggerFactory.getLogger(GeneralSettingsDialog.class);

    private static GeneralSettingsDialog dialog;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonClose;
    private JButton buttonApply;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JPasswordField apiKeyField;
    private JTextField serverUrlField;
    private JTextField uploadUriField;
    private JComboBox uploadMethodBox;
    private JTextField targetContainerField;
    private JCheckBox moveFilesToTrashCheckBox;
    private JButton addFolderButton;
    private JButton folderSettingsButton;
    private JButton removeFolderButton;
    private JList folderList;
    private JButton exitButton;

    private DefaultListModel folderListModel;

    private final GlobalSettings settings;

    public GeneralSettingsDialog(GlobalSettings settings) {
        this.settings = settings;
        setTitle("Cygnet OurDrive Settings");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (UploadMethod uploadMethod : UploadMethod.values()) {
            model.addElement(uploadMethod);
        }
        uploadMethodBox.setModel(model);

        folderListModel = new DefaultListModel();
        folderList.setModel(folderListModel);

        folderList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateListButtons();
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });

        buttonApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onApply();
            }
        });

        addFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddFolder();
            }
        });

        removeFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveFolder();
            }
        });

        folderSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onFolderSettings();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExit();
            }
        });

        serverUrlField.getDocument().addDocumentListener(new ModifiedDocumentListener());
        uploadUriField.getDocument().addDocumentListener(new ModifiedDocumentListener());
        apiKeyField.getDocument().addDocumentListener(new ModifiedDocumentListener());
        usernameField.getDocument().addDocumentListener(new ModifiedDocumentListener());
        passwordField.getDocument().addDocumentListener(new ModifiedDocumentListener());
        targetContainerField.getDocument().addDocumentListener(new ModifiedDocumentListener());
        uploadMethodBox.addActionListener(new ModifiedActionListener());
        if (moveFilesToTrashCheckBox != null) {
            moveFilesToTrashCheckBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    setModified(true);
                }
            });
        }

        // call onClose() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        }

        );

        // call onClose() on ESCAPE
        contentPane.registerKeyboardAction(new
                                           ActionListener() {
                                               public void actionPerformed(ActionEvent e) {
                                                   onClose();
                                               }
                                           }

                , KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        populateForm();

        this.settings.addListener(this);
        setModified(false);
        updateListButtons();
    }

    private void updateListButtons() {
        Object[] selectedValues = folderList.getSelectedValues();
        final boolean selected = selectedValues != null && selectedValues.length > 0;
        removeFolderButton.setEnabled(selected);
        folderSettingsButton.setEnabled(selected);
    }

    private void onOK() {
        applySettings();
        dispose();
    }

    private void onExit() {
        OurDriveService.getInstance().stop();
        dispose();
    }

    private void onClose() {
        dispose();
    }

    @Override
    public void dispose() {
        super.dispose();
        this.settings.removeListener(this);
        dialog = null;
    }

    private void setModified(boolean modified) {
        buttonApply.setEnabled(modified);
        buttonOK.setEnabled(modified);
    }

    private void onApply() {
        applySettings();
    }

    @Override
    public void settingsChanged(GlobalSettings settings) {
        setModified(true);
    }

    @Override
    public void settingsSaved(GlobalSettings settings) {
        setModified(false);
    }

    private void onAddFolder() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //In response to a button click:
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            if (canAddFolder(file)) {
                FolderSettings folderSettings = new FolderSettings();
                folderSettings.setFolder(file);
                settings.addFolderSettings(folderSettings);
                folderListModel.addElement(folderSettings);
                redrawFolders();
            } else {
                JOptionPane.showMessageDialog(this, "This folder is already being monitored, please select a different one",
                        "Cygnet Ourdrive", JOptionPane.WARNING_MESSAGE, OurDriveGUI.getLargeIcon());
            }
        }
        updateListButtons();
    }

    /**
     * Check if user can add folder
     *
     * @param dir folder to add
     * @return check flag
     */
    private boolean canAddFolder(File dir) {
        if(settings.getFolderSettings(dir) != null) { //Folder already monitored
            return false;
        }

        String path = dir.getAbsolutePath();
        for(FolderSettings fs: settings.getAllFolderSettings()) {
            //Check if folder is subfolder or parent folder of already monitored folder
            String folderPath = fs.getFolder().getAbsolutePath();
            if(folderPath.startsWith(path) || path.startsWith(folderPath)) {
                return false;
            }
        }
        return true;
    }

    private void onRemoveFolder() {
        int selectedIndex = folderList.getSelectedIndex();
        for (Object fs : folderList.getSelectedValues()) {
            folderListModel.removeElement(fs);
            settings.removeFolderSettings((FolderSettings) fs);
            redrawFolders();
        }
        selectedIndex = Math.min(selectedIndex, folderList.getModel().getSize() - 1);
        if (selectedIndex >= 0) {
            folderList.setSelectedIndex(selectedIndex);
        }
        updateListButtons();
    }

    private void onFolderSettings() {
        Object selectedValue = folderList.getSelectedValue();
        if (selectedValue != null && selectedValue instanceof FolderSettings) {
            FolderSettings folderSettings = (FolderSettings) selectedValue;
            FolderSettingsDialog.showDialog(folderSettings);
        }
    }

    private void redrawFolders() {

    }

    private void populateForm() {
        apiKeyField.setText(settings.getApiKey());
        usernameField.setText(settings.getUsername());
        passwordField.setText(settings.getPassword());
        Boolean isMoveFilesToTrash = settings.getMoveFilesToTrash();
        if (moveFilesToTrashCheckBox != null) {
            moveFilesToTrashCheckBox.setSelected(isMoveFilesToTrash == null ? false : isMoveFilesToTrash);
        }
        serverUrlField.setText(settings.getServerBaseUrl());
        targetContainerField.setText(settings.getTargetContainer());
        uploadMethodBox.setSelectedIndex(settings.getUploadMethod().ordinal());
        uploadUriField.setText(settings.getUploadUri());

        for (FolderSettings fs : settings.getAllFolderSettings()) {
            folderListModel.addElement(fs);
        }
    }

    private void applySettings() {
        settings.setApiKey(apiKeyField.getText());
        settings.setUsername(usernameField.getText());
        settings.setPassword(passwordField.getText());
        if (moveFilesToTrashCheckBox != null) {
            settings.setMoveFilesToTrash(moveFilesToTrashCheckBox.isSelected());
        }
        settings.setServerBaseUrl(serverUrlField.getText());
        settings.setTargetContainer(targetContainerField.getText());

        UploadMethod uploadMethod = UploadMethod.values()[uploadMethodBox.getSelectedIndex()];
        settings.setUploadMethod(uploadMethod);
        settings.setUploadUri(uploadUriField.getText());

        try {
            this.settings.save();
        } catch (IOException e) {
            logger.warn("Could not save settings", e);
        }
    }

    public static GeneralSettingsDialog showDialog() {
        GlobalSettings settings = GlobalSettings.getInstance();
        if (dialog == null) {
            dialog = new GeneralSettingsDialog(settings);
            dialog.pack();
            dialog.setModal(true);
            dialog.setVisible(true);
        } else {
            dialog.setVisible(true);
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    dialog.toFront();
                    dialog.repaint();
                }
            });
        }
        return dialog;
    }

    private class ModifiedDocumentListener implements DocumentListener {
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

    private class ModifiedActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            setModified(true);
        }
    }
}
