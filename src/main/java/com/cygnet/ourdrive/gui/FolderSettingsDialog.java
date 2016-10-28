package com.cygnet.ourdrive.gui;

import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.settings.UploadMethod;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class FolderSettingsDialog extends AbstractSettingsDialog<FolderSettings> {

    private static FolderSettingsDialog dialog;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonClose;
    private JButton buttonApply;
    private JComboBox uploadMethodBox;
    private JButton serverUrlClearButton;
    private JButton usernameClearButton;
    private JButton uploadUriClearButton;
    private JButton apiKeyClearButton;
    private JButton passwordClearButton;
    private JButton targetContainerClearButton;
    private TextFieldWithPlaceholder folderTextField;
    private TextFieldWithPlaceholder serverUrlTextField;
    private TextFieldWithPlaceholder uploadUriTextField;
    private PasswordFieldWithPlaceholder apiKeyTextField;
    private TextFieldWithPlaceholder usernameTextField;
    private PasswordFieldWithPlaceholder passwordTextField;
    private TextFieldWithPlaceholder targetContainerTextField;
    private JCheckBox moveFilesToTrashCheckBox;

    public FolderSettingsDialog(FolderSettings folderSettings) {
        super(folderSettings);

        setTitle("Settings for folder " + folderSettings.getFolder().getPath());
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        if (uploadMethodBox != null) {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (UploadMethod uploadMethod : UploadMethod.values()) {
                model.addElement(uploadMethod);
            }
            uploadMethodBox.setModel(model);
        }


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

        if (buttonApply != null) {
            buttonApply.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onApply();
                }
            });
        }

        if (serverUrlTextField != null)
            serverUrlTextField.getDocument().addDocumentListener(new ModifiedDocumentListener());

        if (uploadUriTextField != null)
            uploadUriTextField.getDocument().addDocumentListener(new ModifiedDocumentListener());

        if (apiKeyTextField != null)
            apiKeyTextField.getDocument().addDocumentListener(new ModifiedDocumentListener());

        if (usernameTextField != null)
            usernameTextField.getDocument().addDocumentListener(new ModifiedDocumentListener());

        if (passwordTextField != null)
            passwordTextField.getDocument().addDocumentListener(new ModifiedDocumentListener());

        if (targetContainerTextField != null)
            targetContainerTextField.getDocument().addDocumentListener(new ModifiedDocumentListener());

        if (uploadMethodBox != null)
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
        });

        // call onClose() on ESCAPE
        contentPane.registerKeyboardAction(new
                                                   ActionListener() {
                                                       public void actionPerformed(ActionEvent e) {
                                                           onClose();
                                                       }
                                                   }

                , KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        populateForm();

        serverUrlClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverUrlTextField.setText("");
            }
        });

        if (uploadUriClearButton != null) {
            uploadUriClearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    uploadUriTextField.setText("");
                }
            });
        }

        apiKeyClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apiKeyTextField.setText("");
            }
        });

        usernameClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usernameTextField.setText("");
            }
        });

        passwordClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordTextField.setText("");
            }
        });

        targetContainerClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                targetContainerTextField.setText("");
            }
        });

    }

    public static FolderSettingsDialog showDialog(FolderSettings folderSettings) {
        if (dialog == null) {
            dialog = new FolderSettingsDialog(folderSettings);
            dialog.setMinimumSize(new Dimension(500, 300));
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

    @Override
    protected void applySettings() {
        if (folderTextField != null)
            settings.setFolder(new File(folderTextField.getText()));

        if (apiKeyTextField != null)
            settings.setApiKey(apiKeyTextField.getText());

        if (usernameTextField != null)
            settings.setUsername(usernameTextField.getText());

        if (passwordTextField != null)
            settings.setPassword(passwordTextField.getText());

        if (moveFilesToTrashCheckBox != null)
            settings.setMoveFilesToTrash(moveFilesToTrashCheckBox.isSelected());

        if (serverUrlTextField != null)
            settings.setServerBaseUrl(serverUrlTextField.getText());

        if (targetContainerTextField != null)
            settings.setTargetContainer(targetContainerTextField.getText());

        if (uploadMethodBox != null) {
            UploadMethod uploadMethod = UploadMethod.values()[uploadMethodBox.getSelectedIndex()];
            settings.setUploadMethod(uploadMethod);
        }

        if (uploadUriTextField != null)
            settings.setUploadUri(uploadUriTextField.getText());
    }

    @Override
    protected void populateForm() {
        settings.setResolvePropertiesFromParent(false);
        if (folderTextField != null)
            folderTextField.setText(settings.getFolder().getAbsolutePath());

        if (apiKeyTextField != null)
            apiKeyTextField.setText(settings.getApiKey());

        if (usernameTextField != null)
            usernameTextField.setText(settings.getUsername());

        if (passwordTextField != null)
            passwordTextField.setText(settings.getPassword());

        if (moveFilesToTrashCheckBox != null) {
            Boolean isMoveFilesToTrash = settings.getMoveFilesToTrash();
            moveFilesToTrashCheckBox.setSelected(isMoveFilesToTrash == null ? false : isMoveFilesToTrash);
        }

        if (serverUrlTextField != null)
            serverUrlTextField.setText(settings.getServerBaseUrl());

        if (targetContainerTextField != null)
            targetContainerTextField.setText(settings.getTargetContainer());

        if (uploadMethodBox != null)
            uploadMethodBox.setSelectedIndex(settings.getUploadMethod() == null ? 0 : settings.getUploadMethod().ordinal());

        if (uploadUriTextField != null)
            uploadUriTextField.setText(settings.getUploadUri());
        settings.setResolvePropertiesFromParent(true);
    }

    @Override
    public void dispose() {
        super.dispose();
        dialog = null;
    }

    protected void setModified(boolean modified) {
        if (buttonApply != null) {
            buttonApply.setEnabled(modified);
        }
        if (buttonOK != null) {
            buttonOK.setEnabled(modified);
        }
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
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonClose = new JButton();
        buttonClose.setText("Close");
        panel2.add(buttonClose, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        usernameTextField = new TextFieldWithPlaceholder();
        panel3.add(usernameTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Username");
        panel3.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Password");
        panel3.add(label2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Server URL");
        panel3.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverUrlTextField = new TextFieldWithPlaceholder();
        serverUrlTextField.setText("");
        panel3.add(serverUrlTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Target container");
        panel3.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        targetContainerTextField = new TextFieldWithPlaceholder();
        panel3.add(targetContainerTextField, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        passwordTextField = new PasswordFieldWithPlaceholder();
        panel3.add(passwordTextField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        serverUrlClearButton = new JButton();
        serverUrlClearButton.setText("Clear");
        panel3.add(serverUrlClearButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        usernameClearButton = new JButton();
        usernameClearButton.setText("Clear");
        panel3.add(usernameClearButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordClearButton = new JButton();
        passwordClearButton.setText("Clear");
        panel3.add(passwordClearButton, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        targetContainerClearButton = new JButton();
        targetContainerClearButton.setText("Clear");
        panel3.add(targetContainerClearButton, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Upload method");
        panel3.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uploadMethodBox = new JComboBox();
        panel3.add(uploadMethodBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("API key");
        panel3.add(label6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        apiKeyTextField = new PasswordFieldWithPlaceholder();
        panel3.add(apiKeyTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        apiKeyClearButton = new JButton();
        apiKeyClearButton.setText("Clear");
        panel3.add(apiKeyClearButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
