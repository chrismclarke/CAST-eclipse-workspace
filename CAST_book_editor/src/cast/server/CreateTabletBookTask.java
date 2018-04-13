package cast.server;

import java.io.*;
import java.util.*;

import javax.swing.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cast.bookManager.*;
import cast.core.*;
import cast.utils.*;




public class CreateTabletBookTask extends CoreCopyTask {
	static final private int kCopyStages = 2;
	static final private int kCopyWithVideosStages = 3;
	
	static final private String[] kCoreCopyItems = {"index.html", "pageStyles.css",
																											"images", "structure", "terms"};
	
	static final private String[] kBookCopyItems = {"book_dataSets.html", "book_index.html",
																	"book_splash.html", "book_structure.js", "images"};
	
	private String coreBook;
	private boolean copyVideos;
	private File castDestDir, coreDestDir, booksDestDir, coreSourceDir, booksSourceDir;
	
	public CreateTabletBookTask(File castSourceDir, File castDestDir, String coreBook,
										boolean copyVideos, JLabel progressStage,
										CastProgressBar uploadStageProgress, CastProgressBar uploadItemProgress) {
		super(castSourceDir, null, null, progressStage, uploadStageProgress, uploadItemProgress);
		uploadStageProgress.initialise(copyVideos ? kCopyWithVideosStages : kCopyStages, "");
		
		this.castDestDir = castDestDir;
		this.coreBook = coreBook;
		this.copyVideos = copyVideos;
	}
	
	public Object doInBackground() throws InterruptedException {
		copyToDestination();
		setFinishedNormally();
		
		return null;
	}
	
	protected void copyToDestination() throws InterruptedException  {
		castDestDir.mkdir();
		
		File bookIndexFile = new File(castSourceDir, "book_" + coreBook + ".html");
		File destIndexFile = new File(castDestDir, "index.html");
		copyOneFile(bookIndexFile, destIndexFile);
		
		coreSourceDir = new File(castSourceDir, "core");
		coreDestDir = new File(castDestDir, "core");
		coreDestDir.mkdir();
		
		for (int i=0 ; i<kCoreCopyItems.length ; i++) {
			File coreSourceItem = new File(coreSourceDir, kCoreCopyItems[i]);
			File coreDestItem = new File(coreDestDir, kCoreCopyItems[i]);
			copyRecursive(coreSourceItem, coreDestItem);
		}
		
		booksSourceDir = new File(coreSourceDir, "bk");
		booksDestDir = new File(coreDestDir, "bk");
		booksDestDir.mkdir();
		
		writeReleaseInfo();
		
		copyBook(coreBook, coreSourceDir);
	}
	
	protected void writeReleaseInfo() {
		File versionInfoFile = new File(coreDestDir, kReleasInfoFileName);
		
		versionInfo = new StringsHash();
		String newServerUrl = " \"http://" + Options.kCastDownloadUrl + "\";";
		versionInfo.put(kServerUrlKey, newServerUrl);
		
		versionInfo.saveToFile(versionInfoFile);
	}
	
	
	private void copyBook(String coreBookName, File coreDir) throws InterruptedException {
		CastEbook theEbook = new CastEbook(coreDir, coreBookName, false);
		theEbook.setupDom();
		DomBook domBook = theEbook.getDomBook();
		
//		String description = domBook.getDescription();
		
		int nChapters = domBook.noOfChildren();
		publish(new CopyStatus("Copying section folders...", nChapters, RESET_BAR));
		failIfInterrupted();
		
		for (int i=0 ; i<nChapters ; i++) {
			DomElement chapterElement = domBook.getChild(i);
			if (chapterElement instanceof DomChapter) {
				DomChapter domChapter = (DomChapter)chapterElement;
				
				String chapterDirName = domChapter.getDir();
				String chapterFileName = domChapter.getFilePrefix();
				
				publish(new CopyStatus("Chapter " + chapterDirName + "...", INCREMENT_BAR));
				failIfInterrupted();
				
				File sourceChapterDir = findSourceDir(chapterDirName);
				File destChapterDir = setupDestDir(chapterDirName);
				File sourceChapterFile = new File(sourceChapterDir, chapterFileName + ".html");
				File destChapterFile = new File(destChapterDir, chapterFileName + ".html");
				copyOneFile(sourceChapterFile, destChapterFile);
				
				int nChildren = domChapter.noOfChildren();
				if (nChildren > 0)
					for (int j=0 ; j<nChildren ; j++) {
						DomElement sectionElement = domChapter.getChild(j);
						if (sectionElement instanceof DomSection) {
							DomSection domSection = (DomSection)sectionElement;
							
							String bookName = domSection.getDir();
							String sectionName = domSection.getFilePrefix();
							File sourceBookDir = findSourceDir(bookName);
							File destBookDir = setupDestDir(bookName);
							File sourceSecDir = new File(sourceBookDir, "sec");
							File destSecDir = new File(destBookDir, "sec");
							File sourceSecFile = new File(sourceSecDir, sectionName + ".js");
							File destSecFile = new File(destSecDir, sectionName + ".js");
							copyOneFile(sourceSecFile, destSecFile);
							
							copySectionFiles(domSection.getDir(), domSection.getFilePrefix(), theEbook);
						}
					}
			}
		}
		if (copyVideos)
			doCopyVideos(theEbook);
		
		CastEbook destEbook = new CastEbook(coreDestDir, coreBookName, false);
		VideosDownloadTask.rememberVideosDownloaded(destEbook, copyVideos);
	}
	
