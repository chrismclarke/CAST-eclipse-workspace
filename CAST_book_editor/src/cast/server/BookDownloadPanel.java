package cast.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.bookEditor.*;
import cast.core.*;
import cast.utils.*;


public class BookDownloadPanel extends JPanel {
	static final public Color kDarkBlue = new Color(0x000066);
	
	private class DescriptionHash extends Hashtable<String, String> { }
	private class BookGroupHash extends Hashtable<String, DescriptionHash> { }
	
	private StringsHash bookDescriptionsHash;
	private DatesHash localBooksHash, serverBooksHash;
	private File coreDir;
	
	private JComboBox groupChoice, bookChoice;
	private JButton downloadButton, cancelButton;
	private JLabel cancelLabel;
	private CastProgressBar theProgressBar;
	private JTextArea bookDescription;
	
	private BookDownloadTask downloadTask = null;
	
	private int noOfFiles, currentFileIndex;
	
	public BookDownloadPanel(final DatesHash localBooksHash, final DatesHash serverBooksHash,
																		final StringsHash bookDescriptionsHash, String emptyLabel,
																		String downloadButtonName, final File coreDir, final JFrame parentFrame) {
		this.coreDir = coreDir;
		this.localBooksHash = localBooksHash;
		this.serverBooksHash = serverBooksHash;
		this.bookDescriptionsHash = bookDescriptionsHash;
		
		setLayout(new BorderLayout(0, 16));
			String[] newBooks = serverBooksHash.matchCustomBooks(localBooksHash);
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout(20, 10));
		
