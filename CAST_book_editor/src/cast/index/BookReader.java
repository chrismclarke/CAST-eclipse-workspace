package cast.index;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cast.bookManager.*;
import cast.utils.*;


public class BookReader {
	private CastEbook castEbook;
	
	private String longBookName;
	private String tocBookName;
	private String versionImage;
	private String logoGif;
	private String language;
	private boolean isLecturingVersion, isModule, hasSummaries, hasVideos;
	private String fullPdfUrl, summaryPdfUrl;
	
	private BookTree bookTree = null;
	private File bookXmlFile;
	
	private boolean doReadPages = true;
	
	public BookReader(CastEbook castEbook) {
		this.castEbook = castEbook;
		
		bookXmlFile = castEbook.getBookXmlFile();
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
			Document dom = db.parse(bookXmlFile);
			
			Element bookElement = dom.getDocumentElement();
			
			longBookName = bookElement.getAttribute("name");
			tocBookName = bookElement.getAttribute("tocTitle");
			versionImage = bookElement.getAttribute("versionImage");		//	On right of banner under arrows (relative to structure folder)
			logoGif = bookElement.getAttribute("logoGif");							//	logo under ToC (if it exists)
			if (logoGif.length() == 0)
				logoGif = null;
			language = bookElement.getAttribute("language");						//	null=English -- override with zh(Chinese), de(German), etc
			if (language.length() == 0)
				language = null;
			
			String typeString = bookElement.getAttribute("type");
			isLecturingVersion = typeString.startsWith(DomBook.TYPE_LECTURE);
			isModule = typeString.startsWith(DomBook.TYPE_MODULE);
			
			hasSummaries = typeString.equals(DomBook.TYPE_BOOK_AND_SUMMARIES) || DomBook.hasTypeExtra(typeString, 's');
			hasVideos = DomBook.hasTypeExtra(typeString, 'v');
			
			fullPdfUrl = bookElement.getAttribute("fullPdfUrl");							//	folders containing PDFs for printing
			if (fullPdfUrl.length() == 0)
				fullPdfUrl = null;
			summaryPdfUrl = bookElement.getAttribute("summaryPdfUrl");
			if (summaryPdfUrl.length() == 0)
				summaryPdfUrl = null;
		} catch(Exception e) {
			System.err.println("Error opening book (" + castEbook.getShortBookName() + ")\n" + e);
		}
	}
	
	public void setDoReadPages(boolean doReadPages) {
		this.doReadPages = doReadPages;						//		BookEditor only needs the chapters and sections
	}
	
	private String getTitleTag(String dirPath, String filePrefix) {		//	dirPath is relative to core folder
		File theFile = castEbook.getPageHtmlFile(dirPath, filePrefix);
		String fileString = HtmlHelper.getFileAsString(theFile);
		return HtmlHelper.getTagInFile(fileString, "title");
	}
	
	public void setupBook() {
		if (bookTree == null) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(true);
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(bookXmlFile);
				
				startBook(castEbook);
				
				Element bookElement = dom.getDocumentElement();
				NodeList nl = bookElement.getChildNodes();
				for (int i=0 ; i<nl.getLength() ; i++)
					if (nl.item(i) instanceof Element) {
						Element e = (Element)nl.item(i);
						if (e.getTagName().equals("part")) {
							String partName = e.getAttribute("name");
							readPart(partName);
						}
						else if (e.getTagName().equals("chapter")) {
							Element chapterElement = e;
							String chapterDir = chapterElement.getAttribute("dir");
							String chapterFile = chapterElement.getAttribute("file");
							readChapter(chapterDir, chapterFile);
							
							
							NodeList snl = chapterElement.getChildNodes();
							for (int j=0 ; j<snl.getLength() ; j++)
								if (snl.item(j) instanceof Element) {
									Element se = (Element)snl.item(j);
									String dir = se.getAttribute("dir");
									String name = se.getAttribute("file");
									if (se.getTagName().equals("page")) {
//										System.out.println("dir = " + dir + ", name = " + name);
										String title = getTitleTag(dir, name);
										bookTree.addItem(BookTree.SECTION, dir, name, title);
									}
									else
										readSection(dir, name);
								}
						}
					}
			} catch(Exception e) {
				System.err.println("Error setting up book: " + e);
				e.printStackTrace();
			}
		}
	}
	
	public BookTree getBookTree() {
		return bookTree;
	}
	
	public String getLongBookName() {
		return longBookName;
	}
	
	protected String getTOCTitle() {
		return tocBookName;
	}
	
	protected String getVersionImage() {
		return versionImage;
	}
	
	protected String getLogoGif() {
		return logoGif;
	}
	
	protected String getSummaryPdfUrl() {
		return summaryPdfUrl;
	}
	
	protected boolean isLecturingVersion() {
		return isLecturingVersion;
	}
	
	protected boolean isModule() {
		return isModule;
	}
	
	protected boolean hasSummaries() {
		return hasSummaries;
	}
	
	protected boolean hasVideos() {
		return hasVideos;
	}
	
	protected String getLanguage() {
		return language;
	}

