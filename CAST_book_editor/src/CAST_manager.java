
//import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
//import java.awt.event.*;

import javax.swing.*;

import cast.core.*;
import ebook.*;


public class CAST_manager {
	
	static Scanner inputScanner;
	
	static private void startInputThread(HashMap<String,BookFrame> theBookWindows, File coreDir) {
    
		class PageNameWorker extends SwingWorker<String, Void> {
			private HashMap<String,BookFrame> theBookWindows;	
			private File coreDir;
			PageNameWorker(HashMap<String,BookFrame> theBookWindows, File coreDir) {
				this.theBookWindows = theBookWindows;
				this.coreDir = coreDir;
			}
			protected String doInBackground() throws Exception {
				String nextPageName = inputScanner.nextLine();
				return nextPageName;
			}

			/** Run in event-dispatching thread after doInBackground() completes */
			protected void done() {
				try {
					// Use get() to get the result of doInBackground()
					String bookAndPageString = get();
					String[] bookAndPage = bookAndPageString.split("[ ,]+");
					BookFrame book = theBookWindows.get(bookAndPage[0]);
					if (book != null && book.isVisible()) {
						book.showNamedPage(bookAndPage[1]);
						book.setVisible(true);
					}
					else {
						book = AppletProgram.openBook(bookAndPage[0], coreDir, bookAndPage[1]);
						theBookWindows.put(bookAndPage[0], book);
					}
					startInputThread(theBookWindows, coreDir);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		new PageNameWorker(theBookWindows, coreDir).execute();
	}
	
	static public void main(final String params[]) {
		SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						File castDir = new File(".");
						File coreDir = new File(castDir, "core");
						Options.initialise(castDir);
		
/*
						boolean capsLockDown = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
						if (capsLockDown) {
							SpecialSettingsDialog specialDialog = new SpecialSettingsDialog(castDir);
							specialDialog.setVisible(true);
		
							if (specialDialog.doSaveSettings()) {
								castDir = specialDialog.getCastFolder();
								Options.isSuperUser = specialDialog.isAdministratorMode();
//								Options.kCastDownloadUrl = specialDialog.getServerUrl();
							}
						}
*/
		
						if (castDir == null || !castDir.exists()) {
							JOptionPane jop = new JOptionPane();
							JOptionPane.showMessageDialog(jop, "No valid CAST directory has been found, so the program will exit.");
							System.exit(0);
						}

						if (params != null && params.length > 0) {
							String bookName = params[0];
							String startPage = (params.length > 1) ? params[1] : null;
							BookFrame theBookWindow = AppletProgram.openBook(bookName, coreDir, startPage);
							HashMap<String,BookFrame> theBookWindows = new HashMap<String,BookFrame>();
							theBookWindows.put(bookName, theBookWindow);
							inputScanner = new Scanner(System.in);
							startInputThread(theBookWindows, coreDir);
						}
						else
							new BookChoiceWindow(castDir);
					}
				});
	}
}