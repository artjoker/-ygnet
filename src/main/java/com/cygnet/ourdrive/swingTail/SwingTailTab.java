package com.cygnet.ourdrive.swingTail;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class SwingTailTab extends ButtonTabComponent{
	private final GridLayout layout = new GridLayout();
	private final JPanel panel;
	private final HTMLInJEditorPane html;
	private final JScrollPane scrollPane;
	private final SettingHandler settingHandler;

	private static final long serialVersionUID = 5676550860899134502L;

	public SwingTailTab(JTabbedPane inPane,File inFile, SettingHandler settingHandler, KeyListener keyListener) throws FileNotFoundException{
		super(inPane, inFile);
		this.settingHandler = settingHandler;
		panel = new JPanel(layout);
		panel.setName(file.getName());
		html = new HTMLInJEditorPane(settingHandler, this);

		readSettings();

		scrollPane = new JScrollPane(html);

		panel.add(scrollPane);
		pane.addTab(file.getName(), panel);
		pane.setTabComponentAt(pane.getTabCount()-1, this);
		pane.setSelectedIndex(pane.getTabCount()-1);
		monitorFile = new FileMonitor(html,file, settingHandler, scrollPane.getVerticalScrollBar());

		this.addKeyListener(keyListener);
		this.panel.addKeyListener(keyListener);
		this.html.addKeyListener(keyListener);
	}
	public void pauseFileSearch(){
		monitorFile.setPause(true);
		knapp1.setIcon(pause);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
	}

	public File getFile(){
		return file;
	}

	public String getText(){

		return html.getText();
	}

	public void readSettings(){
		//		text.setLineWrap(settingHandler.isLineWrap());
		html.setFont(settingHandler.getSettingsFont());
		html.setMaxrows(settingHandler.getRowsToShow());
		html.setSettingHandler(settingHandler);
	}
	public HTMLInJEditorPane getHtml() {
		return html;
	}
}
