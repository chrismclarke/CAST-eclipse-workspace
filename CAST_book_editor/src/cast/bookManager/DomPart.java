package cast.bookManager;

import java.awt.*;

import javax.swing.*;

import org.w3c.dom.*;

import cast.utils.*;


public class DomPart extends DomElement {
	public DomPart(Element domElement, CastEbook castEbook) {
		super(domElement, castEbook);
	}
	
	public String getPartName() {
		return XmlHelper.decodeHtml(domElement.getAttribute("name"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public void setPartName(String name) {
		String encodedName = XmlHelper.encodeHtml(name, XmlHelper.WITHOUT_PARAGRAPHS);
		domElement.setAttribute("name", encodedName);
		if (castEbook != null)
			castEbook.setDomChanged();
	}
	
	public boolean isNewItem() {
		return getPartName().equals("New Part");
	}
	
	public DomElement cloneElement() {
		return new DomPart((Element)domElement.cloneNode(true), castEbook);
	}
	
	public boolean createCopyInEbook(Component caller) {
		String newName = (String)JOptionPane.showInputDialog(caller, "Type the Part name:", "Part name",
																			JOptionPane.QUESTION_MESSAGE, null, null, getPartName());
		if (newName != null && newName.length() > 0) {
			setPartName(newName);
			return true;
		}
		return false;
	}
}
