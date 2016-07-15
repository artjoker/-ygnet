package com.cygnet.ourdrive.swingTail;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class HTMLInJEditorPane extends JEditorPane{
	private static final long serialVersionUID = 1L;
	private SettingHandler settingHandler;
	private SwingTailTab owner;
	private int maxrows = 5;
	private Font font;
	private HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
	private HTMLDocument doc = new HTMLDocument();

	private long lastPainted;

	public HTMLInJEditorPane(SettingHandler settingHandler, SwingTailTab owner){
		this.setContentType("text/html");
		this.setDocument(doc);
		this.settingHandler = settingHandler;
		this.owner = owner;
		this.setEditable(false);
		this.setBounds(this.getBounds());

	}
	/**
	 * Trim removes first line every time it is called.
	 */
	private void trim (){
		Element element = doc.getDefaultRootElement().getElement(1).getElement(0);

		try {
			doc.remove(element.getStartOffset(), element.getEndOffset() - element.getStartOffset());

		} catch (BadLocationException e) {

			e.printStackTrace();
		}
	}

	private String addTab(){
		// returns empty spaces as specified by user in settings
		String tab = "";
		for (int i=0; i<settingHandler.getTabbSize(); i++){
			// for each tabbSize add one space.
			tab = tab + "&nbsp;";
		}
		return tab;
	}

	private String createHTMLString(String text){

		boolean markAsBold = false;
		boolean markWithColor = false;
		String returnString = null;
		String fontHTMLStartTag = "<font size=\""+(font.getSize()/4)+"\" face=\""+font.getName()+"\">";

		// fix the incoming string
		// find tabs and make sure there are spaces
		if (!text.equalsIgnoreCase("<BR>")){
			text = text.replaceAll("\\t", this.addTab());
			text = text.replaceAll("<", "&lt;");
			text = text.replaceAll(">", "&gt;");
		}
		for (Filter filter: settingHandler.getActiveFilters()){

			if((filter.isStartsWith() && text.startsWith(filter.getTriggerValue()))
					|| (filter.isContains() && text.contains(filter.getTriggerValue()))
					|| (filter.isEndsWith() && text.endsWith(filter.getTriggerValue()))){

				if (filter.isHide()){
					// Do nothing since the filter says to hide this row
					return null;
				}
				if(filter.isPauseOnFind()){
					owner.pauseFileSearch();
				}
				if(filter.isSoundWarning()){
					filter.playSound();
				}
				if (filter.isMarkAsBold()){
					// Mark the row as bold
					markAsBold=true;
				}
				if (filter.isMarkWithColor()){
					// Mark the row with color
					markWithColor = true;
				}

				if (markAsBold && markWithColor){

					returnString = fontHTMLStartTag+"<b><span bgcolor=\"#" + Integer.toHexString(filter.getColor().getRGB()& 0x00ffffff) + "\">" +text+ "</span></b></font>";

				}else if (markAsBold){

					returnString = fontHTMLStartTag+"<b><span>" +text+ "</span></b></font>";

				}else if (markWithColor){

					returnString = fontHTMLStartTag+"<span bgcolor=\"#" + Integer.toHexString(filter.getColor().getRGB()& 0x00ffffff) + "\">" +text+ "</span></font>";
				}
				break;
			}
		}

		if(returnString == null){
			//No filter found
			return fontHTMLStartTag+"<span>" + text + "</span></font>";
		}else{
			return returnString;
		}
	}

	public void append(String text){
		// check if the no of rows exceed the maximum allowed
		while(doc.getDefaultRootElement().getElementCount() >= 2 && doc.getDefaultRootElement().getElement(1).getElementCount() > maxrows){
			this.trim();
		}

		String lineToAdd = this.createHTMLString(text);

		if(lineToAdd != null){

			try {
				htmlEditorKit.insertHTML(doc, doc.getLength(), lineToAdd, 1, 1, null);
				//doc.insertString(caret.getDot(), lineToAdd, htmlEditorKit.getInputAttributes());

			} catch (BadLocationException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
	@Override
	public void repaint() {

		this.lastPainted = System.currentTimeMillis();

		super.repaint();
	}
	@Override
	public void repaint(int x, int y, int width, int height) {

		this.lastPainted = System.currentTimeMillis();

		super.repaint(x, y, width, height);
	}
	@Override
	public void repaint(long tm) {

		this.lastPainted = System.currentTimeMillis();

		super.repaint(tm);
	}
	public long getLastPainted() {
		return lastPainted;
	}
	public int getMaxrows() {
		return maxrows;
	}
	public void setMaxrows(int maxrows) {
		this.maxrows = maxrows;
	}

	@Override
	public Font getFont() {
		return font;
	}

	@Override
	public void setFont(Font font) {
		this.font = font;
	}
	public SettingHandler getSettingHandler() {
		return settingHandler;
	}
	public void setSettingHandler(SettingHandler settingHandler) {
		this.settingHandler = settingHandler;
	}
	@Override
	protected void processKeyEvent(KeyEvent e) {

		if(e.getKeyCode() == KeyEvent.VK_ENTER){
		
			for(KeyListener keyListener : this.getKeyListeners()){
				keyListener.keyPressed(e);
			}			
		}else{
			
			super.processKeyEvent(e);
		}
	}
}