	private void copySectionFiles(String bookDir, String sectionFilePrefix, CastEbook theEbook) {
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
				
				String dir = pageElement.getAttribute("dir");
				String summaryDir = pageElement.getAttribute("summaryDir");
				String videoDir = pageElement.getAttribute("videoDir");
				boolean hasSummaryVersion = summaryDir != null && summaryDir.length() > 0;
				boolean hasVideoVersion = videoDir != null && videoDir.length() > 0;
				
				if (!hasSummaryVersion || !hasVideoVersion)
					copyItem(dir, pageElement.getAttribute("filePrefix"));
				copyItem(summaryDir, pageElement.getAttribute("summaryFilePrefix"));
				copyItem(videoDir, pageElement.getAttribute("videoFilePrefix"));
			}
		} catch(Exception e) {
			System.err.println("Error parsing section's xml file");
			e.printStackTrace();
		}
	}
	
	private void doCopyVideos(CastEbook theEbook) throws InterruptedException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new ErrorHandler() {
													public void warning(SAXParseException exception) throws SAXException {
													}
													public void error(SAXParseException exception) throws SAXException {
													}
													public void fatalError(SAXParseException exception) throws SAXException {
													}
												} );
			
			File videoXmlFile = theEbook.getXmlFile("videos");
			Document videoDomDocument = db.parse(videoXmlFile);
			
			int nSections = 0;
			
			NodeList bookList = videoDomDocument.getElementsByTagName("book");
			for (int i=0; i<bookList.getLength(); i++) {
				Node bookNode = bookList.item(i);
				if (bookNode.getNodeType() == Node.ELEMENT_NODE) {
					Element bookElement = (Element)bookNode;
//					String bookName = bookElement.getAttribute("name");
					NodeList sectionList = bookElement.getElementsByTagName("dir");
					for (int j=0 ; j<sectionList.getLength() ; j++) {
						Node sectionNode = sectionList.item(j);
						if (sectionNode.getNodeType() == Node.ELEMENT_NODE)
							nSections ++;
					}
				}
			}
			
			publish(new CopyStatus("Copying video folders...", nSections, RESET_BAR));
			failIfInterrupted();
			
			for (int i=0; i<bookList.getLength(); i++) {
				Node bookNode = bookList.item(i);
				if (bookNode.getNodeType() == Node.ELEMENT_NODE) {
					Element bookElement = (Element)bookNode;
					String bookName = bookElement.getAttribute("name");
					File sourceBookDir = findSourceDir("bk/" + bookName);
					File sourceVideosDir = new File(sourceBookDir, "videos");
					File destBookDir = setupDestDir("bk/" + bookName);
					File destVideosDir = new File(destBookDir, "videos");
					if (!destVideosDir.exists())
						destVideosDir.mkdir();
					
					NodeList sectionList = bookElement.getElementsByTagName("dir");
					for (int j=0 ; j<sectionList.getLength() ; j++) {
						Node sectionNode = sectionList.item(j);
						if (sectionNode.getNodeType() == Node.ELEMENT_NODE) {
							Element sectionElement = (Element)sectionNode;
							String sectionName = sectionElement.getAttribute("name");
							File sourceSectionDir = new File(sourceVideosDir, sectionName);
							File destSectionDir = new File(destVideosDir, sectionName);
							if (!destSectionDir.exists())
								destSectionDir.mkdir();
							
							publish(new CopyStatus("Section " + bookName + "/" + sectionName + "...", INCREMENT_BAR));
							failIfInterrupted();
							
							NodeList videoList = sectionElement.getElementsByTagName("video");
							for (int k=0 ; k<videoList.getLength() ; k++) {
								Node videoNode = videoList.item(k);
								Element videoElement = (Element)videoNode;
								String videoName = videoElement.getAttribute("name");
								
								File sourcePngFile = new File(sourceSectionDir, videoName + ".png");
								File destPngFile = new File(destSectionDir, videoName + ".png");
								copyOneFile(sourcePngFile, destPngFile);
								
								File sourceMp4File = new File(sourceSectionDir, videoName + ".mp4");
								File destMp4File = new File(destSectionDir, videoName + ".mp4");
								copyOneFile(sourceMp4File, destMp4File);
								
//								File sourceWebmFile = new File(sourceSectionDir, videoName + ".webm");
//								File destWebmFile = new File(destSectionDir, videoName + ".webm");
//								copyOneFile(sourceWebmFile, destWebmFile);
							}
						}
					}
				}
			}
		} catch(ParserConfigurationException e) {
			System.err.println("Error creating XML DocumentBuilder\n");
			e.printStackTrace();
		} catch(SAXException e) {
			System.err.println("Error parsing video XML file\n");
			e.printStackTrace();
		} catch(IOException e) {
			System.err.println("Error reading video XML file\n");
			e.printStackTrace();
		}
	}
								 
	private void copyItem(String dir, String filePrefix) {
		if (dir == null || dir.length() == 0)
			return;
		File sourceDir = findSourceDir(dir);
		File destDir = setupDestDir(dir);
		File sourceFile = new File(sourceDir, filePrefix + ".html");
		File destFile = new File(destDir, filePrefix + ".html");
		copyOneFile(sourceFile, destFile);
	}
	
	private File setupDestDir(String sectionName) {
		StringTokenizer st = new StringTokenizer(sectionName, "/");
		String languageName = st.nextToken();
		String sectionBookName = st.nextToken();
		
		if (languageName.equals("bk")) {
			File destBookDir = new File(booksDestDir, sectionBookName);
			if (!destBookDir.exists()) {
				destBookDir.mkdir();
				File sourceBookDir = new File(booksSourceDir, sectionBookName);
			
				for (int i=0 ; i<kBookCopyItems.length ; i++) {
					File bookSourceItem = new File(sourceBookDir, kBookCopyItems[i]);
					File bookDestItem = new File(destBookDir, kBookCopyItems[i]);
					copyRecursive(bookSourceItem, bookDestItem);
				}
				
				File secDestDir = new File(destBookDir, "sec");
				secDestDir.mkdir();
			}
			return destBookDir;
		}
		else {
			File sourceLanguageDir = new File(coreSourceDir, languageName);
			File destLanguageDir = new File(coreDestDir, languageName);
			if (!destLanguageDir.exists())
				destLanguageDir.mkdir();
			
			File sourceSectionDir = new File(sourceLanguageDir, sectionBookName);
			File destSectionDir = new File(destLanguageDir, sectionBookName);
			if (!destSectionDir.exists()) {
				destSectionDir.mkdir();
				
				File sourceImagesDir = new File(sourceSectionDir, "images");
				File destImagesDir = new File(destSectionDir, "images");
				if (sourceImagesDir.exists())
					copyRecursive(sourceImagesDir, destImagesDir);
			}
			return destSectionDir;
		}
	}
	
	private File findSourceDir(String sectionName) {
		StringTokenizer st = new StringTokenizer(sectionName, "/");
		String languageName = st.nextToken();
		String sectionBookName = st.nextToken();
		if (languageName.equals("bk"))
			return new File(booksSourceDir, sectionBookName);
		else {
			File sourceLanguageDir = new File(coreSourceDir, languageName);
			return new File(sourceLanguageDir, sectionBookName);
		}
	}
}