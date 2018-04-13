package cast.server;

import java.io.*;
import java.net.*;

import cast.bookManager.*;
import cast.core.*;



public class LocalUpdateTask extends CoreDownloadTask {
	static final private int kNoOfStages = 6;
	
	static final private String kCastManagerName = "Start_CAST.jar";
	
	private File castDir;
	private LocalUpdateFrame updateFrame;
	private AllDates serverDates, localDates;
	
	public LocalUpdateTask(final File castDir, LocalUpdateFrame updateFrame, AllDates localDates, AllDates serverDates) {
		this.castDir = castDir;
		this.updateFrame = updateFrame;
		this.localDates = localDates;
		this.serverDates = serverDates;
	}
	
	public Object doInBackground() throws Exception {
		publish(new DownloadStatus(kNoOfStages));
		publish(new DownloadStatus("Checking server files...", 1));
		failIfInterrupted();
		
		File coreDir = new File(castDir, "core");
		File datesDir = new File(coreDir, "dates");
		
		updateCoreFiles(datesDir);
		
		updateCollectionFiles(datesDir);
		
		updateSectionFiles(datesDir);
		
		updateBookFiles(datesDir);
		
		updateVideoFiles(datesDir);
		
		try {
			StringsHash serverReleaseInfo = new StringsHash(new URL("http://" + Options.kCastDownloadUrl + "/core/" + CoreCopyTask.kReleasInfoFileName));
			File localReleaseInfoFile = new File(coreDir, CoreCopyTask.kReleasInfoFileName);
			StringsHash localReleaseInfo = new StringsHash(localReleaseInfoFile);
			
			localReleaseInfo.updateAllEntries(serverReleaseInfo);
			localReleaseInfo.saveToFile(localReleaseInfoFile);
		} catch (MalformedURLException e) {
		}
		
		return null;
	}
	
	private void updateCoreFiles(File datesDir) throws InterruptedException {
		String[] coreUpdates = localDates.coreNeedingUpdate(serverDates);
		publish(new DownloadStatus("Core files...", coreUpdates.length));
		failIfInterrupted();
		printItemList(coreUpdates, "Core files:");
		
		for (int i=0 ; i<coreUpdates.length ; i++) {
			String itemName = coreUpdates[i];
			publish(new DownloadStatus(itemName, null));
			failIfInterrupted();
			
			File f = getFile(itemName, castDir);
			if (itemName.endsWith("/"))
				downloadDatedFolder(f, itemName, itemName);
			else if (itemName.equals(kCastManagerName)) {		//	we cannot simply overwrite the running program
				File tempNewFile = new File(castDir, ".tempNew.tmp");
				downloadFile(tempNewFile, urlPath(itemName));		//	downloaded file is initially hidden and called ".new_CAST_manager.jar"
				File tempOldFile = new File(castDir, ".tempOld.tmp");
				f.renameTo(tempOldFile);
				tempOldFile.deleteOnExit();
				tempNewFile.renameTo(f);
			}
			else if (itemName.equals("core/index.html"))
				downloadIndexFile(f, urlPath(itemName));
			else
				downloadFile(f, urlPath(itemName));
			
			localDates.updateCoreEntry(itemName, serverDates, datesDir);
		}
	}
	
	private void updateCollectionFiles(File datesDir) throws InterruptedException {
		String[] collectionUpdates = localDates.collectionNeedingUpdate(serverDates);
		publish(new DownloadStatus("Collection files...", collectionUpdates.length));
		failIfInterrupted();
		printItemList(collectionUpdates, "Collection files:");
		
		for (int i=0 ; i<collectionUpdates.length ; i++) {
			String itemName = collectionUpdates[i];
			publish(new DownloadStatus(itemName, null));
			failIfInterrupted();
			
			File f = getFile(itemName, castDir);
			downloadFile(f, urlPath(itemName));
			
			localDates.updateCollectionEntry(itemName, serverDates, datesDir);
		}
	}
	
	private void updateSectionFiles(File datesDir) throws InterruptedException {
		String[] sectionUpdates = localDates.sectionsNeedingUpdate(serverDates);
		publish(new DownloadStatus("Section files...", sectionUpdates.length));
		failIfInterrupted();
		printItemList(sectionUpdates, "Sections:");
		
		File coreDir = new File(castDir, "core");
		
		for (int i=0 ; i<sectionUpdates.length ; i++) {
			String itemName = sectionUpdates[i];
			publish(new DownloadStatus(itemName, null));
			failIfInterrupted();
			
			File sectionDir = getFile(itemName, coreDir);
			downloadDatedFolder(sectionDir, "core/" + itemName + "/", itemName);
			
			localDates.updateSectionEntry(itemName, serverDates, datesDir);
		}
	}
	
