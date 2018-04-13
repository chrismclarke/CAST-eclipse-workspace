package cast.server;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import org.apache.commons.net.ftp.FTPClient;

import cast.utils.*;




public class UpdateServerTask extends CoreCopyTask {
	static final private int kUploadStages = 5;
	
	private String castServerUrl;
	private FTPClient ftpClient;
	private UpdateServerFrame callingWindow;
	private boolean uploadTheManagerJar, uploadJavaJar;
	
	private String currentPath = "";
	private File currentDir;
	
	private HashSet collectionsUpdated = new HashSet();
	private HashSet booksUpdated = new HashSet();
//	private HashSet booksVideosUpdated = new HashSet();
	private HashSet sectionsUpdated = new HashSet();
	private boolean coreUpdated = false;
	
	public UpdateServerTask(File castSourceDir, boolean uploadTheManagerJar,
															boolean uploadJavaJar, String castServerUrl, HashSet<String> bookDirs,
															FTPClient ftpClient, JLabel progressStage, CastProgressBar uploadStageProgress,
															CastProgressBar uploadItemProgress, UpdateServerFrame callingWindow) {
		super(castSourceDir, "all", bookDirs, progressStage, uploadStageProgress, uploadItemProgress);
			//		"all" means that the "collectionDates" dates hash contains entries for all known collections
		uploadStageProgress.initialise(kCoreCopyStages + kUploadStages, "");
		
		this.castServerUrl = castServerUrl;
		this.ftpClient = ftpClient;
		this.callingWindow = callingWindow;
		this.uploadTheManagerJar = uploadTheManagerJar;
		this.uploadJavaJar = uploadJavaJar;
		currentDir = castSourceDir;
	}
	

	public void done() {
		super.done();
		try {
			ftpClient.logout();
		} catch (IOException e) {
		}
		callingWindow.enableUploadButton();
	}
	
//-----------------------------------------------------------------
	
	protected void copyToDestination() throws InterruptedException {
		if (!uploadTheManagerJar)
			coreDates.remove("Start_CAST.jar");
		if (!uploadJavaJar)
			coreDates.remove("core/java/");
		
		File coreDir = new File(castSourceDir, "core");
		File collectionsDir = new File(coreDir, "collections");
		
		uploadCoreFiles();
		
		uploadCollectionFiles(collectionsDir);
		
		uploadBooks(coreDir);
		
		uploadVideos(coreDir);
		
		uploadSections(coreDir);
		
//		incrementVersions();
	}
	
	private void uploadCoreFiles() throws InterruptedException {
		String serverDatesUrlString = "core/dates/" + kCoreDatesFileName;
		coreUpdated = uploadFilesAndFolders(coreDates, serverDatesUrlString, "", "Uploading miscelleneous core files...");
		if (coreUpdated)
			uploadFile(coreDates, serverDatesUrlString);
		uploadFile(versionInfo, "core/" + kReleasInfoFileName);
		StringsHash serverVersionInfo = getServerVersionInfo();
		uploadFile(serverVersionInfo, "core/" + kServerReleasInfoFileName);
		
		uploadFile("core/" + kInstalledBooksFileName);
	}
	
	private StringsHash getServerVersionInfo() {
		StringsHash serverHash = new StringsHash();
		Enumeration<String> e = versionInfo.keys();
		while (e.hasMoreElements()) {
			String theKey = e.nextElement();			//	 of form "var xxx = " or "xxx[xxx] = "
			String theValue = versionInfo.get(theKey);
			String serverKey;
			if (theKey.startsWith("var "))			//	 of form "var xxx = "
				serverKey = theKey.replace("var ", "var server_");
			else																//	 of form "xxx[xxx] = "
				serverKey = "server_" + theKey;
			serverHash.put(serverKey, theValue);
		}
		return serverHash;
	}
	