//**************************************************************
	
	private void startBook(CastEbook castEbook) {
		String dirPath = castEbook.getHomeDirName();
		String filePrefix = "book_splash";
		String name = getTitleTag(dirPath, filePrefix);
		bookTree = new BookTree(dirPath, filePrefix, name);
	}
	
	private void readPart(String partName) {
		bookTree.addItem(BookTree.CHAPTER, null, null, partName);
	}
	
	private void readChapter(String dir, String filePrefix) {
		String name = getTitleTag(dir, filePrefix);
		bookTree.addItem(BookTree.CHAPTER, dir, filePrefix, name);
	}
	
	private void readSection(String sectionDir, String sectionFilePrefix) {
		File sectionXmlFile = castEbook.getXmlFile(sectionDir, sectionFilePrefix);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(sectionXmlFile);
			
			Element sectionElement = dom.getDocumentElement();
			
			String shortSectionName = sectionElement.getAttribute("shortName");
			if (shortSectionName == null || shortSectionName.length() == 0)
				shortSectionName = sectionElement.getAttribute("name");
			bookTree.addItem(BookTree.SECTION, sectionDir, sectionFilePrefix, shortSectionName);
			
			if (doReadPages) {
				NodeList nl = sectionElement.getElementsByTagName("page");
				for (int i=0 ; i<nl.getLength() ; i++) {
					Element pageElement = (Element)nl.item(i);
					String pageDir = pageElement.getAttribute("dir");
					String pageFilePrefix = pageElement.getAttribute("filePrefix");
					
					String summaryDir = null;
					String summaryFilePrefix = null;
					if (hasSummaries) {
						summaryDir = pageElement.getAttribute("summaryDir");
						if (summaryDir.length() == 0)
							summaryDir = null;
						summaryFilePrefix = pageElement.getAttribute("summaryFilePrefix");
						if (summaryFilePrefix.length() == 0)
							summaryFilePrefix = null;
					}
					
					String videoDir = null;
					String videoFilePrefix = null;
					if (hasVideos) {
						videoDir = pageElement.getAttribute("videoDir");
						if (videoDir.length() == 0)
							videoDir = null;
						videoFilePrefix = pageElement.getAttribute("videoFilePrefix");
						if (videoFilePrefix.length() == 0)
							videoFilePrefix = null;
					}
					
					String pageName = pageElement.getAttribute("nameOverride");
					if (pageName == null || pageName.length() == 0)
						pageName = HtmlHelper.getTagInFile(pageDir, pageFilePrefix, castEbook, "title");
					
					String note = pageElement.getAttribute("note");
					if (note != null && note.length() > 0 && note.indexOf("#") >= 0) {
						int hashIndex = note.indexOf("#");
						pageName += "#?# " + note.substring(hashIndex + 1);		//	adds shortened form of note to page name for banner
						note = note.substring(0, hashIndex);
					}
					
					String description = XmlHelper.getTagInterior(pageElement);
					description = XmlHelper.decodeHtml(description, XmlHelper.WITHOUT_PARAGRAPHS);
					if (note != null && note.length() > 0)
						description = note + "#?#" + description;
					
					readPage(pageDir, pageFilePrefix, pageName, description, summaryDir, summaryFilePrefix, videoDir, videoFilePrefix);
				}
			}
		} catch(Exception e) {
			System.err.println("Error reading section (" + sectionDir + "," + sectionFilePrefix + "): " + e);
		}
	}
	
	private void readPage(String pageDir, String pageFilePrefix, String pageName, String description,
																	String summaryDir, String summaryFilePrefix, String videoDir, String videoFilePrefix) {
		bookTree.addItem(BookTree.PAGE, pageDir, pageFilePrefix, pageName, description, summaryDir, summaryFilePrefix,
																																													videoDir, videoFilePrefix);
	}
}
