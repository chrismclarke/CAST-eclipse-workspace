package cast.server;

import java.io.*;
import java.net.*;
import java.util.*;

import cast.core.*;



public class BookDownloadTask extends CoreDownloadTask {
	
	private File bookDir;
	private BookDownloadPanel downloadPanel;
	private DatesHash bookFileDates;
	
	private File currentFile = null;
	
	public BookDownloadTask(File bookDir, BookDownloadPanel downloadPanel) {
		this.bookDir = bookDir;
		this.downloadPanel = downloadPanel;
		try {
			bookFileDates = new DatesHash(new URL("http://" + Options.kCastDownloadUrl + "/core/bk/"
																			+ bookDir.getName() + "/" + DatesHash.kDateStampFileName));
			
			checkDirExists(bookDir);
			checkDirExists(new File(bookDir, "images"));
			checkDirExists(new File(bookDir, "sec"));
			checkDirExists(new File(bookDir, "text"));
			checkDirExists(new File(bookDir, "videos"));
			checkDirExists(new File(bookDir, "xml"));
			checkDirExists(new File(bookDir, "tests"));
		} catch (MalformedURLException e) {
		}
	}
	
	public Object doInBackground() throws Exception {
		TreeSet<String> filesToDownload = bookFileDates.allFileNames();
		String bookName = bookDir.getName();
		
		publish(new DownloadStatus(filesToDownload.size()));
		
		for (String fileName : filesToDownload) {
			downloadFile(fileName, "http://" + Options.kCastDownloadUrl + "/core/bk/" + bookName);
			
			failIfInterrupted();
			publish(new DownloadStatus(fileName, 0));
		}
		
		try {
			StringsHash serverReleaseInfo = new StringsHash(new URL("http://" + Options.kCastDownloadUrl + "/core/" + CoreCopyTask.kReleasInfoFileName));
			File coreDir = bookDir.getParentFile().getParentFile();
			File localReleaseInfoFile = new File(coreDir, CoreCopyTask.kReleasInfoFileName);
			StringsHash localReleaseInfo = new StringsHash(localReleaseInfoFile);
			localReleaseInfo.initJsBookEntries();
			String bookDateKey = "bookDates['" + bookName + "'] ";
			localReleaseInfo.addJsBookDate(bookName, serverReleaseInfo.get(bookDateKey));
			
			localReleaseInfo.saveToFile(localReleaseInfoFile);
		} catch (MalformedURLException e) {
		}
		
		return null;
	}

	public void done() {
		if (currentFile != null)
			cancelBookDownload();
		if (isCancelled())
			downloadPanel.setCancelled();
		else {
			bookFileDates.saveToFile(new File(bookDir, DatesHash.kDateStampFileName));
			downloadPanel.setFinished();
		}
	}
	
	private void cancelBookDownload() {
		if (currentFile != null) {
			if (currentFile.exists())
				currentFile.delete();
			downloadPanel.setCancelled();
		}
	}
	
	
	protected void process(final java.util.List<DownloadStatus> chunks) {
		for (final DownloadStatus status : chunks) {
			if (status.section == null)
				downloadPanel.updateForStart(status.noOfSections);
			else
				downloadPanel.updateForNewFile("Downloading " + status.section);
		}
	}
	
	private void downloadFile(String filePath, String serverBookPath) throws InterruptedException {
		currentFile = getFile(filePath, bookDir);
		String urlString = serverBookPath + "/" + filePath;
		downloadFile(currentFile, urlString);
		
		currentFile = null;
	}
}