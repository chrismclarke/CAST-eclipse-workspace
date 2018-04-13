package cast.server;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.core.*;
import cast.utils.*;

class DownloadStatus {
	String section = null;
	String item = null;
	String fileType = null;
	int noOfSections = 0;
	int noOfItems = 0;
	
	DownloadStatus(int noOfSections) {
		this.noOfSections = noOfSections;
	}
	
	DownloadStatus(String section, int noOfItems) {
		this.section = section;
		this.noOfItems = noOfItems;
	}
	
	DownloadStatus(String item, String fileType) {
		this.item = item;
		this.fileType = fileType;			//	for videos where the video has files with different encodings
	}
}


abstract public class CoreDownloadTask extends SwingWorker<Object, DownloadStatus> {

	static final protected String kBookStructureFile = "book_structure.js";
	
	static public void updateInstalledBook(CastEbook theEbook, boolean onlyIfBookExists) {
		theEbook.setupDom();
		File coreDir = theEbook.getCoreDir();
		String shortBookName = theEbook.getShortBookName();
		
		String longBookName = theEbook.getLongBookName();
		String description = theEbook.getDescription();
		if (description == null)
			description = "Miscellaneous#";
		int hashIndex = description.indexOf("#");
		String groupName;
		if (hashIndex > 0) {
			groupName = description.substring(0, hashIndex);
			description = description.substring(hashIndex + 1);
		}
		else
			groupName = "Miscellaneous";
		String newBookEntry = "addBook('" + shortBookName + "', '" + groupName + "', '" + longBookName + "', '" + description + "');";
		
		File installedBooksFile = new File(coreDir, "installedBooks.js");
		String installedBooks = (installedBooksFile.exists()) ? HtmlHelper.getFileAsString(installedBooksFile, "UTF-8") : "";
		String searchString = "addBook('" + shortBookName + "',";
		int entryStartIndex = installedBooks.indexOf(searchString);
		if (entryStartIndex < 0) {
			if (onlyIfBookExists)
				return;
			else {
				if (installedBooks.length() > 0)
					installedBooks += "\n";
				installedBooks += newBookEntry;
			}
		}
		else {
			int entryEndIndex = installedBooks.indexOf("');", entryStartIndex);
			installedBooks = installedBooks.substring(0, entryStartIndex) + newBookEntry + installedBooks.substring(entryEndIndex + 3);
		}
		
		try {
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(installedBooksFile), "UTF-8"));
			
			output.write(installedBooks);
			
			output.flush();
			output.close();
		} catch (IOException e) {
			System.err.println("Could not update list of custom e-books.");
			e.printStackTrace();
		}
	}
	
	static public void updateInstalledBook(String shortBookName, File coreDir) {
		CastEbook theEbook = new CastEbook(coreDir, shortBookName, false);
		updateInstalledBook(theEbook, false);					//	will create entry if it does not already exist
	}
	
	static public void rememberVideosDownloaded(CastEbook theEbook, boolean nowDownloaded) {
		rememberVideosDownloaded(theEbook.getBookDir(), nowDownloaded);
	}
	
	static public void rememberVideosDownloaded(File bookDir, boolean nowDownloaded) {
		String correctString = nowDownloaded ? "var localVideos = true;" : "var localVideos = false;";
		String wrongString = nowDownloaded ? "var localVideos = false;" : "var localVideos = true;";
		
		File bookStructureFile = new File(bookDir, kBookStructureFile);
		String structureContents = HtmlHelper.getFileAsString(bookStructureFile);
		if (structureContents.indexOf(correctString) >= 0)
			return;
		else if (structureContents.indexOf(wrongString) >= 0)
			structureContents = structureContents.replaceFirst(wrongString, correctString);
		else
			structureContents += "\n" + correctString;
		
		try {
			PrintWriter structureWriter = FileFinder.createUTF8Writer(bookStructureFile);
			structureWriter.print(structureContents);
			structureWriter.flush();
			structureWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static public boolean hasDownloadedVideos(File bookDir) {
		File bookStructureFile = new File(bookDir, kBookStructureFile);
		String structureContents = HtmlHelper.getFileAsString(bookStructureFile);
		return (structureContents == null) ? false : structureContents.indexOf("var localVideos = true;") >= 0;
	}
	
	static protected void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted while searching files");
		}
	}
	
	protected void checkDirExists(File dir) {
		if (!dir.exists())
			dir.mkdir();
	}
	
	public File getFile(String filePath, File baseDir) {
		File theFile = baseDir;
		StringTokenizer st = new StringTokenizer(filePath, "/");
		while (st.hasMoreTokens()) {
			checkDirExists(theFile);
			theFile = new File(theFile, st.nextToken());
		}
		return theFile;
	}
	
	protected void downloadIndexFile(File localFile, String urlString) throws InterruptedException {
		try {
			URL serverUrl = new URL(urlString);
			URLConnection connection = serverUrl.openConnection();
			InputStream input = connection.getInputStream();
			
			Scanner s = new Scanner(input, "UTF-8");
			String content = s.useDelimiter("\\Z").next();
			s.close();
			
			content = content.replace("<script src=\"releaseInfo.js\"></script>",
												"<script src=\"releaseInfo.js\"></script>"
												+ "\n<script src=\"http://" + Options.kCastDownloadUrl + "/core/serverReleaseInfo.js\"></script>");
			
			failIfInterrupted();

			OutputStream output = new FileOutputStream(localFile);
			PrintStream printStream = new PrintStream(output);
			printStream.print(content);
			printStream.close();
		} catch (MalformedURLException e) {
			System.out.println("Bad URL: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
		failIfInterrupted();
	}
	
	protected void downloadFile(File localFile, String urlString) throws InterruptedException {
		try {
			URL serverUrl = new URL(urlString);
			URLConnection connection = serverUrl.openConnection();
			InputStream input = connection.getInputStream();
			byte[] buffer = new byte[4096];
			int n = - 1;

			OutputStream output = new FileOutputStream(localFile);
			
			while ( (n = input.read(buffer)) != -1) {
				output.write(buffer, 0, n);
				failIfInterrupted();
			}
			
			output.close();
		} catch (MalformedURLException e) {
			System.out.println("Bad URL: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
		failIfInterrupted();
	}
	
	
	protected void downloadOneVideo(File sectionDir, String videoName, String videoPath) throws InterruptedException {
		String serverVideoPath = "http://" + Options.kCastDownloadUrl + "/core/bk/" + videoPath;
																													//	still does not include the final ".png" etc
		
		publish(new DownloadStatus(videoName, null));		//	to update the progress bar
		
		publish(new DownloadStatus(videoName, "png"));
		downloadFile(sectionDir, videoName + ".png", serverVideoPath + ".png");
		
		publish(new DownloadStatus(videoName, "mp4"));
		downloadFile(sectionDir, videoName + ".mp4", serverVideoPath + ".mp4");
		
		publish(new DownloadStatus(videoName, "webm"));
		downloadFile(sectionDir, videoName + ".webm", serverVideoPath + ".webm");
	}
	
	private void downloadFile(File sectionDir, String fileName, String serverVideoPath) throws InterruptedException {
		File localFile = new File(sectionDir, fileName);
		downloadFile(localFile, serverVideoPath);
	}
}