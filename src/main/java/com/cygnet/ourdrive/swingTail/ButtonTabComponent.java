package com.cygnet.ourdrive.swingTail;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

public class ButtonTabComponent extends JPanel implements ActionListener{
	private static final long serialVersionUID = 8661114071455098418L;
	protected JTabbedPane pane;
	protected JButton knapp1, knapp2;
	protected ImageIcon play, pause;
	protected JLabel label;
	protected File file;
	protected FileMonitor monitorFile;

	public ButtonTabComponent(final JTabbedPane inPane, final File inFile){

		play = new ImageIcon(this.getClass().getResource("/swingtail/play.gif"));
		pause = new ImageIcon(this.getClass().getResource("/swingtail/pause.gif"));

		file = inFile;
		pane = inPane;
		label = new JLabel(){
			private static final long serialVersionUID = -4129777755655823923L;

			@Override
			public String getText() {
				int i = pane.indexOfTabComponent(ButtonTabComponent.this);
				if (i != -1) {
					return pane.getTitleAt(i);
				}
				return null;
			}
		};
		new JPanel();

		knapp1 = new JButton();
		knapp1.setPreferredSize(new Dimension(17, 17));
		knapp1.setToolTipText("Play/Pause");
		knapp1.setUI(new BasicButtonUI());
		knapp1.setContentAreaFilled(false);
		knapp1.setFocusable(false);
		knapp1.setBorder(BorderFactory.createEtchedBorder());
		knapp1.setBorderPainted(false);
		knapp1.setRolloverEnabled(false);
		knapp1.addActionListener(this);
		knapp1.setIcon(play);

		knapp2 = new TabButton();
		setOpaque(false);
		add(knapp1);
		add(label);
		add(knapp2);
	}

	public void actionPerformed(ActionEvent e) {
		if (knapp1.getIcon().equals(play)){
			knapp1.setIcon(pause);
			monitorFile.setPause(true);

		}else{
			knapp1.setIcon(play);
			monitorFile.setPause(false);
		}
	}

	private class TabButton extends JButton implements ActionListener {
		private static final long serialVersionUID = 173106415151171898L;

		public TabButton() {
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("close this tab");
			setUI(new BasicButtonUI());
			setContentAreaFilled(false);
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			setRolloverEnabled(true);
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			int i = pane.indexOfTabComponent(ButtonTabComponent.this);
			if (i != -1) {
				pane.remove(i);
				monitorFile.closeThread();
			}
		}

		//we don't want to update UI for this button
		@Override
		public void updateUI() {
		}

		//paint the cross
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			//shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.MAGENTA);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}
	}
}
