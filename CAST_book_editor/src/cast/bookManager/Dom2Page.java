package cast.bookManager;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.nio.channels.*;

import javax.swing.*;

import org.w3c.dom.*;

import cast.utils.*;


public class Dom2Page implements Transferable {
	static private DataFlavor kDomPageFlavor = null;
	static public DataFlavor getDomPageFlavor() throws Exception {
		if (kDomPageFlavor == null)
			kDomPageFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Dom2Page.class.getName());
		return kDomPageFlavor;
	}
	
	
	private Element domElement;
	
	public Dom2Page(Element domElement) {
		this.domElement = domElement;
	}
	
	public void moveElementToSection(CastSection castSection) {			//	changes owner (Document) for element when copying from other e-book
		Document doc = castSection.getDocument();
		domElement = (Element)doc.importNode(domElement, true);
	}
	
	public void setPageFile(String dir, String filePrefix) {
		domElement.setAttribute("dir", dir);
		domElement.setAttribute("filePrefix", filePrefix);
	}
	
	public void setSummaryPageFile(String dir, String filePrefix) {
		setAlternativePageFile(dir, filePrefix, "summary");
	}
	
	public void setVideoPageFile(String dir, String filePrefix) {
		setAlternativePageFile(dir, filePrefix, "video");
	}
	
	private void setAlternativePageFile(String dir, String filePrefix, String attributePrefix) {
		if (dir == null || dir.length() == 0)
			domElement.removeAttribute(attributePrefix + "Dir");
		else
			domElement.setAttribute(attributePrefix + "Dir", dir);
			
		if (filePrefix == null || filePrefix.length() == 0)
			domElement.removeAttribute(attributePrefix + "FilePrefix");
		else
			domElement.setAttribute(attributePrefix + "FilePrefix", filePrefix);
	}
	
//-----------------------------------------------------------------
	
	public Element getDomElement() {
		return domElement;
	}
	
	public Object getTransferData(DataFlavor flavor) {
		DataFlavor thisFlavor = null;
		try {
			thisFlavor = getDomPageFlavor();
		} catch (Exception e) {
			System.err.println("Problem lazy loading: " + e.getMessage());
			e.printStackTrace();
		}
		
		if (thisFlavor.equals(flavor))
			return this;
		else
			return null;
	}
	
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor flavors[] = {null};
		try {
			flavors[0] = getDomPageFlavor();
		} catch (Exception e) {
			System.err.println("Problem lazy loading: " + e.getMessage());
			e.printStackTrace();
		}
		
		return flavors;
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor thisFlavor = null;
		try {
			thisFlavor = getDomPageFlavor();
		} catch (Exception e) {
			System.err.println("Problem lazy loading: " + e.getMessage());
			e.printStackTrace();
		}
		
		return thisFlavor.equals(flavor);
	}

//------------------------------------------------------------------------
	
	public String getDescriptionFromXml() {
		String description = XmlHelper.getTagInterior(domElement);
		return XmlHelper.decodeHtml(description, XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String getDirFromXml() {
		return domElement.getAttribute("dir");
	}
	
	public String getFilePrefixFromXml() {
		return domElement.getAttribute("filePrefix");
	}
	
	public String getSummaryDirFromXml() {
		return domElement.getAttribute("summaryDir");
	}
	
	public String getSummaryFilePrefixFromXml() {
		return domElement.getAttribute("summaryFilePrefix");
	}
	
	public String getVideoDirFromXml() {
		return domElement.getAttribute("videoDir");
	}
	
	public String getVideoFilePrefixFromXml() {
		return domElement.getAttribute("videoFilePrefix");
	}
	
	public String getNoteFromXml() {
		String rawNote = domElement.getAttribute("note");
		return XmlHelper.decodeHtml(rawNote, XmlHelper.WITHOUT_PARAGRAPHS);
	}
	
	public String getCustomTitleFromXml() {
		return domElement.getAttribute("nameOverride");
	}

//------------------------------------------------------------------------
	
	protected boolean copyHtmlFile(String oldDir, String oldFilePrefix, String newDir, String newFilePrefix,
																																		Frame caller, CastEbook castEbook) {
		File oldFile = castEbook.getPageHtmlFile(oldDir, oldFilePrefix);
		File newFile = castEbook.getPageHtmlFile(newDir, newFilePrefix);
		
		if (newFile.exists())
			return true;							//	Just use existing file
		
/*
		if (newFile.exists()) {
			JOptionPane.showMessageDialog(caller, "File already exists:"
															+ "\n" + newDir + "/" + newFilePrefix + ".html", "Error!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	*/
		
		try {
			@SuppressWarnings("resource")
			FileChannel inChannel = new FileInputStream(oldFile).getChannel();
			@SuppressWarnings("resource")
			FileChannel outChannel = new FileOutputStream(newFile).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
			inChannel.close();
			outChannel.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(caller, "Could not copy file from (" + oldDir + "/" + oldFilePrefix
									+ ".html) to (" + newDir + "/" + newFilePrefix + ".html).", "Error!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	public boolean isNewItem() {
		return getDirFromXml().equals("bk/general") && getFilePrefixFromXml().equals("newPage");
	}
	
	public boolean createCopyInEbook(Component caller, CastEbook castEbook) {
		String oldDir = getDirFromXml();
		String oldFilePrefix = getFilePrefixFromXml();
		
		String newDir = castEbook.getHomeDirName();
		
		while (! (caller instanceof Frame))
			caller = caller.getParent();
		
		String newFilePrefix = FileNameChooser.findNewFilePrefix((Frame)caller, getFilePrefixFromXml(), newDir,
																														isNewItem(), FileNameChooser.PAGE_TYPE);
		if (newFilePrefix != null && newFilePrefix.length() > 0) {
			if (copyHtmlFile(oldDir, oldFilePrefix, newDir, newFilePrefix, (Frame)caller, castEbook)) {
				setPageFile(newDir, newFilePrefix);
				return true;
			}
		}
		return false;
	}
}
