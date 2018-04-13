package ebookStructure;

import org.w3c.dom.*;


public class DomPage extends DomElement {		//	Page in e-book that is within a Section
	public DomPage(Element domElement, CastEbook castEbook, int index, DomElement parent) {
		super(domElement, castEbook, index, parent);
	}
	

	public String readElementName() {
		String nameOverride = domElement.getAttribute("nameOverride");
		if (nameOverride == null || nameOverride.length() == 0) {
			String dir = domElement.getAttribute("dir");
			String filePrefix = domElement.getAttribute("filePrefix");
			String title = XmlHelper.decodeHtml(HtmlHelper.getTagInFile(dir, filePrefix, castEbook, "title"), XmlHelper.WITHOUT_PARAGRAPHS);
			return title;
		}
		else {
			nameOverride = XmlHelper.decodeHtml(nameOverride.replaceAll("#.#", ""), XmlHelper.WITHOUT_PARAGRAPHS);
			return nameOverride;
		}
	}

	
	public String getPageDescription() {
		String description = XmlHelper.getTagInterior(domElement);
		return XmlHelper.decodeHtml(description, XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
/*
	public File getFile() {
		String dir = domElement.getAttribute("dir");
		String filePrefix = domElement.getAttribute("filePrefix");
		return castEbook.getPageHtmlFile(dir, filePrefix);
	}
*/
	
	public String[] getDirStrings() {
		String dirStrings[] = new String[kNoOfVersions];
		dirStrings[FULL_VERSION] = domElement.getAttribute("dir");
		dirStrings[VIDEO_VERSION] = domElement.getAttribute("videoDir");
		dirStrings[SUMMARY_VERSION] = domElement.getAttribute("summaryDir");
		return dirStrings;
	}
	
	public String[] getFilePrefixStrings() {
		String prefixStrings[] = new String[kNoOfVersions];
		prefixStrings[FULL_VERSION] = domElement.getAttribute("filePrefix");
		prefixStrings[VIDEO_VERSION] = domElement.getAttribute("videoFilePrefix");
		prefixStrings[SUMMARY_VERSION] = domElement.getAttribute("summaryFilePrefix");
		return prefixStrings;
	}
	
	public String getSectionNote() {
		String note = domElement.getAttribute("note");
		if (note == null)
			return null;
		else {
			int hashIndex = note.indexOf("#");
			if (hashIndex >= 0)
				note = note.substring(0, hashIndex);
			return note;
		}
	}
	
	public String getBannerNote() {
		String note = domElement.getAttribute("note");
		int hashIndex = note.indexOf("#");
		if (hashIndex >= 0)
			return note.substring(hashIndex + 1);
		else
			return null;
	}
}
