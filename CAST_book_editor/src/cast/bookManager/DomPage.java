package cast.bookManager;

import java.awt.*;

import org.w3c.dom.*;

import cast.utils.*;


public class DomPage extends DomElement {
	public DomPage(Element domElement, CastEbook castEbook) {
		super(domElement, castEbook);
	}
	
	public String getDir() {
		return domElement.getAttribute("dir");
	}
	
	public String getFilePrefix() {
		return domElement.getAttribute("file");
	}
	
	public void setPageFile(String dir, String filePrefix) {
		domElement.setAttribute("dir", dir);
		domElement.setAttribute("file", filePrefix);
		if (castEbook != null)
			castEbook.setDomChanged();
	}
	
	public String getPageName() {
		return XmlHelper.decodeHtml(HtmlHelper.getTagInFile(getDir(), getFilePrefix(), castEbook, "title"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public boolean isNewItem() {
		return getDir().equals("bk/general") && getFilePrefix().equals("newPage");
	}
	
	public DomElement cloneElement() {
		return new DomPage((Element)domElement.cloneNode(true), castEbook);
	}
	
	public boolean createCopyInEbook(Component caller) {
		String oldDir = getDir();
		String oldFilePrefix = getFilePrefix();
		
		String newDir = castEbook.getHomeDirName();
		
		while (! (caller instanceof Frame))
			caller = caller.getParent();
		
		String newFilePrefix = FileNameChooser.findNewFilePrefix((Frame)caller, getFilePrefix(), newDir, isNewItem(),
																																		FileNameChooser.PAGE_TYPE);
		if (newFilePrefix != null && newFilePrefix.length() > 0) {
			if (copyHtmlFile(oldDir, oldFilePrefix, newDir, newFilePrefix, (Frame)caller)) {
				setPageFile(newDir, newFilePrefix);
				return true;
			}
		}
		return false;
	}
}
