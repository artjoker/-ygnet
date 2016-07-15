package com.cygnet.ourdrive.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA on 10/11/12.
 *
 * @author werneraltewischer
 */
public class TextFieldPlaceholderPainter {

    private final String placeHolder = "<Inherited>";

    public void paintTextField(JTextField textField, java.awt.Graphics g) {
        if (textField.getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setBackground(Color.gray);
            g2.setFont(textField.getFont().deriveFont(Font.ITALIC));
            g2.setColor(Color.gray);
            g2.drawString(this.placeHolder, 4, 14); //figure out x, y from font's FontMetrics and size of component.
            g2.dispose();
        }
    }
}
