package ebook;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

import dataView.*;
import ebookStructure.*;


public class AppletProgram extends JFrame {
	
	static public BookFrame openBook(final String bookName, final File coreDir, String startPage) {
		UiTextStrings uiTextStrings = new UiTextStrings(findLanguage(bookName, coreDir), coreDir);
		
		final JDialog dialog = waitDialog(uiTextStrings);
		dialog.paint(dialog.getGraphics());
		
		CastEbook theBook = new CastEbook(coreDir, bookName);
		BookFrame theBookWindow = BookFrame.showWindow(theBook, uiTextStrings);
		
		dialog.dispose();
		if (startPage != null)
			theBookWindow.showNamedPage(startPage);
		return theBookWindow;
	}
	
	static private String findLanguage(String bookName, File coreDir) {
		File xmlFile = new File(coreDir, "bk/" + bookName + "/xml/book.xml");
		String s = HtmlHelper.getFileAsString(xmlFile);
		int languageIndex = s.indexOf("language=\"");
		if (languageIndex < 0)
			return "en";
		else
			return s.substring(languageIndex + 10, languageIndex + 12);
	}
	
	static private JDialog waitDialog(UiTextStrings uiTextStrings) {
		final JOptionPane optionPane = new JOptionPane(uiTextStrings.translate("Opening CAST e-book.") + "\n"
																			+ uiTextStrings.translate("Wait") + "...   ", JOptionPane.INFORMATION_MESSAGE,
																			JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);

		final JDialog dialog = new JDialog();
		dialog.setTitle(uiTextStrings.translate("Wait message"));
		dialog.setModal(false);

		dialog.setContentPane(optionPane);

		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.pack();

		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		return dialog;
	}
	
	
	public AppletProgram() {
		setLayout(new BorderLayout(0, 0));
		
		final File coreDir = new File("..");
		
		final String[] bookNames = getBookNames(coreDir);
		
			JLabel instructions = new JLabel("Choose an e-book", JLabel.CENTER);
		add("North", instructions);
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				final JComboBox bookChoice = new JComboBox();
				for (int i=0 ; i<bookNames.length ; i++)
					bookChoice.addItem(bookNames[i]);
				bookChoice.setMaximumRowCount(40);
				
				bookChoice.addActionListener(new ActionListener() {
												public void actionPerformed(ActionEvent e) { 
													String selectedBookName = (String)bookChoice.getSelectedItem();
													openBook(selectedBookName, coreDir, null);
													dispose();
												}
				});
			choicePanel.add(bookChoice);
				
		add("Center", choicePanel);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	private String[] getBookNames(File coreDir) {
		File booksDir = new File(coreDir, "bk");
		File[] dirs = booksDir.listFiles();
		int nBooks = 0;
		for (int i=0 ; i<dirs.length; i++)
			if (dirs[i].isDirectory()) {
				File xmlDir = new File(dirs[i], "xml");
				if (xmlDir.exists()) {
					File bookXmlFile = new File(xmlDir, "book.xml");
					if (bookXmlFile.exists())
						nBooks ++;
				}
			}
			
		String bookName[] = new String[nBooks];
		nBooks = 0;
		
		for (int i=0 ; i<dirs.length; i++)
			if (dirs[i].isDirectory()) {
				File xmlDir = new File(dirs[i], "xml");
				if (xmlDir.exists()) {
					File bookXmlFile = new File(xmlDir, "book.xml");
					if (bookXmlFile.exists())
						bookName[nBooks++] = dirs[i].getName();
				}
			}
		return bookName;
	}
	
	static Scanner inputScanner;
	
	static private void startInputThread(BookFrame theBookWindow) {
    
		class PageNameWorker extends SwingWorker<String, Void> {
			private BookFrame theBookWindow;	
			PageNameWorker(BookFrame theBookWindow) {
				this.theBookWindow = theBookWindow;
			}
			protected String doInBackground() throws Exception {
				String nextPageName = inputScanner.next();
				return nextPageName;
			}

			/** Run in event-dispatching thread after doInBackground() completes */
			protected void done() {
				try {
					// Use get() to get the result of doInBackground()
					String pageName = get();
					theBookWindow.showNamedPage(pageName);
					startInputThread(theBookWindow);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		new PageNameWorker(theBookWindow).execute();
	}

	static public void main(final String[] params) {
		Runnable r = new Runnable() {
										public void run() {
											if (params != null && params.length > 0) {
												File coreDir = new File("..");
												String startPage = (params.length > 1) ? params[1] : null;
												BookFrame theBookWindow = openBook(params[0], coreDir, startPage);
												inputScanner = new Scanner(System.in);
												startInputThread(theBookWindow);
											}
											else {
												AppletProgram bookChoiceWindow = new AppletProgram();
												bookChoiceWindow.setTitle("Choose an e-book");
												bookChoiceWindow.setResizable(true);
												
												
												bookChoiceWindow.pack();
												bookChoiceWindow.setVisible(true);
											}
										}
		};
		SwingUtilities.invokeLater(r);
	}
}