package cast.bookManager;

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.io.*;
import java.nio.channels.*;

import javax.swing.*;

import org.w3c.dom.*;


abstract public class DomElement implements Transferable {
	static private DataFlavor kDomElementFlavor = null;
	static public DataFlavor getDomElementFlavor() throws Exception {
		if (kDomElementFlavor == null)
			kDomElementFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + DomElement.class.getName());
		return kDomElementFlavor;
	}
	
	
	protected Element domElement;
	protected CastEbook castEbook;
	
	private Vector children = new Vector();
	
	public DomElement(Element domElement, CastEbook castEbook) {
		this.domElement = domElement;
		this.castEbook = castEbook;
		setupChildren();
	}
	
	private void setupChildren() {
		NodeList nodes = domElement.getChildNodes();
		int nNodes = nodes.getLength();
		for (int i=0 ; i<nNodes ; i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				String elementTag = ((Element)n).getTagName();
				
				DomElement child = null;
				
				if(elementTag.equals("part"))
						child = new DomPart((Element)n, castEbook);
				else if(elementTag.equals("chapter"))
						child = new DomChapter((Element)n, castEbook);
				else if(elementTag.equals("section"))
						child = new DomSection((Element)n, castEbook);
				else if(elementTag.equals("page"))
						child = new DomPage((Element)n, castEbook);
				
				if (child != null)				//	not for a description tag
					children.add(child);
			}
		}
	}
	
	public boolean isLockedPreface() {
		return false;
	}
	
	public int noOfChildren() {
		return children.size();
	}
	
	public DomElement getChild(int i) {
		return (DomElement)children.elementAt(i);
	}
	
	public DomElement cutChild(int i) {
		DomElement child = (DomElement)children.remove(i);
		domElement.removeChild(child.domElement);
		castEbook.setDomChanged();
		
		return child;
	}
	
	public void insertElement(DomElement child, int i) {
		if (i == children.size())
			domElement.appendChild(child.domElement);
		else {
			Element oldElement = getChild(i).domElement;
			domElement.insertBefore(child.domElement, oldElement);
		}
		children.add(i, child);
		castEbook.setDomChanged();
	}
	
	public void moveElementToEbook(CastEbook newCastEbook) {			//	changes owner (Document) for element when copying from other e-book
		Document doc = newCastEbook.getDocument();
		domElement = (Element)doc.importNode(domElement, true);
		castEbook = newCastEbook;
		
		children.clear();
		setupChildren();
	}
	
//-----------------------------------------------------------------
	
	abstract public DomElement cloneElement();
	
	abstract public boolean isNewItem();
	
	abstract public boolean createCopyInEbook(Component caller);
	
	protected boolean copyHtmlFile(String oldDir, String oldFilePrefix, String newDir, String newFilePrefix, Frame caller) {
		File oldFile = castEbook.getPageHtmlFile(oldDir, oldFilePrefix);
		File newFile = castEbook.getPageHtmlFile(newDir, newFilePrefix);
		if (newFile.exists()) {
			JOptionPane.showMessageDialog(caller, "File already exists:"
															+ "\n" + newDir + "/" + newFilePrefix + ".html", "Error!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
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
	
	protected boolean copyXmlFile(String oldDir, String oldFilePrefix, String newDir, String newFilePrefix, Frame caller) {
		File oldFile = castEbook.getXmlFile(oldDir, oldFilePrefix);
		File newFile = castEbook.getXmlFile(newDir, newFilePrefix);
		if (newFile.exists()) {
			JOptionPane.showMessageDialog(caller, "File already exists:"
															+ "\n" + newDir + "/xml/" + newFilePrefix + ".xml", "Error!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
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
			JOptionPane.showMessageDialog(caller, "Could not copy file from (" + oldDir + "/xml/" + oldFilePrefix
									+ ".xml) to (" + newDir + "/xml/" + newFilePrefix + ".xml).", "Error!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
//-----------------------------------------------------------------
	
	
	public Object getTransferData(DataFlavor flavor) {
		DataFlavor thisFlavor = null;
		try {
			thisFlavor = getDomElementFlavor();
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
			flavors[0] = getDomElementFlavor();
		} catch (Exception e) {
			System.err.println("Problem lazy loading: " + e.getMessage());
			e.printStackTrace();
		}
		
		return flavors;
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor thisFlavor = null;
		try {
			thisFlavor = getDomElementFlavor();
		} catch (Exception e) {
			System.err.println("Problem lazy loading: " + e.getMessage());
			e.printStackTrace();
		}
		
		return thisFlavor.equals(flavor);
	}
}
