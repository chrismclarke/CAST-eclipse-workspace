package ebookStructure;

import org.w3c.dom.*;


public class DomBook extends DomElement {
	static final public String TYPE_BOOK = "book";
	static final public String TYPE_BOOK_AND_SUMMARIES = "book_and_summaries";	//	for backward compatibility
	static final public String TYPE_LECTURE = "lecture";
	static final public String TYPE_MODULE = "module";
	
	static public boolean hasTypeExtra(String typeString, char c) {
		while (typeString.length() >= 2 && typeString.charAt(typeString.length() - 2) == '_') {
			if (typeString.charAt(typeString.length() - 1) == c)
				return true;
			else
				typeString = typeString.substring(0, typeString.length() - 2);
		}
		return false; 
	}
	
	public DomBook(Element domElement, CastEbook castEbook) {
		super(domElement, castEbook, -1, null);
	}
	
	public String readElementName() {
		String dir = castEbook.getHomeDirName();
		String filePrefix = "book_splash";
		return XmlHelper.decodeHtml(HtmlHelper.getTagInFile(dir, filePrefix, castEbook, "title"), XmlHelper.WITHOUT_PARAGRAPHS);
//		return "About this CAST e-book";
	}
	
	public String getBookTitle() {
		return domElement.getAttribute("name");
	}
	
	public String getTOCTitle() {
		return domElement.getAttribute("tocTitle");
	}
	
	public String getVersionImage() {
		return domElement.getAttribute("versionImage");		//	On right of banner under arrows (relative to structure folder)
	}
	
	public String getLogoGif() {										//	logo under ToC (if it exists)
		String logoGif = domElement.getAttribute("logoGif");
		if (logoGif != null && logoGif.length() > 0)
			return logoGif;
		else
			return null;
	}
	
	public String getLanguage() {										//	null=English -- override with zh(Chinese), de(German), etc
		String language = domElement.getAttribute("language");
		if (language != null && language.length() > 0)
			return language;
		else
			return null;
	}
	
	public boolean isLecturingVersion() {
		String typeString = domElement.getAttribute("type");
		return typeString.equals(TYPE_LECTURE);
	}
	
	public boolean hasSummaries() {
		String typeString = domElement.getAttribute("type");
		if (typeString.equals(TYPE_BOOK_AND_SUMMARIES))
			return true;
		else
			return hasTypeExtra(typeString, 's');
	}
	
	public boolean hasVideos() {
		String typeString = domElement.getAttribute("type");
		return hasTypeExtra(typeString, 'v');
	}
	
	public boolean isModule() {
		String typeString = domElement.getAttribute("type");
		return typeString.equals(TYPE_MODULE);
	}
	
	public String getDescription() {
		NodeList descriptionNodes = domElement.getElementsByTagName("description");
		if (descriptionNodes.getLength() != 1)
			return null;
		Element descriptionElement = (Element)descriptionNodes.item(0);
		String description = descriptionElement.getFirstChild().getNodeValue();
		return description;
	}
	
	public String[] getDirStrings() {
		String dirStrings[] = new String[kNoOfVersions];
		dirStrings[FULL_VERSION] = dirStrings[VIDEO_VERSION] = castEbook.getHomeDirName();
		return dirStrings;
	}
	
	public String[] getFilePrefixStrings() {
		String prefixStrings[] = new String[kNoOfVersions];
		prefixStrings[FULL_VERSION] = "book_splash";
		prefixStrings[VIDEO_VERSION] = "v_book_splash";
		return prefixStrings;
	}
	
	public String getSummaryPdfUrl() {
		String s = domElement.getAttribute("summaryPdfUrl");
		if (s == null || s.length() == 0)
			return null;
		else
			return s;
	}
}
