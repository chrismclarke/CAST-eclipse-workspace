package ebookStructure;

import org.w3c.dom.*;


public class DomChapter extends DomElement {
	public DomChapter(Element domElement, CastEbook castEbook, int index, DomElement parent) {
		super(domElement, castEbook, index, parent);
	}
	
	public String readElementName() {
		String dir = domElement.getAttribute("dir");
		String filePrefix = domElement.getAttribute("file");
		return XmlHelper.decodeHtml(HtmlHelper.getTagInFile(dir, filePrefix, castEbook, "title"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String[] getDirStrings() {
		String dirStrings[] = new String[kNoOfVersions];
		dirStrings[FULL_VERSION] = dirStrings[VIDEO_VERSION] = domElement.getAttribute("dir");
		return dirStrings;
	}
	
	public String[] getFilePrefixStrings() {
		String prefixStrings[] = new String[kNoOfVersions];
		prefixStrings[FULL_VERSION] = domElement.getAttribute("file");
		prefixStrings[VIDEO_VERSION] = "v_" + prefixStrings[FULL_VERSION];
		return prefixStrings;
	}
}
