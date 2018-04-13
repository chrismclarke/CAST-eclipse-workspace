package cast.exercise;

import org.w3c.dom.*;

import cast.utils.*;


public class DomEnding {
	static public boolean CREATE_COPY = true;
	static public boolean USE_ORIGINAL = false;
	
	private DomVariation variation;
	private Element domElement;
	
	public DomEnding(Element domElement, DomVariation variation, boolean createCopyInDocument) {
		this.variation = variation;
		if (createCopyInDocument) {
			Document domDocument = variation.getDocument();
			this.domElement = (Element)domDocument.importNode(domElement, true);
		}
		else
			this.domElement = domElement;
	}
	
	public Element getDomElement() {
		return domElement;
	}
	
	public String getRawEndingString() {
		return XmlHelper.getTagInterior(domElement);
	}
	
	public DomVariation getVariation() {
		return variation;
	}
	
	public void updateDom(String endingString) {
		XmlHelper.setTagInterior(domElement, endingString);
	}
}
