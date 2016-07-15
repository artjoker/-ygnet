package com.cygnet.ourdrive.swingTail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.text.html.HTMLDocument;


public class FileMonitor implements Runnable{
	private final File inFil;
	private final RandomAccessFile file;
	private final HTMLInJEditorPane html;
	private boolean pause = false;
	private boolean closeThread = false;
	private long placeInFile = 0;
	private final Thread thread;
	private final SettingHandler settingHandler;
	private JScrollBar jScrollBar = null;

	public FileMonitor(final HTMLInJEditorPane inHtml, final File inFile, SettingHandler settingHandler, JScrollBar jScrollBar) throws FileNotFoundException {
		inFil = inFile;
		file = new RandomAccessFile(inFile,"r");
		html = inHtml;
		this.settingHandler = settingHandler;

		this.jScrollBar = jScrollBar;

		thread = new Thread(this);
		thread.start();
	}

	public synchronized void run() {

		String inText = null;

		try {
			if(file.length() > 0 && settingHandler.getRowsToRead() > 0){

				int linesFound = 0;

				placeInFile = file.length();

				outer: while(placeInFile > 0){

					byte[] buffer;

					if(placeInFile > 1024){
						file.seek(placeInFile - 1024);
						buffer = new byte[1024];
						file.read(buffer);
					}else{
						file.seek(0);
						buffer = new byte[(int)placeInFile];
						file.read(buffer, 0, (int)placeInFile);
					}

					String currentText = new String(buffer);

					for(int i = currentText.length(); i > 0; i--){

						placeInFile--;

						if(currentText.charAt(i-1) == '\n'){
							linesFound++;

							if(linesFound == (settingHandler.getRowsToRead() + 1)){
								placeInFile++;
								break outer;
							}
						}
					}
				}
				file.seek(placeInFile);

			}else{

				file.seek(file.length());
			}
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		long lastAppended = Long.MAX_VALUE;
		boolean autoscroll = true;

		while(true){
			try{
			if (closeThread){
				try {
					file.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
				return;

			}else if (pause){

				try {
					this.wait();
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}else{
				try {

					if (placeInFile>file.length()){
						placeInFile = 0;
						file.seek(placeInFile);
					}

					if((autoscroll && lastAppended >= html.getLastPainted()) || (jScrollBar.getValue() + jScrollBar.getVisibleAmount()) == this.jScrollBar.getMaximum()){

						autoscroll = true;

					}else{

						autoscroll = false;
					}
					
					
					while((inText = file.readLine()) != null){
						if (pause){
							break;
						}

						lastAppended = System.currentTimeMillis();
						html.append(inText.toString());

						if(autoscroll){
							try{
								html.setCaretPosition(((HTMLDocument)html.getDocument()).getLength());
							}catch(RuntimeException e){
								e.printStackTrace();
							}
						}

						placeInFile = file.getFilePointer();
					}

				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, this, "Error reading file " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
					this.setPause(true);
					//break;
				}

				try {
					Thread.sleep(settingHandler.getPolltimeMS());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			}catch (Throwable e){
				// something has gone terrible wrong...
				StackTraceElement [] stackTElement = e.getStackTrace();
				String stackTrace = "";
				for (StackTraceElement element: stackTElement){
					stackTrace = stackTrace + element.toString() + "\n";
				}
				JOptionPane.showMessageDialog(null, stackTrace, "Something bad has happend...", JOptionPane.ERROR_MESSAGE);
				this.closeThread = true;
				this.closeThread();
			}
			
		}
	}

	public void setPause(boolean pause) {

		if(!closeThread){
			this.pause = pause;

			if(!pause && thread.getState() == Thread.State.WAITING){
				synchronized(this){
					notifyAll();
				}
			}
		}
	}

	public boolean isPause() {
		return pause;
	}
	public boolean isCloseThread() {
		return closeThread;
	}

	public void closeThread() {
		closeThread = true;
		setPause(false);
	}
	public File getFile(){
		return inFil;
	}

}