				JPanel bookChoicePanel = new JPanel();
				bookChoicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					if (newBooks.length == 0) {
						JLabel bookLabel = new JLabel(emptyLabel, JLabel.LEFT);
						bookLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
						bookChoicePanel.add(bookLabel);
					}
					else {
							JPanel choiceMenuPanel = new JPanel();
							choiceMenuPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 3));
								
								JPanel groupPanel = new JPanel();
								groupPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
								
									JLabel groupLabel = new JLabel("Collection:", JLabel.LEFT);
								groupPanel.add(groupLabel);
								
									final BookGroupHash groupDescriptions = findBookGroups(newBooks);
									groupChoice = new JComboBox();
									addKeysInOrder(groupDescriptions, groupChoice);
									
									groupChoice.addActionListener(new ActionListener() {
																										public void actionPerformed(ActionEvent e) {
																											String groupName = (String) groupChoice.getSelectedItem();
																											bookChoice.removeAllItems();
																											DescriptionHash theDescriptions = groupDescriptions.get(groupName);
																											addKeysInOrder(theDescriptions, bookChoice);
																											bookChoice.setSelectedIndex(0);
																											parentFrame.pack();
																										}
																								});
								groupPanel.add(groupChoice);
								
							choiceMenuPanel.add(groupPanel);
									
								JPanel bookPanel = new JPanel();
								bookPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
								
									JLabel bookLabel = new JLabel("E-book:", JLabel.LEFT);
								bookPanel.add(bookLabel);
									
									bookChoice = new JComboBox();
									bookChoice.addActionListener(new ActionListener() {
																										public void actionPerformed(ActionEvent e) {
																											String groupName = (String) groupChoice.getSelectedItem();
																											String bookName = (String) bookChoice.getSelectedItem();
																											if (bookName != null)
																												bookDescription.setText(groupDescriptions.get(groupName).get(bookName));
																										}
																								});
								bookPanel.add(bookChoice);
								
							choiceMenuPanel.add(bookPanel);
							
						bookChoicePanel.add(choiceMenuPanel);
						
							JPanel buttonPanel = new JPanel();
							buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 3));
							
								downloadButton = new JButton(downloadButtonName);
								downloadButton.addActionListener(new ActionListener() {
																									public void actionPerformed(ActionEvent e) {
																										String bookName = (String) bookChoice.getSelectedItem();
																										File booksDir = new File(coreDir, "bk");
																										File bookDir = new File(booksDir, bookName);
																										
																										groupChoice.setEnabled(false);
																										bookChoice.setEnabled(false);
																										downloadButton.setEnabled(false);
																										
																										downloadBook(bookDir);
																										cancelButton.setEnabled(true);
																										cancelLabel.setForeground(getBackground());		//	to hide it
																									}
																							});
							buttonPanel.add(downloadButton);
							
								JButton tryButton = new JButton("Try e-book on server");
								tryButton.addActionListener(new ActionListener() {
																									public void actionPerformed(ActionEvent e) {
																										String bookName = (String) bookChoice.getSelectedItem();
																										AdvancedWindow.showUrl("http://" + Options.kCastDownloadUrl + "?book=" + bookName);
																									}
																							});
							buttonPanel.add(tryButton);
						
						bookChoicePanel.add(buttonPanel);
					}
			topPanel.add("North", bookChoicePanel);
				
				theProgressBar = new CastProgressBar("Download progress");
					
			topPanel.add("Center", theProgressBar);
			
		add("North", topPanel);
		
			bookDescription = new JTextArea(5, 20);
			bookDescription.setEditable(false);
			bookDescription.setLineWrap(true);
			bookDescription.setWrapStyleWord(true);
			if (newBooks.length > 0)
				bookDescription.setText(bookDescriptionsHash.getStringValue(newBooks[0]));
			JScrollPane scrollPane = new JScrollPane(bookDescription); 
		add("Center", scrollPane);
		
			JPanel cancelPanel = new JPanel();
			cancelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0)); 
			
				cancelButton = new JButton("Cancel download");
				cancelButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							cancelLabel.setText("Cancelling...");
																							downloadTask.cancel(true);
																							cancelButton.setEnabled(false);
																							cancelLabel.setForeground(Color.blue);
																						}
																				});
				cancelButton.setEnabled(false);
				
			cancelPanel.add(cancelButton);
				
				cancelLabel = new JLabel("Cancelling...", JLabel.LEFT);
				cancelLabel.setForeground(getBackground());				//	to make it invisible but get it laid out large enough
				cancelLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
				
			cancelPanel.add(cancelLabel);
		
		add("South", cancelPanel);
		
		if (groupChoice != null)
			groupChoice.setSelectedIndex(0);
	}
	
	private void addKeysInOrder(Hashtable ht, JComboBox choice) {
		ArrayList al = new ArrayList(ht.keySet());
		Collections.sort(al);
		Iterator i = al.iterator();
		while (i.hasNext()) { 
			String itemName = (String)i.next();
			choice.addItem(itemName);
		}
	}
	
	private BookGroupHash findBookGroups(String[] newBooks) {
		BookGroupHash groups = new BookGroupHash();
		for (int i=0 ; i<newBooks.length ; i++) {
			String bookName = newBooks[i];
			String groupDescription = bookDescriptionsHash.get(bookName);
			int hashIndex = groupDescription.indexOf("#");
			String groupName = (hashIndex <= 0) ? BookSettings.kDefaultBookGroup : groupDescription.substring(0, hashIndex);
			String description = groupDescription.substring(hashIndex + 1);
			
			if (!groupName.equals("core")) {
				DescriptionHash groupDescriptions = groups.get(groupName);
				if (groupDescriptions == null) {
					groupDescriptions = new DescriptionHash();
					groups.put(groupName, groupDescriptions);
				}
				groupDescriptions.put(bookName, description);
			}
		}
		
		return groups;
	}
	
	public boolean isWaiting() {
		return downloadTask != null;
	}
	
	
	private void downloadBook(File bookDir) {
		downloadTask = new BookDownloadTask(bookDir, this);
		downloadTask.execute();
	}
	
	public void setFinished() {
		String groupName = (String) groupChoice.getSelectedItem();
		String bookName = (String) bookChoice.getSelectedItem();
		theProgressBar.setValue(noOfFiles, "Finished");
		bookChoice.removeItemAt(bookChoice.getSelectedIndex());
		groupChoice.setEnabled(true);
		bookChoice.setEnabled(true);
		downloadButton.setEnabled(true);
		cancelButton.setEnabled(false);
		downloadTask = null;
		
		finaliseBookDownload(bookName);
		
		if (bookChoice.getItemCount() == 0) {
			groupChoice.removeItem(groupName);
			groupChoice.setSelectedItem(0);
		}
		else
			bookChoice.removeItem(bookName);
	}
	
	private void finaliseBookDownload(String bookName) {
		localBooksHash.updateEntry(bookName, serverBooksHash);
		File datesDir = new File(coreDir, "dates");
		localBooksHash.saveToFile(new File(datesDir, CoreCopyTask.kBookDatesFileName));
		
		CoreDownloadTask.updateInstalledBook(bookName, coreDir);
		
		VideosDownloadTask.rememberVideosDownloaded(new CastEbook(coreDir, bookName, false), false);
					//	when a book is downloaded, the videos are marked as being on the server.
	}
	
	public void setCancelled() {
		groupChoice.setEnabled(true);
		bookChoice.setEnabled(true);
		downloadButton.setEnabled(true);
		cancelButton.setEnabled(false);
		theProgressBar.clear();
		downloadTask = null;
		cancelLabel.setText("Cancelled");
	}
	
	public void updateForStart(int noOfFiles) {
		this.noOfFiles = noOfFiles;
		currentFileIndex = -1;
		
		theProgressBar.initialise(noOfFiles, "Finding files in book...");
	}
	
	public void updateForNewFile(String fileName) {
		currentFileIndex ++;
		theProgressBar.setValue(currentFileIndex, fileName);
	}
	
}
