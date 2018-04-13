package cast.server;

import java.io.*;
import java.util.*;

import javax.swing.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import cast.bookManager.*;
import cast.utils.*;
import cast.core.*;


	
class CopyStatus {
	static public int currentIndex;
	
	String message;
	int noOfItems;
	String item = null;
	int changeType;
	
	CopyStatus(String message, int noOfItems, int changeType) {
		this.message = message;
		this.noOfItems = noOfItems;
		this.changeType = changeType;
		currentIndex = -1;
	}
	
	CopyStatus(String item, int changeType) {
		this.item = item;
		this.changeType = changeType;
		if (changeType != CoreCopyTask.CHANGE_TEXT)
			currentIndex ++;
	}
}


abstract public class CoreCopyTask extends SwingWorker<Object, CopyStatus> {

	static final public String kCoreDatesFileName = "core_dates.text";
	static final public String kBookDescriptionsFileName = "book_descriptions.text";
	static final public String kBookDatesFileName = "book_dates.text";
	static final public String kSectionDatesFileName = "section_dates.text";
	static final public String kVideoDatesFileName = "video_dates.text";					//	an entry for each book with videos
	static final public String kCollectionDatesPrefix = "collection_dates_";
	
	static final public String kReleasInfoFileName = "releaseInfo.js";
	static final public String kServerReleasInfoFileName = "serverReleaseInfo.js";
	static final public String kInstalledBooksFileName = "installedBooks.js";
	
	static final protected String[] kCoreFolders = {"init", "core/images", "core/java", "core/exercises",
																																							"core/structure", "core/terms"};
	static final protected String[] kCoreFiles = {"Start_CAST.jar", "core/index.html", "core/pageStyles.css",
																															"core/sources.data"};
	
	static final protected int kCoreCopyStages = 4;
	
	static final protected String kInstallerUrlKey = "var castDownloadUrl ";
	static final protected String kServerUrlKey = "var castWebServerUrl ";
	
	static protected void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted while copying files");
		}
	}
	
	static final public int RESET_BAR = 0;
	static final public int INCREMENT_BAR = 1;
	static final public int CHANGE_TEXT = 2;
	
	protected File castSourceDir;
	protected String collection;
	protected HashSet<String> bookDirs;				//		short book names
	private JLabel progressStageLabel;
	private CastProgressBar copyStageProgress, copyItemProgress;
	
	private boolean finishedNormally = false;
//	private File currentFile = null;
	
	protected DatesHash bookDates, sectionDates, coreDates, videoDates;
	protected HashSet<String> sectionNames;						//		section names, including language prefix
	protected StringsHash bookDescriptions, versionInfo;
	protected Hashtable<String, DatesHash> collectionDates;
	
	private int progressStage;
	
	public CoreCopyTask(File castSourceDir, String collection, HashSet<String> bookDirs,
																						JLabel progressStageLabel, CastProgressBar copyStageProgress,
																						CastProgressBar copyItemProgress) {
		this.castSourceDir = castSourceDir;
		this.collection = collection;
		this.progressStageLabel = progressStageLabel;
		this.copyStageProgress = copyStageProgress;
		this.copyItemProgress = copyItemProgress;
		this.bookDirs = bookDirs;
		
		progressStage = -1;
	}
	
	protected void process(final java.util.List<CopyStatus> chunks) {
		for (final CopyStatus status : chunks) {
			switch (status.changeType) {
				case RESET_BAR:
					progressStage ++;
					copyStageProgress.setValue(progressStage, status.message);
					copyItemProgress.initialise(status.noOfItems, "");
					break;
				case INCREMENT_BAR:
					copyItemProgress.setValue(CopyStatus.currentIndex, status.item);
					break;
				case CHANGE_TEXT:
					copyItemProgress.setValue(status.item);
			}
		}
	}

	public void done() {
		copyItemProgress.clear();
		if (finishedNormally) {
			progressStage ++;
			copyStageProgress.setDone("Done");
		}
		progressStageLabel.setText(finishedNormally ? "Finished" : isCancelled() ? "Cancelled" : "Error");
	}
	
	protected void setFinishedNormally() {
		finishedNormally = true;
	}
	
