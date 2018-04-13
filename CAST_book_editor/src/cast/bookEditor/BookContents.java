package cast.bookEditor;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import cast.bookManager.*;
import cast.utils.*;


public class BookContents extends CoreContentsPanel implements DropTargetListener {
	static final private int kDragSlop = 30;
	
	private CastEbook castEbook;
	
	private int[] dragTarget = null;
	private int localDragIndex;
	private int selectedDragIndex = -999;
	
	public BookContents(CastEbook castEbook) {
		this.castEbook = castEbook;
		
		if (castEbook.canEditBook() && castEbook.canChangeStructure()) {
			setTransferHandler(new ElementTransferHandler());
			setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, this, true));
		}
		else {
			setTransferHandler(null);
			setDropTarget(null);
		}
		
		relayout();
	}
	
	public void relayout() {
		removeAll();
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
		DomBook domBook = castEbook.getDomBook();
		int nChildren = domBook.noOfChildren();
		
		int chapterIndex = 0;
		for (int i=0 ; i<nChildren ; i++) {
			DomElement child = domBook.getChild(i);
			if (child instanceof DomChapter)
				add(new ChapterContents((DomChapter)child, castEbook, chapterIndex ++));
			else if (child instanceof DomPart)
				add(new PartTitle((DomPart)child, castEbook));
		}
		revalidate();
		repaint();
	}
	
	public void dragEnter(DropTargetDragEvent dtde) {
		Transferable t = dtde.getTransferable();
		try {
			DataFlavor elementFlavor = DomElement.getDomElementFlavor();
			if (t.isDataFlavorSupported(elementFlavor)) {
				DomElement dragElement = (DomElement)t.getTransferData(elementFlavor);
				DomBook domBook = castEbook.getDomBook();
				
				if (dragElement instanceof DomPart || dragElement instanceof DomChapter) {		// dragging part or chapter
					int nChildren = domBook.noOfChildren();
					dragTarget = new int[nChildren];
					localDragIndex = -999;
					selectedDragIndex = -1;
					for (int i=0 ; i<nChildren ; i++) {
						DomElement element = domBook.getChild(i);
						Component c = getComponent(i);
						if (element == dragElement)
							localDragIndex = i;
						else
							dragTarget[i] = c.getY() + c.getHeight();
					}
				}
				else {																		// dragging section or page
					int nTargets = 0;
					localDragIndex = -999;
					int nChapters = domBook.noOfChildren();
					for (int i=1 ; i<nChapters ; i++) {
						DomElement domChapter = domBook.getChild(i);
						nTargets += domChapter.noOfChildren() + 1;
					}
					
					dragTarget = new int[nTargets];
					nTargets = 0;
					for (int i=1 ; i<nChapters ; i++) {						//	don't allow changes to preface
						DomElement domChapter = domBook.getChild(i);
						if (domChapter instanceof DomPart)
							dragTarget[nTargets ++] = -999;
						else {
							Component chapter = getComponent(i);
							int nSections = ((Container)chapter).getComponentCount();
							for (int j=0 ; j<nSections ; j++) {
								DomElement domSection = (j == 0) ? null : domChapter.getChild(j - 1);		// first component is chapter title
								if (domSection == dragElement) {
									localDragIndex = nTargets;
									dragTarget[nTargets ++] = -999;
								}
								else {
									Component section = ((Container)chapter).getComponent(j);
									int yEndInChapter = section.getY() + section.getHeight();
									dragTarget[nTargets ++] = chapter.getY() + yEndInChapter;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dragOver(DropTargetDragEvent dtde) {
		Transferable t = dtde.getTransferable();
		try {
			DataFlavor elementFlavor = DomElement.getDomElementFlavor();
			if (t.isDataFlavorSupported(elementFlavor)) {
				int dropY = dtde.getLocation().y;
				
				int newSelectedIndex = -1;
				int minDist = Integer.MAX_VALUE;
				for (int i=0 ; i<dragTarget.length ; i++)
					if (i != localDragIndex - 1 && i != localDragIndex) {
						int dist = Math.abs(dropY - dragTarget[i]);
						if (dist < kDragSlop && dist < minDist) {
							newSelectedIndex = i;
							minDist = dist;
						}
					}
				
				if (selectedDragIndex != newSelectedIndex) {
					selectedDragIndex = newSelectedIndex;
					repaint();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}
	
	public void dragExit(DropTargetEvent dtde) {
		dragTarget = null;
		selectedDragIndex = -1;
		repaint();
	}
	
	public void drop(DropTargetDropEvent dtde) {
		Transferable t = dtde.getTransferable();
//		int dropY = getLocation().y;
		
		if (selectedDragIndex >= 0) {
			try {
				DataFlavor elementFlavor = DomElement.getDomElementFlavor();
				DomBook domBook = castEbook.getDomBook();
				
				if (t.isDataFlavorSupported(elementFlavor)) {
					DomElement dragElement = (DomElement)t.getTransferData(elementFlavor);
					
					if (dragElement.isNewItem())
						dragElement = dragElement.cloneElement();
					
					if (dragElement instanceof DomPart || dragElement instanceof DomChapter) {		// dropping part or chapter
						if (localDragIndex >= 0) {			//		dragging element in current e-book
							domBook.cutChild(localDragIndex);
							if (selectedDragIndex <= localDragIndex)
								selectedDragIndex ++;
						}
						else {
							dragElement.moveElementToEbook(castEbook);
							selectedDragIndex ++;
						}
						
						boolean canInsert = true;
						if (dragElement.isNewItem())
							canInsert = dragElement.createCopyInEbook(this);
						
						if (canInsert)
							domBook.insertElement(dragElement, selectedDragIndex);
					}
					else {																										// dropping section or page
						int nChapters = domBook.noOfChildren();
						if (localDragIndex >= 0) {			//		dragging element in current e-book
							if (localDragIndex < selectedDragIndex)
								selectedDragIndex --;
							for (int i=1 ; i<nChapters ; i++) {
								DomElement domChapter = domBook.getChild(i);
								int nSections = domChapter.noOfChildren();
								if (localDragIndex <= nSections) {
									domChapter.cutChild(localDragIndex - 1);		//	since "0" is for the chapter title
									break;
								}
								else
									localDragIndex -= (nSections + 1);
							}
						}
						else
							dragElement.moveElementToEbook(castEbook);
						
						for (int i=1 ; i<nChapters ; i++) {
							DomElement domChapter = domBook.getChild(i);
							int nSections = domChapter.noOfChildren();
							if (selectedDragIndex <= nSections) {
								boolean canInsert = true;
								if (dragElement.isNewItem())
									canInsert = dragElement.createCopyInEbook(this);
								
								if (canInsert)
									domChapter.insertElement(dragElement, selectedDragIndex);
								break;
							}
							else
								selectedDragIndex -= (nSections + 1);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			dragTarget = null;
			selectedDragIndex = -1;
		}
		
		castEbook.setDomChanged();
		relayout();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (selectedDragIndex >= 0) {
			g.setColor(Color.red);
			for (int i=-2 ; i<3 ; i++)
				g.drawLine(0, dragTarget[selectedDragIndex] + i, getSize().width, dragTarget[selectedDragIndex] + i);
		}
	}
}
