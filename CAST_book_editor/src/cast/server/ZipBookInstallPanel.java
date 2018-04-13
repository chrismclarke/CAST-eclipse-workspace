package cast.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import cast.utils.*;


public class ZipBookInstallPanel extends JPanel {
	private DatesHash localBooksHash;
	private File coreDir, newBookDir;
	
	private JButton installButton;
	private CastProgressBar theProgressBar;
	
	private ZipBookInstallTask installTask = null;
	
	private int noOfFiles, currentFileIndex;
	
	public ZipBookInstallPanel(DatesHash localBooksHash, final File coreDir, final JFrame parentFrame) {
		this.coreDir = coreDir;
		this.localBooksHash = localBooksHash;
		
		setLayout(new BorderLayout(0, 16));
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				installButton = new JButton("Install e-book from zip file...");
				installButton.addActionListener(new ActionListener() {
																									public void actionPerformed(ActionEvent e) {
																											JFileChooser fc = new JFileChooser();
																											fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
																																										public boolean accept(File f) {
																																												if (f.isDirectory()) {
																																														return true;
																																												} else {
																																														return f.getName().toLowerCase().endsWith(".zip");
																																												}
																																										}
																																										
																																										public String getDescription() {
																																											return "zip files";
																																										}
																																								});
																											fc.setDialogTitle("Select ZIP file containing e-book");
																											fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

																									//	Show open dialog; this method does not return until the dialog is closed
																											int result = fc.showOpenDialog(parentFrame);
																											
																											if (result == JFileChooser.APPROVE_OPTION) {
																												File zipFile = fc.getSelectedFile();
																												installBook(zipFile, coreDir);
																											}
																									}
																							});
			buttonPanel.add(installButton);
		add("Center", buttonPanel);
		
			theProgressBar = new CastProgressBar("Install progress");
				
		add("South", theProgressBar);
	}
	
	public boolean isWaiting() {
		return installTask != null;
	}
	
	
	private void installBook(File zipFile, File coreDir) {
		String zipFileName = zipFile.getName();
		zipFileName = zipFileName.substring(0, zipFileName.length() - 4);		//	to remove '.zip'
		
		File booksDir = new File(coreDir, "bk");
		newBookDir = new File(booksDir, zipFileName);
		if (newBookDir.exists()) {
			JOptionPane.showMessageDialog(this, "An e-book called \"" + zipFileName
								+ "\" already exists. Delete it before installing this e-book.", "Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		installTask = new ZipBookInstallTask(zipFile, booksDir, this);
		installTask.execute();
	}
	
	public void setFinished() {
		theProgressBar.setValue(noOfFiles, "Finished");
		installButton.setEnabled(true);
		installTask = null;
		
		CoreDownloadTask.rememberVideosDownloaded(newBookDir, false);
		
		File bookDates = new File(newBookDir, DatesHash.kDateStampFileName);
		DatesHash newBookDates = new DatesHash(bookDates);
		String bookName = newBookDir.getName();
		localBooksHash.put(bookName, Long.valueOf(newBookDates.latestChange()));
		File datesDir = new File(coreDir, "dates");
		localBooksHash.saveToFile(new File(datesDir, CoreCopyTask.kBookDatesFileName));
		
		CoreDownloadTask.updateInstalledBook(bookName, coreDir);
	}
	
	public void setCancelled() {
		installButton.setEnabled(true);
		theProgressBar.clear();
		installTask = null;
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