//------------------------------------------------------------------------
	
	public Object doInBackground() throws InterruptedException {
		File coreSourceDir = new File(castSourceDir, "core");
		File datesSourceDir = new File(coreSourceDir, "dates");
		File collectionsSourceDir = new File(coreSourceDir, "collections");
		
		findAllBookFolders(coreSourceDir, datesSourceDir);
		setupBookFolders(coreSourceDir, datesSourceDir);
		setupSectionFolders(coreSourceDir, datesSourceDir);
		setupCoreFiles();
		setupCollectionFiles(collectionsSourceDir);
		setupReleaseInfo(collectionsSourceDir);
		
		copyToDestination();
		
		setFinishedNormally();
		
		return null;
	}
	
	
	private void findAllBookFolders(File coreSourceDir, File datesSourceDir) throws InterruptedException {
		publish(new CopyStatus("Finding book folders...", 1, RESET_BAR));
		failIfInterrupted();
		
		sectionNames = new HashSet<String>();
		bookDescriptions = new StringsHash();
		
		Iterator<String> iter = bookDirs.iterator();
		while (iter.hasNext()) {
			String bookDir = iter.next();
			publish(new CopyStatus(bookDir, INCREMENT_BAR));
			failIfInterrupted();
			findBookFolders(bookDir, coreSourceDir, bookDescriptions);
		}
	}
	
	private void findBookFolders(String bookName, File coreDir, StringsHash bookDescriptions) {
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
	
	private void addSectionFolders(String bookDir, String sectionFilePrefix, CastEbook theEbook) {
		File xmlFile = theEbook.getXmlFile(bookDir, sectionFilePrefix);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document domForSection = db.parse(xmlFile);
			
			Element sectionElement = domForSection.getDocumentElement();
			NodeList pageNodes = sectionElement.getElementsByTagName("page");
			
			int nNodes = pageNodes.getLength();
			for (int i=0 ; i<nNodes ; i++) {
				Element pageElement = (Element)pageNodes.item(i);
				addFolderName(pageElement, "dir");
				addFolderName(pageElement, "summaryDir");
				addFolderName(pageElement, "videoDir");
			}
		} catch(Exception e) {
			System.err.println("Error parsing section's xml file");
			e.printStackTrace();
		}
	}
	
	private void addFolderName(Element pageElement, String attribute) {
		String dirName = pageElement.getAttribute(attribute);
		if (dirName != null && dirName.length() > 0) {
			if (dirName.startsWith("bk/"))
				bookDirs.add(dirName.substring(3));
			else
				sectionNames.add(dirName);
		}
	}
	
