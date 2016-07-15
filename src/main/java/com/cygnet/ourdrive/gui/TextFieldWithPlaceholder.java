package com.cygnet.ourdrive.gui;

import javax.swing.*;

public class TextFieldWithPlaceholder extends JTextField {

    private final TextFieldPlaceholderPainter painter = new TextFieldPlaceholderPainter();

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        painter.paintTextField(this, g);
    }

}