	private void uploadCollectionFiles(File collectionsDir) throws InterruptedException {
		publish(new CopyStatus("Uploading files specific to collections...", collectionDates.size(), RESET_BAR));
		failIfInterrupted();
		
		Enumeration<String> collectionNames = collectionDates.keys();
		while (collectionNames.hasMoreElements()) {
			String collectionName = collectionNames.nextElement();
			publish(new CopyStatus(collectionName, INCREMENT_BAR));
			failIfInterrupted();
			
			DatesHash localCollectionHash = collectionDates.get(collectionName);
			String serverDatesUrlString = "core/dates/" + kCollectionDatesPrefix + collectionName + ".text";
			boolean changed = uploadFilesAndFolders(localCollectionHash, serverDatesUrlString, "", null);
			if (changed) {
				collectionsUpdated.add(collectionName);
				uploadFile(localCollectionHash, serverDatesUrlString);
			}
		}
		
		String indexString = "<html>\n";
		indexString += "<head>\n";
		indexString += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n";
		indexString += "<title>Redirect</title>\n";
		indexString += "<script src=\"init/indexScript.js\"></script>\n";
		indexString += "<script type=\"text/javascript\" language=\"javascript\">\n";
		indexString += "  parseSearchString(\"public\");\n";
		indexString += "</script>\n";
		indexString += "</head>\n\n";
		indexString += "<body>\n";
		indexString += "</body>\n";
		indexString += "</html>";
		uploadString(indexString, "index.html");
	}

	
	private void uploadBooks(File coreDir) throws InterruptedException {
		String serverBookDatesPath = "core/dates/" + kBookDatesFileName;
		DatesHash serverBookDates = new DatesHash(castServerUrl + "/" + serverBookDatesPath);
		String serverBookDescriptionsPath = "core/dates/" + kBookDescriptionsFileName;
		StringsHash serverBookDescriptions = getServerDescriptions(serverBookDescriptionsPath);
		
		String[] booksNeedingUpdate = DatesHash.itemsNeedingUpdate(serverBookDates, bookDates);
		publish(new CopyStatus("Copying main book files ...", booksNeedingUpdate.length, RESET_BAR));
		failIfInterrupted();
		
		for (int i=0 ; i<booksNeedingUpdate.length ; i++) {
			String bookName = booksNeedingUpdate[i];
			publish(new CopyStatus(bookName, INCREMENT_BAR));
			failIfInterrupted();
			
			
			File bookDir = new File(new File(coreDir, "bk"), bookName);
			File bookDateStampFile = DatesHash.getDateStampFile(bookDir);
			DatesHash bookFilesHash = new DatesHash(bookDateStampFile);
			
			String[] filesNeedingUpdate = itemsNeedingUpdate(bookFilesHash, "core/bk/" + bookName + "/"
																																+ DatesHash.kDateStampFileName);
			for (int j=0 ; j<filesNeedingUpdate.length ; j++) {
				publish(new CopyStatus(bookName + "/" + filesNeedingUpdate[j], CHANGE_TEXT));
				failIfInterrupted();
				uploadFile("core/bk/" + bookName + "/" + filesNeedingUpdate[j]);
			}
			
			if (filesNeedingUpdate.length > 0) {
				booksUpdated.add(bookName);
				uploadFile("core/bk/" + bookName + "/" + DatesHash.kDateStampFileName);
			}
			
			serverBookDates.put(bookName, bookDates.get(bookName));
			serverBookDescriptions.put(bookName, bookDescriptions.get(bookName));
		}
		
		if (booksNeedingUpdate.length > 0) {
			uploadFile(serverBookDates, serverBookDatesPath);
			uploadFile(serverBookDescriptions, serverBookDescriptionsPath);
		}
	}

	
	private void uploadVideos(File coreDir) throws InterruptedException {
		String serverVideoDatesPath = "core/dates/" + kVideoDatesFileName;
		DatesHash serverVideoDates = new DatesHash(castServerUrl + "/" + serverVideoDatesPath);
		
		String[] videosNeedingUpdate = DatesHash.itemsNeedingUpdate(serverVideoDates, videoDates);
		publish(new CopyStatus("Copying video files ...", videosNeedingUpdate.length, RESET_BAR));
		failIfInterrupted();
		
		for (int i=0 ; i<videosNeedingUpdate.length ; i++) {
			String videoPath = videosNeedingUpdate[i];					//		e.g. general/videos/bivarCat/xxxx
			publish(new CopyStatus(videoPath, INCREMENT_BAR));
			failIfInterrupted();
			
			uploadVideo(videoPath);
			
			serverVideoDates.put(videoPath, videoDates.get(videoPath));
			uploadFile(serverVideoDates, serverVideoDatesPath);					//	update video dates on server after each video is uploaded
																																	//	in case upload is cancelled before finish
		}
	}
	
