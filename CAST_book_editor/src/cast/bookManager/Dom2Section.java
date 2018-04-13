package cast.bookManager;

import java.util.*;

import org.w3c.dom.*;

import cast.sectionEditor.*;
import cast.utils.*;


public class Dom2Section {
	protected Element domElement;
	protected CastSection castSection;
	
	private Vector children = new Vector();
	
	public Dom2Section(Element domElement, CastSection castSection) {
		this.domElement = domElement;
		this.castSection = castSection;
		
		NodeList pageNodes = domElement.getElementsByTagName("page");
		int nNodes = pageNodes.getLength();
		for (int i=0 ; i<nNodes ; i++)
			children.add(new Dom2Page((Element)pageNodes.item(i)));
//			children.add(new Dom2Page((Element)pageNodes.item(i), castSection));
	}
	
	public String getDir() {
		return castSection.getDir();
	}
	
	public Element getDomElement() {
		return domElement;
	}
	
	public int noOfChildren() {
		return children.size();
	}
	
	public Dom2Page getChild(int i) {
		return (Dom2Page)children.elementAt(i);
	}
	
	public Dom2Page cutChild(int i) {
		Dom2Page child = (Dom2Page)children.remove(i);
		domElement.removeChild(child.getDomElement());
		castSection.setDomChanged();
		
		return child;
	}
	
	public void insertElement(Dom2Page child, int i) {
		if (i == children.size())
			domElement.appendChild(child.getDomElement());
		else {
			Element oldElement = getChild(i).getDomElement();
			domElement.insertBefore(child.getDomElement(), oldElement);
		}
		children.add(i, child);
		castSection.setDomChanged();
	}

//------------------------------------------------------------------------
	
	public String getTopTextFromXml() {
		String topText = XmlHelper.getUniqueTagAsString(domElement, "topText");
		return XmlHelper.decodeHtml(topText, XmlHelper.WITH_PARAGRAPHS);
	}
	
	public String getSectionNameFromXml() {
		return XmlHelper.decodeHtml(domElement.getAttribute("name"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String getShortSectionNameFromXml() {
		return XmlHelper.decodeHtml(domElement.getAttribute("shortName"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public void updateDom(SectionHeader sectionHeader) {
		String topText = sectionHeader.getTopText();
		Element topTextElement = XmlHelper.getUniqueTag(domElement, "topText");
		if (topText == null) {
			if (topTextElement != null)
				domElement.removeChild(topTextElement);
		}
		else {
			String encodedTopText = XmlHelper.encodeHtml(topText, XmlHelper.WITH_PARAGRAPHS);
			if (topTextElement == null) {
				topTextElement = castSection.getDocument().createElement("topText");
				Text textNode = castSection.getDocument().createCDATASection(encodedTopText);
				topTextElement.appendChild(textNode);
				if (domElement.getChildNodes().getLength() == 0)
					domElement.appendChild(topTextElement);
				else {
					Node firstElement = domElement.getChildNodes().item(0);
					domElement.insertBefore(topTextElement, firstElement);
				}
			}
			else {
				CDATASection textData = (CDATASection)topTextElement.getFirstChild();
				textData.setData(encodedTopText);
			}
		}
		
		String sectionName = sectionHeader.getSectionName();
		String encodedSectionName = XmlHelper.encodeHtml(sectionName, XmlHelper.WITHOUT_PARAGRAPHS);
		domElement.setAttribute("name", encodedSectionName);
		
		String shortSectionName = sectionHeader.getShortSectionName();
		if (shortSectionName == null)
			domElement.removeAttribute("shortName");
		else {
			String encodedShortName = XmlHelper.encodeHtml(shortSectionName, XmlHelper.WITHOUT_PARAGRAPHS);
			domElement.setAttribute("shortName", encodedShortName);
		}
	}
	
}
