package cast.bookManager;

import java.awt.*;
import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import cast.utils.*;


public class DomSection extends DomElement {
	private String savedSectionName = null;
	
	public DomSection(Element domElement, CastEbook castEbook) {
		super(domElement, castEbook);
	}
	
	public boolean isLockedPreface() {
		return CastEbook.isPreface(getFilePrefix());
	}
	
	public String getDir() {
		return domElement.getAttribute("dir");
	}
	
	public String getFilePrefix() {
		return domElement.getAttribute("file");
	}
	
	public void setSectionFile(String dir, String filePrefix) {
		domElement.setAttribute("dir", dir);
		domElement.setAttribute("file", filePrefix);
		savedSectionName = null;
		if (castEbook != null)
			castEbook.setDomChanged();
	}
	
	public String getSectionName() {
		if (savedSectionName == null) {
			File xmlFile = castEbook.getXmlFile(getDir(), getFilePrefix());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(true);
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document domForSection = db.parse(xmlFile);
				
				Element sectionElement = domForSection.getDocumentElement();
				savedSectionName = XmlHelper.decodeHtml(sectionElement.getAttribute("name"), XmlHelper.WITHOUT_PARAGRAPHS);
			} catch(Exception e) {
			}
		}
		return savedSectionName;
	}
	
	public boolean isNewItem() {
		return getDir().equals("bk/general") && getFilePrefix().equals("sec_new");
	}
	
	public DomElement cloneElement() {
		return new DomSection((Element)domElement.cloneNode(true), castEbook);
	}
	
	public boolean createCopyInEbook(Component caller) {
		String oldDir = getDir();
		String oldFilePrefix = getFilePrefix();
		
		String newDir = castEbook.getHomeDirName();
		
		while (! (caller instanceof Frame))
			caller = caller.getParent();
		
		String newFilePrefix = FileNameChooser.findNewFilePrefix((Frame)caller, getFilePrefix(), newDir, isNewItem(),
																																			FileNameChooser.SECTION_TYPE);
		if (newFilePrefix != null && newFilePrefix.length() > 0) {
			if (copyXmlFile(oldDir, oldFilePrefix, newDir, newFilePrefix, (Frame)caller)) {
				setSectionFile(newDir, newFilePrefix);
				return true;
			}
		}
		return false;
	}
}