	private void uploadSections(File coreDir) throws InterruptedException {
		String serverSectionDatesPath = "core/dates/" + kSectionDatesFileName;
		DatesHash serverSectionDates = new DatesHash(castServerUrl + "/" + serverSectionDatesPath);
		
		String[] sectionsNeedingUpdate = DatesHash.itemsNeedingUpdate(serverSectionDates, sectionDates);
		publish(new CopyStatus("Copying section files ...", sectionsNeedingUpdate.length, RESET_BAR));
		failIfInterrupted();
		
		for (int i=0 ; i<sectionsNeedingUpdate.length ; i++) {
			String sectionName = sectionsNeedingUpdate[i];
			publish(new CopyStatus(sectionName, INCREMENT_BAR));
			failIfInterrupted();
			
			File sectionDir = new File(coreDir, sectionName);
			File sectionDateStampFile = DatesHash.getDateStampFile(sectionDir);
			DatesHash sectionFilesHash = new DatesHash(sectionDateStampFile);
			
			String[] filesNeedingUpdate = itemsNeedingUpdate(sectionFilesHash, "core/" + sectionName + "/"
																																			+ DatesHash.kDateStampFileName);
			for (int j=0 ; j<filesNeedingUpdate.length ; j++) {
				publish(new CopyStatus(sectionName + "/" + filesNeedingUpdate[j], CHANGE_TEXT));
				failIfInterrupted();
				uploadFile("core/" + sectionName + "/" + filesNeedingUpdate[j]);
			}
			
			if (filesNeedingUpdate.length > 0) {
				sectionsUpdated.add(sectionName);
				uploadFile("core/" + sectionName + "/" + DatesHash.kDateStampFileName);
			}
			serverSectionDates.put(sectionName, sectionDates.get(sectionName));
		}
		
		if (sectionsNeedingUpdate.length > 0)
			uploadFile(serverSectionDates, serverSectionDatesPath);
	}
	
//----------------------------------------------------------------------
	
	private boolean uploadFilesAndFolders(DatesHash localDates, String serverDatesPath,
																String localBasePath, String uploadMessage) throws InterruptedException {
																					//	serverDatesPath is a path relative to castServerUrl
																					//	all file names in dates HashTable are relative to "<CAST>/localBasePath"
																					//	localBasePath is either blank or ends in "/"
		String[] needingUpdate = itemsNeedingUpdate(localDates, serverDatesPath);			
		
		if (uploadMessage != null) {
			publish(new CopyStatus(uploadMessage, needingUpdate.length, RESET_BAR));
			failIfInterrupted();
		}
		
		for (int i=0 ; i<needingUpdate.length ; i++) {
			String itemName = needingUpdate[i];
			
			publish(new CopyStatus(localBasePath + itemName, (uploadMessage == null) ? CHANGE_TEXT : INCREMENT_BAR));
			failIfInterrupted();
			
			if (itemName.endsWith("/")) {
				String folderName = itemName.substring(0, itemName.length() - 1);
				
				File dir = castSourceDir;
				StringTokenizer st = new StringTokenizer(localBasePath, "/");
				while (st.hasMoreTokens()) {
					String nextDir = st.nextToken();
					dir = new File(dir, nextDir);
				}
				dir = new File(dir, folderName);
				
				File localDateStampFile = new File(dir, DatesHash.kDateStampFileName);
				DatesHash localFolderHash = new DatesHash(localDateStampFile);
				String folderRelativePath = localBasePath + folderName + "/";		//	ends in "/" because it is used as prefix for fileNames in directory
				uploadFilesAndFolders(localFolderHash, folderRelativePath + DatesHash.kDateStampFileName, folderRelativePath, null);
				uploadFile(folderRelativePath + DatesHash.kDateStampFileName);
			}
			else
				uploadFile(localBasePath + itemName);
		}
		return needingUpdate.length > 0;
	}
	
