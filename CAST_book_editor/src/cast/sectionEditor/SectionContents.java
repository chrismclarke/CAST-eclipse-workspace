package cast.sectionEditor;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;


public class SectionContents extends JPanel implements DropTargetListener {
	static final private Color kSpacerColor = new Color(0x9999FF);
	
	static final private int kDragSlop = 30;
	
	static final public int DRAG_DROP_DISPLAY = 0;
	static final public int EDIT_DISPLAY = 1;
	
	static private CorePagePanel selectedPagePanel = null;			//	static so it can clear highlights in other windows
	
	static public void select(CorePagePanel newPagePanel) {
		if (selectedPagePanel != null)
			selectedPagePanel.doHighlight(false);
		selectedPagePanel = newPagePanel;
		if (selectedPagePanel != null)
			selectedPagePanel.doHighlight(true);
	}
	
	private CastSection castSection;
	private Dom2Section sectionDom;
	private int displayType;
	
	private int[] pageBoundary;
	private int localDragIndex;
	private int selectedDragIndex;
	
	
	public SectionContents(CastSection castSection, int displayType) {
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 3));
		setBackground(PagePanel.kPageBackground);
		setOpaque(true);
		
		this.castSection = castSection;
		sectionDom = castSection.getDomSection();
		this.displayType = displayType;
		
		if (castSection.canEditSection() && castSection.getCastEbook().canChangeStructure()) {
			setTransferHandler(new PageTransferHandler());
			setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, this, true));
		}
		
		relayout();
	}
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
		relayout();
	}
	
	public void relayout() {
		resetPageBoundaries();
		
		removeAll();
		Dom2Section domSection = castSection.getDomSection();
		int nChildren = domSection.noOfChildren();
		
		for (int i=0 ; i<nChildren ; i++) {
			Dom2Page child = domSection.getChild(i);
			if (displayType == EDIT_DISPLAY)
				add(new PagePanel(child, castSection, i + 1));
			else
				add(new PageDragPanel(child, castSection, i + 1));
		}
		
		revalidate();
		repaint();
	}
	
	private Object getDragObject(Transferable t) throws ClassNotFoundException {
		try {
			DataFlavor pageFlavor = Dom2Page.getDomPageFlavor();
			if (t.isDataFlavorSupported(pageFlavor))
				return t.getTransferData(pageFlavor);
			
			DataFlavor[] flavors = t.getTransferDataFlavors();
			for (int zz = 0; zz < flavors.length; zz++) {
				System.out.println("Flavor: " + flavors[zz].toString());
				if (flavors[zz].isRepresentationClassReader()) {
					Reader reader = flavors[zz].getReaderForText(t);
					BufferedReader br = new BufferedReader(reader);
					
					String fileName = br.readLine();
					System.out.println("Mac file dragged: " + fileName);
					br.close();
					return new File(fileName);
				}
			}
			
			DataFlavor windowsFlavor = DataFlavor.javaFileListFlavor;
			if (t.isDataFlavorSupported(windowsFlavor)) {
				java.util.List fileList = (java.util.List)t.getTransferData(windowsFlavor);
        File f = (File)fileList.get(0);
				System.out.println("Windows file dragged: " + f);
				return f;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void dragEnter(DropTargetDragEvent dtde) {
		try {
			Object dragObject = getDragObject(dtde.getTransferable());
			if (dragObject != null) {
        dtde.acceptDrag(DnDConstants.ACTION_COPY );
				checkPageBoundaries();
				
				Dom2Page dragElement = null;
				if (dragObject instanceof Dom2Page)
					dragElement = (Dom2Page)dragObject;
				Dom2Section domSection = castSection.getDomSection();
				
				int nPages = domSection.noOfChildren();
				localDragIndex = -999;
				selectedDragIndex = -1;
				for (int i=0 ; i<nPages ; i++) {
					Dom2Page element = domSection.getChild(i);
//					Component c = getComponent(i);
					if (element == dragElement)
						localDragIndex = i;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dragOver(DropTargetDragEvent dtde) {
		try {
			Object dragObject = getDragObject(dtde.getTransferable());
			if (dragObject != null) {
				int dropY = dtde.getLocation().y;
				
				int newSelectedIndex = -1;
				int minDist = Integer.MAX_VALUE;
				for (int i=0 ; i<pageBoundary.length ; i++)
					if (i != localDragIndex && i != localDragIndex + 1) {
						int dist = Math.abs(dropY - pageBoundary[i]);
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
		pageBoundary = null;
		selectedDragIndex = -1;
		repaint();
	}
	
	public void drop(DropTargetDropEvent dtde) {
		Transferable t = dtde.getTransferable();
//		int dropY = getLocation().y;
		
		if (selectedDragIndex >= 0) {
			try {
				DataFlavor pageFlavor = Dom2Page.getDomPageFlavor();
				Dom2Section domSection = castSection.getDomSection();
				
				if (t.isDataFlavorSupported(pageFlavor)) {
					Dom2Page dragPage = (Dom2Page)t.getTransferData(pageFlavor);
					
					if (localDragIndex >= 0) {			//		dragging element in current section
						domSection.cutChild(localDragIndex);
						if (selectedDragIndex > localDragIndex)
							selectedDragIndex --;
					}
					else {
						if (dragPage.isNewItem()) {
							dragPage = NewPagePanel.createNewPage();
							
							if (!dragPage.createCopyInEbook(SectionContents.this, castSection.getCastEbook()))
								return;
						}
						dragPage.moveElementToSection(castSection);
					}
					
					domSection.insertElement(dragPage, selectedDragIndex);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			pageBoundary = null;
			selectedDragIndex = -1;
		}
		
		castSection.setDomChanged();
		relayout();
	}
	
	private void checkPageBoundaries() {
		if (pageBoundary == null) {
			int nPages = sectionDom.noOfChildren();
			pageBoundary = new int[nPages + 1];
			
			for (int i=0 ; i<nPages ; i++) {
				Component c = getComponent(i);
				pageBoundary[i + 1] = c.getY() + c.getHeight() + 1;
			}
		}
	}
	
	public void resetPageBoundaries() {
		pageBoundary = null;
		selectedDragIndex = -999;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		checkPageBoundaries();
		
		for (int i=0 ; i<pageBoundary.length ; i++) {
			if (i == selectedDragIndex) {
				g.setColor(Color.red);
				for (int j=-2 ; j<3 ; j++)
					g.drawLine(0, pageBoundary[i] + j, getSize().width, pageBoundary[i] + j);
			}
			else if (i > 0) {
				g.setColor(kSpacerColor);
				for (int j=-1 ; j<2 ; j++)
					g.drawLine(0, pageBoundary[i] + j, getSize().width, pageBoundary[i] + j);
			}
		}
	}
}
