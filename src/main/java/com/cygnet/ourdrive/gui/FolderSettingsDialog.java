package com.cygnet.ourdrive.gui;

import com.cygnet.ourdrive.settings.FolderSettings;
import com.cygnet.ourdrive.settings.UploadMethod;

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
}
