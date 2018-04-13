package ebookStructure;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class CastEbook {
//	static final private String kPublicBooks[] = {"general", "generalx", "biometric", "biometricx", "business", "businessx",
//												"agExper", "assessment", "exercises", "indExper", "official", "presentation", "regn",
//												"simulation", "slug", "mathStats"};
	
	static final private String kServerNamePattern = "var castWebServerUrl = \"(.*?)\"";
	
	private File coreDir;
	private String shortBookName;
	private String serverUrl;
	
	private Document bookDomDocument = null;
	private DomBook bookDom = null;
	@SuppressWarnings("unused")
	private boolean domChanged;
	
	public CastEbook(File coreDir, String shortBookName) {
		this.coreDir = coreDir;
		this.shortBookName = shortBookName;
		setupDom();
		findServerUrl();
	}
	
	public boolean isEnglish() {
		String language = bookDom.getLanguage();
		return language == null || language.equals("en");
	}
	
	public void setupDom() {
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
			File bookXmlFile = getBookXmlFile();
			bookDomDocument = db.parse(bookXmlFile);
			
			bookDom = new DomBook(bookDomDocument.getDocumentElement(), this);
			
			domChanged = false;
		} catch(Exception e) {
			System.err.println("Error opening book (" + getShortBookName() + ")\n" + e);
		}
	}
	
	private void findServerUrl() {
													//	For historical reasons, this is stored in a Javascript file, not XML
		File coreDir = getCoreDir();
		File releaseInfoFile = new File(coreDir, "releaseInfo.js");
		String releaseInfo = HtmlHelper.getFileAsString(releaseInfoFile);
		
		Pattern serverNamePattern = Pattern.compile(kServerNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher serverNameMatcher = serverNamePattern.matcher(releaseInfo);
		serverUrl = serverNameMatcher.find() ? serverNameMatcher.group(1) : null;
	}
	
	public Document getDocument() {
		return bookDomDocument;
	}
	
	public DomBook getDomBook() {
		return bookDom;
	}
	
	public String getServerUrl() {
		return serverUrl;
	}
	
//----------------------------------------------------------------------
	
	
	private File getFolderDir(String folderPath) {
		File f = coreDir;
		StringTokenizer st = new StringTokenizer(folderPath, "/");
		while (st.hasMoreTokens())
			f = new File(f, st.nextToken());
		return f;
	}
	
	private File getXmlDir(String shortBookName) {
		return new File(getFolderDir(shortBookName), "xml");
	}
	
	private File getSecDir(String shortBookName) {
		return new File(getFolderDir(shortBookName), "sec");
	}
	
	private File getBookXmlFile(String shortBookName) {
		return new File(getXmlDir(shortBookName), "book.xml");
	}
	
	public File getXmlFile(String shortBookName, String filePrefix) {
		return new File(getXmlDir(shortBookName), filePrefix + ".xml");
	}
	
	public File getSectionJsFile(String shortBookName, String filePrefix) {
		return new File(getSecDir(shortBookName), filePrefix + ".js");
	}
	
	public File getBookDir(String bookDirPath) {
		return getFolderDir(bookDirPath);
	}
	
	public File getSectionDir(String sectionPath) {
		return getFolderDir(sectionPath);
	}
	
	public File getStructureDir() {
		return new File(coreDir, "structure");
	}
	
	//----------------------------------------------------
	
	public File getBookDir() {
		return getFolderDir(getHomeDirName());
	}
	
/*
	private File getXmlDir() {
		return getXmlDir(getHomeDirName());
	}
	
	private File getTextDir() {
		return new File(getBookDir(), "text");
	}
*/
	
	public File getBookXmlFile() {
		return getBookXmlFile(getHomeDirName());
	}
	
	public File getXmlFile(String filePrefix) {
		return getXmlFile(getHomeDirName(), filePrefix);
	}
	
	public File getPageHtmlFile(String dirPath, String filePrefix) {
		File theDir = getFolderDir(dirPath);
		return new File(theDir, filePrefix + ".html");
	}
	
	public File getIndexTermsFile(String language) {
		return getTermsFile("index", language);
	}
	
	public File getUiTermsFile(String language) {
		return getTermsFile("uiTerms", language);
	}
	
	private File getTermsFile(String fileName, String language) {
		File termsDir = new File(coreDir, "terms");
		File f;
		if (language != null) {
			f = new File(termsDir, fileName + "_" + language + ".properties");
			if (f.exists())
				return f;
		}
		f = new File(termsDir, fileName + ".properties");
		if (f.exists())
			return f;
		else
			return null;
	}
	
	public File getDataSourceFile() {
		return new File(coreDir, "sources.data");
	}
	
	//----------------------------------------------------
	
	public String getShortBookName() {
		return shortBookName;
	}
	
	public String getHomeDirName() {
		return "bk/" + shortBookName;
	}
	
	public boolean isHomeDirName(String dir) {
		return getHomeDirName().equals(dir);
	}
	
	public File getCoreDir() {
		return coreDir;
	}

//----------------------------------------------------------------------
	
	public String getLongBookName() {
		return bookDom.getBookTitle();
	}
	
	protected String getTOCTitle() {
		return bookDom.getTOCTitle();
	}
	
	public String getVersionImage() {
		return bookDom.getVersionImage();
	}
	
	public String getLogoGif() {
		return bookDom.getLogoGif();
	}
	
	public boolean isLecturingVersion() {
		return bookDom.isLecturingVersion();
	}
	
	public boolean isModule() {
		return bookDom.isModule();
	}
	
	public boolean hasSummaries() {
		return bookDom.hasSummaries();
	}
	
	public boolean hasVideos() {
		return bookDom.hasVideos();
	}
	
	public String getLanguage() {
		return bookDom.getLanguage();
	}
	
	public String getDescription() {
		return bookDom.getDescription();
	}
}
