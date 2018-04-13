package ebookStructure;

import org.w3c.dom.*;


public class DomPart extends DomElement {
	public DomPart(Element domElement, CastEbook castEbook, DomElement parent) {
		super(domElement, castEbook, -1, parent);
	}
	
	public String readElementName() {
		return XmlHelper.decodeHtml(domElement.getAttribute("name"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String[] getDirStrings() {
		return new String[kNoOfVersions];			//	all null since part cannot be selected
	}
	
	public String[] getFilePrefixStrings() {
		return new String[kNoOfVersions];
	}
}
