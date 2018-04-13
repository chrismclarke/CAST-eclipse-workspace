package ebookStructure;

import org.w3c.dom.*;


public class DomTopPage extends DomElement {		//	Pages in e-book other than within sections
	public DomTopPage(Element domElement, CastEbook castEbook, int index, DomElement parent) {
		super(domElement, castEbook, index, parent);
	}
	
	public String readElementName() {
		String dir = domElement.getAttribute("dir");
		String filePrefix = domElement.getAttribute("file");
		return XmlHelper.decodeHtml(HtmlHelper.getTagInFile(dir, filePrefix, castEbook, "title"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String[] getDirStrings() {
		String dirStrings[] = new String[kNoOfVersions];
		dirStrings[FULL_VERSION] = domElement.getAttribute("dir");
		return dirStrings;
	}
	
	public String[] getFilePrefixStrings() {
		String prefixStrings[] = new String[kNoOfVersions];
		prefixStrings[FULL_VERSION] = domElement.getAttribute("file");
		return prefixStrings;
	}
}
