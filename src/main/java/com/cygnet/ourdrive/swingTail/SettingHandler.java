package com.cygnet.ourdrive.swingTail;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SettingHandler {
	// Settings
	private int rowsToRead						= 50;
	private int rowsToShow						= 200;
	private int polltimeMS						= 500;
	private int tabSize							= 5;
	private Font settingsFont					= new Font("Arial",0,12);
	private boolean lineWrap					= false;
	private boolean showSplash					= true;
	private final ArrayList<String> bookmarks	= new ArrayList<String>();

	//Filter
	private ArrayList<Filter> filters			= new ArrayList<Filter>();


	public SettingHandler() {
		File settingsFile = new File("./settings.xml");
		if (!settingsFile.exists()){
			saveSettings();
		}
		loadSettings();
	}

	public int getRowsToRead() {
		return rowsToRead;
	}
	public void setRowsToRead(int rowsToRead) {
		this.rowsToRead = rowsToRead;
	}
	public int getRowsToShow() {
		return rowsToShow;
	}
	public void setRowsToShow(int rowsToShow) {
		this.rowsToShow = rowsToShow;
	}
	public Font getSettingsFont() {
		return settingsFont;
	}
	public void setSettingsFont(Font settingsFont) {
		this.settingsFont = settingsFont;
	}
	public boolean isLineWrap() {
		return lineWrap;
	}
	public void setLineWrap(boolean lineWrap) {
		this.lineWrap = lineWrap;
	}
	public void setShowSplash(boolean showSplash) {
		this.showSplash = showSplash;
	}

	public boolean isShowSplash() {
		return showSplash;
	}

	public ArrayList<String> getBookmarks() {
		return bookmarks;
	}

	public void setPolltimeMS(int polltimeMS) {
		this.polltimeMS = polltimeMS;
	}

	public int getPolltimeMS() {
		return polltimeMS;
	}

	private void loadSettings(){

		//Try to read setting from config file
		try {
			Document doc = XMLUtils.parseXmlFile("settings.xml", false);

			XPath xPath = XPathFactory.newInstance().newXPath();

			// rowsToRead
			Double doubleRowsToRead = (Double) xPath.evaluate("/swingtail/settings/rowsToRead", doc,XPathConstants.NUMBER);

			if(doubleRowsToRead != null){
				int rowsToRead = doubleRowsToRead.intValue();

				if(rowsToRead >= 0){
					this.rowsToRead = rowsToRead;
				}
			}

			// rowsToShow
			Double doubleRowsToShow = (Double) xPath.evaluate("/swingtail/settings/rowsToShow", doc,XPathConstants.NUMBER);

			if(doubleRowsToShow != null){
				int rowsToShow = doubleRowsToShow.intValue();

				if(rowsToShow >= 0){
					this.rowsToShow = rowsToShow;
				}
			}
			// polltime
			Double doublepolltime = (Double) xPath.evaluate("/swingtail/settings/polltime", doc,XPathConstants.NUMBER);

			if(doublepolltime != null){
				int polltime = doublepolltime.intValue();

				if(polltime >= 0){
					polltimeMS = polltime;
				}
			}

			// lineWrap
			String stringLineWrap = (String) xPath.evaluate("/swingtail/settings/lineWrap", doc,XPathConstants.STRING);
			if (stringLineWrap != null){
				if (stringLineWrap.equalsIgnoreCase("true")){
					lineWrap=true;
				}else{
					lineWrap=false;
				}
			}

			// tabSize
			Double stringTabSize = (Double) xPath.evaluate("/swingtail/settings/tabSize", doc,XPathConstants.NUMBER);
			if(stringTabSize != null){
				int tabSize = stringTabSize.intValue();

				if(tabSize >= 0){
					this.tabSize = tabSize;
				}
			}

			// showSplash
			String stringShowSplash = (String) xPath.evaluate("/swingtail/settings/showSplash", doc,XPathConstants.STRING);
			if (stringShowSplash != null){
				if (stringShowSplash.equalsIgnoreCase("true")){
					showSplash=true;
				}else{
					showSplash=false;
				}
			}
			// filters
			NodeList filterNodeList = (NodeList) xPath.evaluate("/swingtail/settings/filters/*[name()='filter']", doc,XPathConstants.NODESET);
			filters.clear();

			for (int i=0; i< filterNodeList.getLength(); i++){
				Filter filter = new Filter();
				boolean foundFilter = false;
				Node node = filterNodeList.item(i);
				NodeList childList =node.getChildNodes();
				for (int j=0; j< childList.getLength(); j++){
					if (!childList.item(j).getNodeName().equalsIgnoreCase("#text")){
						foundFilter=true;
						if (childList.item(j).getNodeName().equalsIgnoreCase("triggerValue")){
							// Spara trigger value
							filter.setTriggerValue(childList.item(j).getTextContent());
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("startsWith")){
							// Spara starts with
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setStartsWith(true);
							}else{
								filter.setStartsWith(false);
							}
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("contains")){
							// Spara contains
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setContains(true);
							}else{
								filter.setContains(false);
							}
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("endsWith")){
							// Spara ends with
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setEndsWith(true);
							}else{
								filter.setEndsWith(false);
							}
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("markAsBold")){
							// Spara mark as bold
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setMarkAsBold(true);
							}else{
								filter.setMarkAsBold(false);
							}
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("markWithColor")){
							// Spara mark with color
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setMarkWithColor(true);
							}else{
								filter.setMarkWithColor(false);
							}
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("color")){
							// Spara color
							Color color = new Color(Integer.parseInt(childList.item(j).getTextContent()));
							filter.setColor(color);
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("soundWarning")){
							// Spara sound warning
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setSoundWarning(true);
							}else{
								filter.setSoundWarning(false);
							}
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("soundPath")){
							// Spara file path to sound file
							filter.setSoundFilePath(childList.item(j).getTextContent());
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("hide")){
							// Spara hide
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setHide(true);
							}else{
								filter.setHide(false);
							}
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("pause")){
							// Spara hide
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setPauseOnFind(true);
							}else{
								filter.setPauseOnFind(false);
							}
						}else if (childList.item(j).getNodeName().equalsIgnoreCase("active")){
							// Spara hide
							if (childList.item(j).getTextContent().equalsIgnoreCase("true")){
								filter.setFilterActive(true);
							}else{
								filter.setFilterActive(false);
							}
						}else{
						}
					}else{
					}
				}
				
				filter.loadSoundFile();
				
				if (foundFilter){
					filters.add(filter);
					foundFilter = false;
				}
			}

			// bookmarks
			NodeList bookmarkNodeList = (NodeList) xPath.evaluate("/swingtail/bookmarks/*[name()='bookmark']", doc,XPathConstants.NODESET);
			bookmarks.clear();
			for (int i=0; i<bookmarkNodeList.getLength(); i++){
				bookmarks.add(bookmarkNodeList.item(i).getTextContent());
			}

			// font
			String name = (String) xPath.evaluate("/swingtail/settings/font/fontName", doc,XPathConstants.STRING);
			Double doubleStyle = (Double) xPath.evaluate("/swingtail/settings/font/fontStyle", doc,XPathConstants.NUMBER);
			Double doubleSize = (Double) xPath.evaluate("/swingtail/settings/font/fontSize", doc,XPathConstants.NUMBER);

			if (name != null && doubleStyle != null && doubleSize != null){
				int style = doubleStyle.intValue();
				int size = doubleSize.intValue();

				if (style >= 0 && size > 0){
					settingsFont = new Font(name,style,size);
				}
			}

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

	}

	public void saveSettings(){
		Document doc = XMLUtils.createDomDocument();

		// Root
		Element swingtailElement = doc.createElement("swingtail");
		doc.appendChild(swingtailElement);

		// Settings
		Element configElement = doc.createElement("settings");
		swingtailElement.appendChild(configElement);

		// Rows to read
		configElement.appendChild(XMLUtils.createElement("rowsToRead", rowsToRead + "", doc));
		// Rows to show
		configElement.appendChild(XMLUtils.createElement("rowsToShow", rowsToShow + "", doc));
		// Line wrap
		configElement.appendChild(XMLUtils.createElement("lineWrap", lineWrap + "", doc));
		// Tab Size
		configElement.appendChild(XMLUtils.createElement("tabSize", tabSize + "", doc));
		// Show splash
		configElement.appendChild(XMLUtils.createElement("showSplash", showSplash + "", doc));
		// polltime
		configElement.appendChild(XMLUtils.createElement("polltime", polltimeMS + "", doc));

		// Filter
		Element filtersElement = doc.createElement("filters");
		configElement.appendChild(filtersElement);
		for (Filter filter: filters){
			Element filterElement = doc.createElement("filter");
			filtersElement.appendChild(filterElement);

			// TriggerValue
			filterElement.appendChild(XMLUtils.createElement("triggerValue", filter.getTriggerValue(), doc));
			// StartsWith
			filterElement.appendChild(XMLUtils.createElement("startsWith", filter.isStartsWith() + "", doc));
			// Contains
			filterElement.appendChild(XMLUtils.createElement("contains", filter.isContains() + "", doc));
			// EndsWith
			filterElement.appendChild(XMLUtils.createElement("endsWith", filter.isEndsWith() + "", doc));
			// MarkWithBold
			filterElement.appendChild(XMLUtils.createElement("markAsBold", filter.isMarkAsBold() + "", doc));
			// MarkWithColor
			filterElement.appendChild(XMLUtils.createElement("markWithColor", filter.isMarkWithColor() + "", doc));
			//
			filterElement.appendChild(XMLUtils.createElement("color", filter.getColor().getRGB() + "", doc));
			// SoundWarning
			filterElement.appendChild(XMLUtils.createElement("soundWarning", filter.isSoundWarning() + "", doc));
			// SoundPath
			filterElement.appendChild(XMLUtils.createElement("soundPath", filter.getSoundFilePath() + "", doc));
			// Hide
			filterElement.appendChild(XMLUtils.createElement("hide", filter.isHide() + "", doc));
			// Pause
			filterElement.appendChild(XMLUtils.createElement("pause", filter.isPauseOnFind() + "", doc));
			// Active
			filterElement.appendChild(XMLUtils.createElement("active", filter.isFilterActive() + "", doc));

		}

		// Font
		Element fontElement = doc.createElement("font");
		configElement.appendChild(fontElement);

		// Font name
		fontElement.appendChild(XMLUtils.createElement("fontName", settingsFont.getFontName(), doc));
		// Font style
		fontElement.appendChild(XMLUtils.createElement("fontStyle", settingsFont.getStyle() + "", doc));
		// Font size
		fontElement.appendChild(XMLUtils.createElement("fontSize", settingsFont.getSize() + "", doc));

		// Bookmarks
		Element bookmarksElement = doc.createElement("bookmarks");
		swingtailElement.appendChild(bookmarksElement);

		for(String filepath : bookmarks){
			// Bookmark
			bookmarksElement.appendChild(XMLUtils.createCDATAElement("bookmark", filepath, doc));
		}

		try {
			XMLUtils.writeXmlFile(doc, "settings.xml", true);
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Filter> getFilters() {
		return filters;
	}

	public ArrayList<Filter> getActiveFilters() {
		ArrayList<Filter> activeFilters = new ArrayList<Filter>();
		for (Filter filter: filters){
			if (filter.isFilterActive()){
				activeFilters.add(filter);
			}
		}
		return activeFilters;
	}

	public void setFilters(ArrayList<Filter> filters) {
		this.filters = filters;
	}

	public void addFilters(Filter filter) {
		this.filters.add(filter);
	}

	public int getTabbSize() {
		return tabSize;
	}

	public void setTabbSize(int tabbSize) {
		this.tabSize = tabbSize;
	}
}