	private String[] itemsNeedingUpdate(DatesHash localDates, String serverDatesPath) {
		DatesHash serverDates = new DatesHash(castServerUrl + "/" + serverDatesPath);
		String[] updateItems = DatesHash.itemsNeedingUpdate(serverDates, localDates);
		
		return updateItems;
	}
	
	private StringsHash getServerDescriptions(String serverNamesPath) {
		try {
			URL serverNamesUrl = new URL(castServerUrl + "/" + serverNamesPath);
			return new StringsHash(serverNamesUrl);
		} catch (MalformedURLException e) {
			return new StringsHash();
		}
	}
	
	private void uploadFile(String filePath) {			//	filePath is relative to the CAST folder
		try {
			String fileName = prepareForUpload(filePath);
			
			File sourceFile = new File(currentDir, fileName);
			FileInputStream fis = new FileInputStream(sourceFile);
			ftpClient.storeFile(fileName, fis);
			fis.close();
		} catch (Exception e) {
			System.out.println("Could not upload file: " + filePath);
			e.printStackTrace();
		}
		
		System.out.println("Finished uploading: " + filePath);
	}
	
	private String prepareForUpload(String filePath) throws Exception {			//	filePath is relative to the CAST folder
		int lastSlashIndex = filePath.lastIndexOf("/");
		String relativeFilePath = (lastSlashIndex <= 0) ? "" : filePath.substring(0, lastSlashIndex);
		String fileName = filePath.substring(lastSlashIndex + 1);
		String relativeCurrentPath = currentPath;
		
									//		Strip off common directories in file path and current path
		StringTokenizer stCurrent = new StringTokenizer(relativeCurrentPath, "/");
		StringTokenizer stFile = new StringTokenizer(relativeFilePath, "/");
		while (stCurrent.hasMoreTokens() && stFile.hasMoreTokens()) {
			String currentFirstDir = stCurrent.nextToken();
			String fileFirstDir = stFile.nextToken();
			if (currentFirstDir.equals(fileFirstDir)) {
				trimFirstDir(relativeFilePath);
				trimFirstDir(relativeCurrentPath);
			}
			else
				break;
		}
		
		while (relativeCurrentPath.length() > 0) {
			currentDir = currentDir.getParentFile();
			currentPath = trimLastDir(currentPath);
			relativeCurrentPath = trimLastDir(relativeCurrentPath);
			ftpClient.changeWorkingDirectory("..");
		}
		
		stFile = new StringTokenizer(relativeFilePath, "/");
		while (stFile.hasMoreTokens()) {
			String destDir = stFile.nextToken();
			currentDir = new File(currentDir, destDir);
			currentPath = (currentPath.length() == 0) ? destDir : (currentPath + "/" + destDir);
			try {
				ftpClient.makeDirectory(destDir);
			} catch (IOException e) {						//	it does not matter if the directory already exists
			}
			ftpClient.changeWorkingDirectory(destDir);
		}
		return fileName;
	}
	
	private String trimFirstDir(String path) {
		int firstSlashIndex = path.indexOf("/");		//	could be -1
		return (firstSlashIndex < 0) ? "" : path.substring(firstSlashIndex + 1);
	}
	
	private String trimLastDir(String path) {
		int lastSlashIndex = path.lastIndexOf("/");		//	could be -1
		return (lastSlashIndex < 0) ? "" : path.substring(0, lastSlashIndex);
	}
	