//-------------------------------------------------------------
	
	private void setupBookFolders(File coreSourceDir, File datesSourceDir) throws InterruptedException {
		publish(new CopyStatus("Setting up date files for books...", bookDirs.size(), RESET_BAR));
		failIfInterrupted();
		
		File booksDir = new File(coreSourceDir, "bk");
		bookDates = new DatesHash();
		videoDates = new DatesHash();
		Iterator<String> iter = bookDirs.iterator();
		while (iter.hasNext()) {
			String bookName = iter.next();
			publish(new CopyStatus(bookName, INCREMENT_BAR));
			failIfInterrupted();
			
			File bookDir = new File(booksDir, bookName);
			DatesHash datesInBook = new DatesHash(bookDir);
			datesInBook.saveToFile(DatesHash.getDateStampFile(bookDir));
			
			long lastMod = datesInBook.latestChange();
			bookDates.put(bookName, lastMod);
			
			findBookVideos(bookDir);
		}
		publish(new CopyStatus("finished", INCREMENT_BAR));
		failIfInterrupted();
	}
	
	private void findBookVideos(File bookDir) {
		File videoDir = new File(bookDir, "videos");
		if (videoDir.exists()) {
			File sectionDirs[] = videoDir.listFiles(new FilenameFilter() {
																				public boolean accept(File dir, String name) {
																					return new File(dir, name).isDirectory();
																				}
																			});
			for (int i=0 ; i<sectionDirs.length ; i++) {
				File sectionDir = sectionDirs[i];
				File[] videos = sectionDir.listFiles(new FilenameFilter() {
																				public boolean accept(File dir, String name) {
																					return name.endsWith(".mp4");
																				}
																			});
				for (int k=0 ; k<videos.length ; k++) {
					String videoName = videos[k].getName();
					videoName = videoName.substring(0, videoName.length() - 4);		//	to delete the ".mp4"
					long lastModifiedDate = Math.max(videos[k].lastModified(), new File(sectionDir, videoName + ".png").lastModified());
					
					String videoPath = bookDir.getName() + "/videos/" + sectionDir.getName() + "/" + videoName;
					videoDates.put(videoPath, Long.valueOf(lastModifiedDate));
				}
			}
		}
	}
	
	private void setupSectionFolders(File coreSourceDir, File datesSourceDir) throws InterruptedException {
		publish(new CopyStatus("Setting up date files for sections...", sectionNames.size(), RESET_BAR));
		failIfInterrupted();
		
		sectionDates = new DatesHash();
		Iterator<String> iter = sectionNames.iterator();
		while (iter.hasNext()) {
			String sectionPath = iter.next();
			publish(new CopyStatus(sectionPath, INCREMENT_BAR));
			failIfInterrupted();
			
			int slashIndex = sectionPath.indexOf("/");
			String languageDirName = sectionPath.substring(0, slashIndex);
			String sectionName = sectionPath.substring(slashIndex + 1);
			File sectionDir = new File(new File(coreSourceDir, languageDirName), sectionName);
			DatesHash datesInSection = new DatesHash(sectionDir);
			datesInSection.saveToFile(DatesHash.getDateStampFile(sectionDir));
			
			long lastMod = datesInSection.latestChange();
			sectionDates.put(sectionPath, lastMod);	
		}
	}
	
	
	private void setupCoreFiles() {
		coreDates = new DatesHash();
		for (int i=0 ; i<kCoreFolders.length ; i++) {
			findFolderDates(kCoreFolders[i], coreDates);
		}
		
		for (int i=0 ; i<kCoreFiles.length ; i++) {
			findFileDate(kCoreFiles[i], coreDates);
		}
	}
	
	protected void setupReleaseInfo(File collectionsSourceDir) {
		File coreDir = new File(castSourceDir, "core");
		File versionInfoFile = new File(coreDir, kReleasInfoFileName);
		
		versionInfo = new StringsHash();
		String newInstallerUrl = " \"http://" + Options.kCastInstallerUrl + "\";";
		String newServerUrl = " \"http://" + Options.kCastDownloadUrl + "\";";
		versionInfo.put(kInstallerUrlKey, newInstallerUrl);
		versionInfo.put(kServerUrlKey, newServerUrl);
		
		long latestCoreChange = coreDates.latestChange();
		HashSet<String> coreBooks = new HashSet<String>();
		versionInfo.initJsCollectionEntries();
		
		Enumeration<String> e = collectionDates.keys();
		while (e.hasMoreElements()) {
			String collectionName = e.nextElement();
			long latestCollectionChange = Math.max(latestCoreChange, collectionDates.get(collectionName).latestChange());
			
			File collectionsDir = new File(coreDir, "collections");
			File bookListFile = new File(collectionsDir, collectionName + "_books.text");
			if (bookListFile.exists())
				try {
					BufferedReader br = new BufferedReader(new FileReader(bookListFile));
					String bookName;
					while ((bookName = br.readLine()) != null) {
						coreBooks.add(bookName);
						latestCollectionChange = Math.max(latestCollectionChange, bookDates.getDate(bookName));
					}
					br.close();
				} catch (FileNotFoundException ex) {
					System.err.println("Cannot find file with core books");
					ex.printStackTrace();
				} catch (IOException ex) {
					System.err.println("Cannot read file with core books");
					ex.printStackTrace();
				}
			
			String languageCode = SetupDatesTask.getLanguageCode(collectionName);
			if (languageCode == null)
				languageCode = "en";
			File sectionsDir = new File(coreDir, languageCode);
			File[] allSectionDirs = sectionsDir.listFiles();
			for (int i=0 ; i<allSectionDirs.length ; i++)
				if (allSectionDirs[i].isDirectory()) {
					String sectionName = languageCode + "/" + allSectionDirs[i].getName();
					long sectionChangeDate = sectionDates.getDate(sectionName);
					latestCollectionChange = Math.max(latestCollectionChange, sectionChangeDate);
				}
			
			versionInfo.addJsCollectionDate(collectionName, " " + String.valueOf(latestCollectionChange) + ";");
		}
		
		if (collection.equals("all")) {					//	we don't need information about individual books when copying a collection
			versionInfo.initJsBookEntries();
			File booksDir = new File(coreDir, "bk");
			File[] bookDirs = booksDir.listFiles();
			
			for (int i=0 ; i<bookDirs.length ; i++) {
				File bookDir = bookDirs[i];
				String bookName = bookDir.getName();
				if (bookDir.isDirectory() && !coreBooks.contains(bookName))
					versionInfo.addJsBookDate(bookName, " " + String.valueOf(bookDates.getDate(bookName)) + ";");
			}
			
			versionInfo.saveToFile(versionInfoFile);		//	only save when uploading everything to server
		}
	}
	
	
	private void setupCollectionFiles(File collectionsSourceDir) {
		collectionDates = new Hashtable<String, DatesHash>();
		if (collection == "all") {
			File[] sourceFiles = collectionsSourceDir.listFiles(new FilenameFilter() {
																		public boolean accept(File dir, String name) {
																			return name.endsWith("_books.text");
																		}
																	});
			for (int i=0 ; i<sourceFiles.length ; i++) {
				String fileName = sourceFiles[i].getName();
				String thisCollection = fileName.substring(0, fileName.length() - 11);
				DatesHash thisCollectionDates = new DatesHash(castSourceDir, thisCollection);
				if (thisCollectionDates.size() > 0)
					collectionDates.put(thisCollection, thisCollectionDates);
			}
		}
		else
			collectionDates.put(collection, new DatesHash(castSourceDir, collection));
	}
	
	private void findFileDate(String fileName, DatesHash otherDates) {
		File f = castSourceDir;
		StringTokenizer st = new StringTokenizer(fileName, "/");
		while (st.hasMoreTokens())
			f = new File(f, st.nextToken());
		if (f.exists())
			otherDates.put(fileName, f.lastModified());
	}
	
	private void findFolderDates(String folderName, DatesHash otherDates) {
		File f = castSourceDir;
		StringTokenizer st = new StringTokenizer(folderName, "/");
		while (st.hasMoreTokens())
			f = new File(f, st.nextToken());
		DatesHash folderDates = new DatesHash(f);
		folderDates.saveToFile(DatesHash.getDateStampFile(f));
		
		otherDates.put(folderName + "/", folderDates.latestChange());
	}
	
//---------------------------------------------------------------------------------
	
	abstract protected void copyToDestination() throws InterruptedException;
	
	//---------------------------------------------------------------------------------
	
	protected void copyOneFile(File source, File dest) {
		try{
			InputStream in = new FileInputStream(source);
			OutputStream out = new FileOutputStream(dest);
			
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			in.close();
			out.close();
		}
		catch(FileNotFoundException e) {
			System.err.println("Could not find file to copy: " + source.getPath());
			e.printStackTrace();
		}
		catch(IOException e) {
			System.err.println("Error copying file: " + source.getPath());
			e.printStackTrace();
		}
	}
	
	protected void copyRecursive(File source, File dest) {
		if (source.isFile())
			copyOneFile(source, dest);
		else {
			if (!source.exists())
				return;
			if (!dest.exists())
				dest.mkdir();
			
			File[] sourceFiles = source.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return !name.startsWith(".");		//	not hidden files
				}
			});
			for (int k=0 ; k<sourceFiles.length ; k++) {
				File sourceFile = sourceFiles[k];
				File destFile = new File(dest, sourceFile.getName());
				copyRecursive(sourceFile, destFile);
			}
		}
	}
	
}