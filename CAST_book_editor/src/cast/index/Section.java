package cast.index;

import java.util.*;
import java.io.*;
import java.util.regex.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cast.bookManager.*;
import cast.utils.*;

public class Section {
	static final private int kMaxPages = 50;
//	static final private String kTitleStart = "<title>";
//	static final private String kTitleEnd = "</title>";
//	static final private String kAppletStart = "startApplet";
	
	private Page pg[] = new Page[kMaxPages];
	private int nPages = 0;
	protected String name;
	private int appletCount = 0;
	
	private boolean hasOverview = false;
	
	public Section(String dir, String filePrefix, CastEbook castEbook) {
		if (filePrefix.startsWith("sec_")) {
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
				File sectionXmlFile = castEbook.getXmlFile(dir, filePrefix);
				Document sectionDomDocument = db.parse(sectionXmlFile);
				
				Element domElement = sectionDomDocument.getDocumentElement();
				name = XmlHelper.decodeHtml(domElement.getAttribute("name"), XmlHelper.WITHOUT_PARAGRAPHS);
			} catch(Exception e) {
				System.err.println("Error opening section (" + dir + ", " + filePrefix + ")\n" + e);
			}
		}
		else {		//	page at section level
			File inFile = castEbook.getPageHtmlFile(dir, filePrefix);
			String s = HtmlHelper.getFileAsString(inFile);
		
			Pattern thePattern;
			Matcher theMatcher;
			
			thePattern = Pattern.compile("<title>(.*)</title>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			theMatcher = thePattern.matcher(s);
			if (theMatcher.find())
				name = theMatcher.group(1);
		
			thePattern = Pattern.compile("<applet", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			theMatcher = thePattern.matcher(s);
			while (theMatcher.find())
						appletCount ++;
		}
		
/*
		File overviewFileObject = castEbook.getPageHtmlFile(dir, filePrefix + "-o");
		File indexFileObject = castEbook.getPageHtmlFile(dir, filePrefix + "-i");
		hasOverview = overviewFileObject.exists() && indexFileObject.exists();
		
		if (!hasOverview)
			indexFileObject = castEbook.getPageHtmlFile(dir, filePrefix);
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(indexFileObject));
			
			String s;
			while ((s = reader.readLine()) != null) {
				int titleStart = s.indexOf(kTitleStart);
				int titleEnd = s.indexOf(kTitleEnd);
				if (titleStart >= 0 && titleEnd > titleStart)
					name = s.substring(titleStart + kTitleStart.length(), titleEnd);
				
				int appletStart = s.indexOf(kAppletStart);
				if (appletStart >= 0)
					appletCount ++;
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
*/
	}
	
	public boolean hasOverview() {
		return hasOverview;
	}
	
	public int countApplets() {
		int pageApplets = 0;
		for (int i=0 ; i<kMaxPages ; i++)
			if (pg[i] != null)
				pageApplets += pg[i].countApplets();
		return appletCount + pageApplets;
	}
	
	public int countPages() {
		return nPages + 1;
	}
	
	protected Page addPage(String dir, String filePrefix, int pageIndex, Hashtable indexTable,
																		Hashtable dataSetTable, Hashtable indexTranslationTable, CastEbook castEbook) {
		return pg[nPages++] = new Page(dir, filePrefix, pageIndex, this, indexTable, dataSetTable, indexTranslationTable, castEbook);
	}
}
