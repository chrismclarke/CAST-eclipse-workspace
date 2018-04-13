package ebookStructure;

import java.util.*;

import org.w3c.dom.*;


public class DomVariation {
	private Element domElement;
	
	private Vector endings = null;
	
	public DomVariation(Element domElement) {
		this.domElement = domElement;
		addEndings();
	}
	
	private void addEndings() {
		NodeList nl = domElement.getElementsByTagName("ending");
		if (nl != null && nl.getLength() > 0) {
			endings = new Vector();
			for (int i=0 ; i<nl.getLength() ; i++) {
				Element endingElement = (Element)nl.item(i);
				String ending = XmlHelper.getTagInterior(endingElement);
				endings.add(ending);
			}
		}
	}
	
	public String getShortName() {
		return XmlHelper.getUniqueTagAsString(domElement, "shortName");
	}
	
	public int getHeight() {
		return XmlHelper.getUniqueTagAsInt(domElement, "height");
	}
	
	public String getRawQuestionText() {
		String qn = XmlHelper.getUniqueTagAsString(domElement, "question");
		return qn.replaceAll("\\\\n", "\\n");
	}
	
	public String getRawParamString() {
		return XmlHelper.getUniqueTagAsString(domElement, "qnParam");
	}
	
	public String getEndings() {
		if (endings == null)
			return null;
		else {
			String endingString = "";
			for (int i=0 ; i<endings.size() ; i++) {
				if (endingString.length() > 0)
					endingString += "||||";
				endingString += (String)endings.elementAt(i);
			}
			return endingString.replaceAll("\\\\n", "\\n");
		}
	}
}
