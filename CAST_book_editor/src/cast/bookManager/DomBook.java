package cast.bookManager;

import java.awt.*;

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
		super(domElement, castEbook);
	}
	
	public String getLongBookName() {
		return domElement.getAttribute("name");
	}
	
	public String getSummaryPdfUrl() {
		String url = domElement.getAttribute("summaryPdfUrl");
		if (url != null && url.length() > 0)
			return url;
		else
			return null;
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
	
//---------------------------------------------------------
	
	public void setLongBookName(String name) {
		domElement.setAttribute("name", name);
		castEbook.setDomChanged();
	}
	
	public void setTOCTitle(String tocTitle) {
		domElement.setAttribute("tocTitle", tocTitle);
		castEbook.setDomChanged();
	}
	
	public void setVersionImage(String versionImage) {
		domElement.setAttribute("versionImage", versionImage);
		castEbook.setDomChanged();
	}
	
	public void setLogoGif(String logoGif) {
		if (logoGif == null)
			domElement.removeAttribute("logoGif");
		else
			domElement.setAttribute("logoGif", logoGif);
		castEbook.setDomChanged();
	}
	
	public void setFullPdfUrl(String pdfUrl) {
		if (pdfUrl == null)
			domElement.removeAttribute("fullPdfUrl");
		else
			domElement.setAttribute("fullPdfUrl", pdfUrl);
		castEbook.setDomChanged();
	}
	
	public void setSummaryPdfUrl(String pdfUrl) {
		if (pdfUrl == null)
			domElement.removeAttribute("summaryPdfUrl");
		else
			domElement.setAttribute("summaryPdfUrl", pdfUrl);
		castEbook.setDomChanged();
	}
	
	public void setLanguage(String language) {
		if (language == null)
			domElement.removeAttribute("language");
		else
			domElement.setAttribute("language", language);
		castEbook.setDomChanged();
	}
	
	public void setBookType(String coreType, boolean withSummaries, boolean withVideos) {
		if (withSummaries)
			coreType += "_s";
		if (withVideos)
			coreType += "_v";
		domElement.setAttribute("type", coreType);
		castEbook.setDomChanged();
	}
	
	public void setDescription(String description) {
		NodeList descriptionNodes = domElement.getElementsByTagName("description");
		if (descriptionNodes.getLength() == 0) {
			Document doc = castEbook.getDocument();
			Element descriptionElement = doc.createElement("description");
			Node newtext = doc.createTextNode(description);
			descriptionElement.appendChild(newtext);
			
			domElement.insertBefore(descriptionElement, domElement.getFirstChild());
		}
		else {
			Element descriptionElement = (Element)descriptionNodes.item(0);
			Node textNode = descriptionElement.getFirstChild();
			textNode.setNodeValue(description);
		}
		castEbook.setDomChanged();
	}
	
	public boolean isNewItem() {
		return false;
	}
	
	public DomElement cloneElement() {
		return null;
	}
	
	public boolean createCopyInEbook(Component caller) {
		return false;
	}
	
}
