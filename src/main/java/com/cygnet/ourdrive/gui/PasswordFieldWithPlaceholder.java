package com.cygnet.ourdrive.gui;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA on 10/11/12.
 *
 * @author werneraltewischer
 */
public class PasswordFieldWithPlaceholder extends JPasswordField {

    private final TextFieldPlaceholderPainter painter = new TextFieldPlaceholderPainter();

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        painter.paintTextField(this, g);
    }

}