	private void updateBookFiles(File datesDir) throws InterruptedException {
		String[] bookUpdates = localDates.booksNeedingUpdate(serverDates);
		publish(new DownloadStatus("Book files...", bookUpdates.length));
		failIfInterrupted();
		printItemList(bookUpdates, "Books:");
		
		File coreDir = new File(castDir, "core");
		
		for (int i=0 ; i<bookUpdates.length ; i++) {
			String bookName = bookUpdates[i];
			publish(new DownloadStatus(bookName, null));
			failIfInterrupted();
			
			File booksDir = new File(coreDir, "bk");
			File bookDir = getFile(bookName, booksDir);
			boolean hadLocalVideos = hasDownloadedVideos(bookDir);
			
			boolean changedStructure = downloadFolderAndCheck(bookDir, "core/bk/" + bookName + "/", bookName,
																																	CoreDownloadTask.kBookStructureFile);
			
			if (changedStructure) {
				localDates.findNewVideosInBook(coreDir, bookName);
				rememberVideosDownloaded(bookDir, hadLocalVideos);
			}
			
			localDates.updateBookEntry(bookName, serverDates, datesDir);
			
			CastEbook theEbook = new CastEbook(coreDir, bookName, false);
			updateInstalledBook(theEbook, true);		//	updates entry in "installedBooks.js"
		}
	}
	
	private void updateVideoFiles(File datesDir) throws InterruptedException {
		String[] videoUpdates = localDates.videosNeedingUpdate(serverDates);
		publish(new DownloadStatus("Videos...", videoUpdates.length));
		failIfInterrupted();
		printItemList(videoUpdates, "Videos:");
		
		File coreDir = new File(castDir, "core");
		File booksDir = new File(coreDir, "bk");
		
		for (int i=0 ; i<videoUpdates.length ; i++) {
			String videoPath = videoUpdates[i];
			publish(new DownloadStatus(videoPath, null));
			failIfInterrupted();
			
			int lastSlashIndex = videoPath.lastIndexOf("/");
			String sectionPath = videoPath.substring(0, lastSlashIndex);
			String videoName = videoPath.substring(lastSlashIndex + 1);
			
			File sectionDir = getFile(sectionPath, booksDir);
			checkDirExists(sectionDir);
			downloadOneVideo(sectionDir, videoName, videoPath);
			
			localDates.updateVideoEntry(videoName, serverDates, datesDir);
		}
	}
	
//------------------------------------------------------------------------------------
	
	private String urlPath(String pathFromCastDir) {
		return "http://" + Options.kCastDownloadUrl + "/" + pathFromCastDir;
	}
	
	private void downloadDatedFolder(File dir, String pathFromCastDir, String pathToDisplay) throws InterruptedException {
																			//	pathFromCastDir ends in "/"
																			//	pathToDisplay is what shows in status bar (and also ends in "/")
		downloadFolderAndCheck(dir, pathFromCastDir, pathToDisplay, null);
	}
	
	private boolean downloadFolderAndCheck(File dir, String pathFromCastDir, String pathToDisplay,
																															String itemToCheck) throws InterruptedException {
		checkDirExists(dir);
		File folderDatesFile = new File(dir, DatesHash.kDateStampFileName);
		DatesHash localFolderDates = new DatesHash(folderDatesFile);
		DatesHash serverFolderDates = new DatesHash(urlPath(pathFromCastDir + "/" + DatesHash.kDateStampFileName));
		String[] updateItems = DatesHash.itemsNeedingUpdate(localFolderDates, serverFolderDates);
		boolean itemChanged = false;
		
		for (int i=0 ; i<updateItems.length ; i++) {
			String itemName = updateItems[i];
			if (itemName.equals(itemToCheck))
				itemChanged = true;
			publish(new DownloadStatus(pathToDisplay + itemName, ""));
			failIfInterrupted();
			File f = getFile(itemName, dir);
			downloadFile(f, urlPath(pathFromCastDir + itemName));
		}
		localFolderDates.updateAllEntries(serverFolderDates);
		localFolderDates.saveToFile(folderDatesFile);
		return itemChanged;
	}
	
	@SuppressWarnings("unused")
	private void updateBook(String bookName) {
									//	finds "localVideos" state before update and restores it after
									//	if there are localVideos, add video entries for book with call to
									//			localDates.findNewVideosInBook(coreDir, bookName)
	}
	
	private void printItemList(String[] files, String title) {
		System.out.println(title);
		for (int i=0 ; i<files.length ; i++)
			System.out.println("   " + files[i]);
	}
	

	public void done() {
		if (isCancelled())
			updateFrame.setCancelled();
		else
			updateFrame.setFinished();
	}
	
	
	protected void process(final java.util.List<DownloadStatus> chunks) {
		for (final DownloadStatus status : chunks) {
			if (status.section == null && status.item ==  null)
				updateFrame.updateForStart(status.noOfSections);
			else if (status.item == null)
				updateFrame.updateForNewStage(status.section, status.noOfItems);
			else if (status.fileType == null)
				updateFrame.updateForNewFile(status.item);
			else																								//	status.fileType = "" or any other String
				updateFrame.updateForNewItem(status.item);
		}
	}
}