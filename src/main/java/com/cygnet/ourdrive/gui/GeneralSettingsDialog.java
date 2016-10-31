package com.cygnet.ourdrive.gui;

import com.cygnet.ourdrive.OurDriveService;
import com.cygnet.ourdrive.settings.AbstractSettings;
import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.settings.GlobalSettings;
import com.cygnet.ourdrive.settings.UploadMethod;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
    private JButton buttonCopy;
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
    private JTextField ourdriveidField;
    private JTextField downloadPathField;

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

        buttonCopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCopy();
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
//        downloadPathField.getDocument().addDocumentListener(new ModifiedDocumentListener());
        apiKeyField.getDocument().addDocumentListener(new ModifiedDocumentListener());
        ourdriveidField.getDocument().addDocumentListener(new ModifiedDocumentListener());
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

    private void onCopy() {
        String ourdriveId = settings.getOurdriveId();
        StringSelection selection = new StringSelection(ourdriveId);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
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
        if (settings.getFolderSettings(dir) != null) { //Folder already monitored
            return false;
        }

        String path = dir.getAbsolutePath();
        for (FolderSettings fs : settings.getAllFolderSettings()) {
            //Check if folder is subfolder or parent folder of already monitored folder
            String folderPath = fs.getFolder().getAbsolutePath();
            if (folderPath.startsWith(path) || path.startsWith(folderPath)) {
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
        ourdriveidField.setText(settings.getOurdriveId());
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
//        downloadPathField.setText(settings.getDownloadPath());

        for (FolderSettings fs : settings.getAllFolderSettings()) {
            folderListModel.addElement(fs);
        }
    }

    private void applySettings() {
        settings.setApiKey(apiKeyField.getText());
        settings.setOurdriveId(ourdriveidField.getText());
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
//        settings.setUpdownloadPath(downloadPathField.getText());

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
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    dialog.toFront();
                    dialog.repaint();
                }
            });
        }
        return dialog;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonClose = new JButton();
        buttonClose.setText("Close");
        panel2.add(buttonClose, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonApply = new JButton();
        buttonApply.setText("Apply");
        panel2.add(buttonApply, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exitButton = new JButton();
        exitButton.setText("Exit");
        panel1.add(exitButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(10, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("APIKey");
        panel3.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Username");
        panel3.add(label2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Password");
        panel3.add(label3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Target container");
        panel3.add(label4, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordField = new JPasswordField();
        panel3.add(passwordField, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        usernameField = new JTextField();
        panel3.add(usernameField, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        apiKeyField = new JPasswordField();
        panel3.add(apiKeyField, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Server URL");
        panel3.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Upload URI");
        panel3.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverUrlField = new JTextField();
        panel3.add(serverUrlField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        uploadUriField = new JTextField();
        uploadUriField.setEditable(false);
        uploadUriField.setEnabled(false);
        panel3.add(uploadUriField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Upload method");
        panel3.add(label7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uploadMethodBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("POST");
        uploadMethodBox.setModel(defaultComboBoxModel1);
        panel3.add(uploadMethodBox, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        targetContainerField = new JTextField();
        panel3.add(targetContainerField, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Folders");
        panel3.add(label8, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(9, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addFolderButton = new JButton();
        addFolderButton.setText("Add folder");
        panel4.add(addFolderButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        folderSettingsButton = new JButton();
        folderSettingsButton.setText("Folder settings");
        panel4.add(folderSettingsButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeFolderButton = new JButton();
        removeFolderButton.setText("Remove folder");
        panel4.add(removeFolderButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        folderList = new JList();
        folderList.setSelectionMode(0);
        scrollPane1.setViewportView(folderList);
        final JLabel label9 = new JLabel();
        label9.setText("Ourdrive ID");
        panel3.add(label9, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ourdriveidField = new JTextField();
        ourdriveidField.setEditable(false);
        ourdriveidField.setEnabled(false);
        panel3.add(ourdriveidField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(300, -1), null, 0, false));
        buttonCopy = new JButton();
        buttonCopy.setText("Copy");
        panel3.add(buttonCopy, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
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
