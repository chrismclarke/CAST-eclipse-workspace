package ebookStructure;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;


abstract public class DomElement {
	static final public int SUMMARY_VERSION = 0;
	static final public int FULL_VERSION = 1;
	static final public int VIDEO_VERSION = 2;
	static final public int kNoOfVersions = 3;
	
	protected Element domElement;
	protected CastEbook castEbook;
	
	protected Vector children = new Vector();
	protected String elementName;
	private int index;
	private DomElement parent;
	
	public DomElement(Element domElement, CastEbook castEbook, int index, DomElement parent) {
		this.domElement = domElement;
		this.castEbook = castEbook;
		this.index = index;
		this.parent = parent;
		setupChildren();
	}
	
	protected void setupChildren() {
		NodeList nodes = domElement.getChildNodes();
		int nNodes = nodes.getLength();
		int index = this instanceof DomBook ? 0 : 1;	//	first chapter of books is 0. Preface
		for (int i=0 ; i<nNodes ; i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				String elementTag = ((Element)n).getTagName();
				
				DomElement child = null;
				
				if(elementTag.equals("part"))
						child = new DomPart((Element)n, castEbook, this);
				else if(elementTag.equals("chapter"))
						child = new DomChapter((Element)n, castEbook, index++, this);
				else if(elementTag.equals("section"))
						child = new DomSection((Element)n, castEbook, index++, this);
				else if(elementTag.equals("page"))
						child = new DomTopPage((Element)n, castEbook, index++, this);
				
				if (child != null)				//	not for a description tag
					children.add(child);
			}
		}
		elementName = readElementName();
	}
	
	public int noOfChildren() {
		return children.size();
	}
	
	public DomElement getChild(int i) {
		return (DomElement)children.elementAt(i);
	}
	
	abstract protected String readElementName();
	
	abstract public String[] getDirStrings();
	abstract public String[] getFilePrefixStrings();
	
	
	final public boolean[] versionsAllowed() {
		boolean allowed[] = new boolean[kNoOfVersions];		//	all false
		if (this instanceof DomSection)
			allowed[FULL_VERSION] = true;
		else {
			String[] dirs = getDirStrings();
			String[] prefixes = getFilePrefixStrings();
			for (int i=0 ; i<kNoOfVersions ; i++)
				if (dirs[i] != null && prefixes[i] != null) {
					File f = castEbook.getPageHtmlFile(dirs[i], prefixes[i]);
					allowed[i] = f.exists();
				}
			DomBook domBook = castEbook.getDomBook();
			if (!domBook.hasSummaries())
				allowed[SUMMARY_VERSION] = false;
			if (!domBook.hasVideos())
				allowed[VIDEO_VERSION] = false;
		}
		return allowed;
	}
	
	
	public File getFile(int version) {
		String dir = getDirStrings()[version];
		String filePrefix = getFilePrefixStrings()[version];
		if (dir == null)
			return null;
		else
			return castEbook.getPageHtmlFile(dir, filePrefix);
	}
	
/*
	public File getFile() {
		return getFile(FULL_VERSION);
	}
	
	
	public File getSummaryFile() {
		return getFile(SUMMARY_VERSION);
	}
	
	
	public File getVideoFile() {
		return getFile(VIDEO_VERSION);
	}
*/
	
	public String getName() {
		return elementName;
	}
	
	public int getIndex() {
		return index;
	}
	
	public DomElement nextChildFrom(int startIndex) {		//	looking forward from startIndex
		int n = noOfChildren();
		for (int i=startIndex ; i<n ; i++) {
			DomElement e = getChild(i);
			if (!(e instanceof DomPart))	// ignore parts when moving around e-book
				return e;
		}
		return null;
	}
	
	public DomElement previousChildFrom(int startIndex) {		//	looking back from startIndex
//		int n = noOfChildren();
		for (int i=startIndex ; i>=0 ; i--) {
			DomElement e = getChild(i);
			if (!(e instanceof DomPart))	// ignore parts when moving around e-book
				return e;
		}
		return null;
	}
	
	public DomElement firstChild() {
		return nextChildFrom(0);
	}
	
	public DomElement lastChild() {
		return previousChildFrom(noOfChildren() - 1);
	}
	
	private int getChildIndex(DomElement element) {
		int n = noOfChildren();
		for (int i=0 ; i<n ; i++) {
			DomElement e = getChild(i);
			if (e == element)
				return i;
		}
		return -1;
	}
	
	public DomElement childAfter(DomElement e) {
		int i = getChildIndex(e);
		return nextChildFrom(i + 1);
	}
	
	public DomElement childBefore(DomElement e) {
		int i = getChildIndex(e);
		return previousChildFrom(i - 1);
	}
	
	public DomElement nextElement() {
		DomElement nextE = firstChild();
		DomElement p = parent;
		DomElement e = this;
		while (nextE == null && p != null) {
			nextE = p.childAfter(e);
			e = p;
			p = p.parent;
		}
		return nextE;
	}
	
	public DomElement previousElement() {
		if (parent == null)
			return null;
		DomElement previousE = parent.childBefore(this);
		if (previousE == null)
			return previousE = parent;
		else {
			while (previousE.noOfChildren() > 0) {
				previousE = previousE.lastChild();
			}
		}

		return previousE;
	}
	
	public String[] getTitles() {
		String[] titles = new String[4];
//		DomElement e = this;
		if (parent == null)
			titles[1] = getName();
		else if (parent.parent == null) {
			titles[0] = String.valueOf(getIndex());
			titles[1] = getName();
		}
		else if (parent.parent.parent == null) {
			titles[0] =  String.valueOf(parent.getIndex());
			titles[1] = parent.getName();
			titles[2] = getIndex() + ". " + getName();
		}
		else {
			titles[0] =  String.valueOf(parent.parent.getIndex());
			titles[1] = parent.parent.getName();
			titles[2] = parent.getIndex() + ". " + parent.getName();
			titles[3] = getIndex() + ". " + getName();
			String note = ((DomPage)this).getBannerNote();
			if (note != null)
				titles[3] += "  " + note;
		}
		return titles;
	}
	
	public boolean hasAncestor(DomElement e) {
		if (e == this || this.parent == e)
			return true;
		return this.parent != null && this.parent.parent == e;
	}
	
	public boolean sameParent(DomElement e) {
		return parent == e.parent;
	}
}
