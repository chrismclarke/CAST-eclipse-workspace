package ebookStructure;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;


public class DomSection extends DomElement {
	private String localSectionName;
	private String sectionOverview;
	
	public DomSection(Element domElement, CastEbook castEbook, int index, DomElement parent) {
		super(domElement, castEbook, index, parent);
	}
	
	public String getPageDescription() {
		String description = XmlHelper.getTagInterior(domElement);
		return XmlHelper.decodeHtml(description, XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String getSectionOverview() {
		return sectionOverview;
	}
	
	
	protected void setupChildren() {
		String dir = domElement.getAttribute("dir");
		String filePrefix = domElement.getAttribute("file");
		localSectionName = filePrefix;
		
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
			Document domForSection = db.parse(sectionXmlFile);
			
			domElement = domForSection.getDocumentElement();
			elementName = readElementName();
			
			sectionOverview = findSectionOverview();
			
			NodeList pageNodes = domElement.getElementsByTagName("page");
			int nNodes = pageNodes.getLength();
			int pageIndex = 1;
			for (int i=0 ; i<nNodes ; i++) {
				Element e = (Element)pageNodes.item(i);
				String pageFilePrefix = e.getAttribute("filePrefix");
				if (!pageFilePrefix.equals("otherInfo1"))		//	this page is only for the browser-based version of CAST
					children.add(new DomPage(e, castEbook, pageIndex++, this));
			}
		} catch(Exception e) {
			System.err.println("Error opening section (" + dir + ", " + filePrefix + ")\n" + e);
		}
	}
	
	private String findSectionOverview() {
		NodeList topTextNodes = domElement.getElementsByTagName("topText");
		int nNodes = topTextNodes.getLength();
		if (nNodes == 0)
			return null;
		else {
			Element topTextElement = (Element)topTextNodes.item(0);
			String topText = XmlHelper.getTagInterior(topTextElement);
			return topText;
		}
	}
	
	protected String readElementName() {
		String name = domElement.getAttribute("shortName");
		if (name == null || name.length() == 0)
			name = domElement.getAttribute("name");
		return XmlHelper.decodeHtml(name, XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String getLongName() {
		return XmlHelper.decodeHtml(domElement.getAttribute("name"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String[] getDirStrings() {
		return new String[kNoOfVersions];			//	all null since no HTML files are associated with sections
	}
	
	public String[] getFilePrefixStrings() {
		String prefixStrings[] = new String[kNoOfVersions];
		prefixStrings[FULL_VERSION] =  localSectionName;	//	so we can find it from link
		return prefixStrings;
	}
}
