package cast.bookManager;

import java.awt.*;

import org.w3c.dom.*;

import cast.utils.*;


public class DomChapter extends DomElement {
	public DomChapter(Element domElement, CastEbook castEbook) {
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
	
	public void setChapterFile(String dir, String filePrefix) {
		domElement.setAttribute("dir", dir);
		domElement.setAttribute("file", filePrefix);
		if (castEbook != null)
			castEbook.setDomChanged();
	}
	
	public String getChapterName() {
		return XmlHelper.decodeHtml(HtmlHelper.getTagInFile(getDir(), getFilePrefix(), castEbook, "title"), XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public boolean isNewItem() {
		return getDir().equals("bk/general") && getFilePrefix().equals("ch_new");
	}
	
	public DomElement cloneElement() {
		return new DomChapter((Element)domElement.cloneNode(true), castEbook);
	}
	
	public boolean createCopyInEbook(Component caller) {
		String oldDir = getDir();
		String oldFilePrefix = getFilePrefix();
		
		String newDir = castEbook.getHomeDirName();
		
		while (! (caller instanceof Frame))
			caller = caller.getParent();
		
		String newFilePrefix = FileNameChooser.findNewFilePrefix((Frame)caller, getFilePrefix(), newDir, isNewItem(),
																																		FileNameChooser.CHAPTER_TYPE);
		if (newFilePrefix != null && newFilePrefix.length() > 0) {
			if (copyHtmlFile(oldDir, oldFilePrefix, newDir, newFilePrefix, (Frame)caller)) {
				setChapterFile(newDir, newFilePrefix);
				return true;
			}
		}
		return false;
	}
}