	private void uploadFile(DatesHash datesHash, String filePath) {			//	filePath is relative to the CAST folder
		try {
			String fileName = prepareForUpload(filePath);
			
			InputStream is = new ByteArrayInputStream(datesHash.toString().getBytes());
			
			ftpClient.storeFile(fileName, is);
			is.close();
		} catch (Exception e) {
			System.out.println("***Could not upload: " + filePath);
			e.printStackTrace();
		}
		
		System.out.println("***Finished uploading: " + filePath	);
	}
	
	private void uploadFile(StringsHash namesHash, String filePath) {			//	filePath is relative to the CAST folder
		String namesHashString = namesHash.toString();
		uploadString(namesHashString, filePath);
	}
	
	private void uploadString(String fileContent, String filePath) {			//	filePath is relative to the CAST folder
		try {
			String fileName = prepareForUpload(filePath);
			
			InputStream is = new ByteArrayInputStream(fileContent.getBytes());
			
			ftpClient.storeFile(fileName, is);
			is.close();
		} catch (Exception e) {
			System.out.println("***Could not upload: " + filePath);
			e.printStackTrace();
		}
		
		System.out.println("***Finished uploading: " + filePath	);
	}
	
	private void uploadVideo(String videoRelativePath) throws InterruptedException {
		String pathRelativeToCore = "core/bk/" + videoRelativePath;
		publish(new CopyStatus(videoRelativePath + ".png", CHANGE_TEXT));
		failIfInterrupted();
		uploadFile(pathRelativeToCore + ".png");
		
		publish(new CopyStatus(videoRelativePath + ".mp4", CHANGE_TEXT));
		failIfInterrupted();
		uploadFile(pathRelativeToCore + ".mp4");
		
		publish(new CopyStatus(videoRelativePath + ".webm", CHANGE_TEXT));
		failIfInterrupted();
		uploadFile(pathRelativeToCore + ".webm");
	}

/*
	private void incrementVersions() {
		Iterator<String> iter = booksUpdated.iterator();
		while (iter.hasNext()) {
			String bookName = iter.next();
			
			CastEbook theEbook = new CastEbook(coreDir, bookName, false);
			theEbook.setupDom();
			DomBook domBook = theEbook.getDomBook();
			
			String description = domBook.getDescription();
			bookDescriptions.put(bookName, (description == null) ? "" : description);
			
			int nChapters = domBook.noOfChildren();
			for (int i=0 ; i<nChapters ; i++) {
				DomElement chapterElement = domBook.getChild(i);
				if (chapterElement instanceof DomChapter) {
					DomChapter domChapter = (DomChapter)chapterElement;
					int nChildren = domChapter.noOfChildren();
					if (nChildren > 0)
						for (int j=0 ; j<nChildren ; j++) {
							DomElement sectionElement = domChapter.getChild(j);
							if (sectionElement instanceof DomSection) {
								DomSection domSection = (DomSection)sectionElement;
								addSectionFolders(domSection.getDir(), domSection.getFilePrefix(), theEbook);
							}
						}
				}
			}
		}
			
		
		File coreDir = new File(castSourceDir, "core");
		File datesDir = new File(coreDir, "dates");
		File localVersionFile = new File(datesDir, kVersionFileName);
		
		try {
			Scanner fileScanner = new Scanner(localVersionFile);
			String versionAssignment = fileScanner.useDelimiter("\\Z").next();
			fileScanner.close();
			int castVersion[] = parseCastVersion(versionAssignment);
			castVersion[2] ++;
			
			versionAssignment = castVersionToString(castVersion, true);		//	assigns to localCastVersion
			
			PrintWriter out = new PrintWriter(localVersionFile);
			out.print(versionAssignment);
			out.flush();
			out.close();
			
			versionAssignment = castVersionToString(castVersion, false);		//	assigns to serverCastVersion
			String versionPath = "core/dates/" + kVersionFileName;
			uploadString(versionAssignment, versionPath);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
*/
}