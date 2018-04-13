package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import cast.bookManager.*;


abstract public class ElementTitle extends JPanel {
	static final private Color kSelectedBackground = new Color(0xBBBBFF);
	
	private DomElement domElement;
	
	protected JPopupMenu menu = null;
	protected JLabel title;
	
	public ElementTitle(DomElement domElement) {
		this.domElement = domElement;
	}
	
	public void select() {
		getContentsPanel().select(this);
	}
	
	private CoreContentsPanel getContentsPanel() {
		Component c = this;
		while (!(c instanceof CoreContentsPanel))
			c = c.getParent();
		return (CoreContentsPanel)c;
	}
	
	public void doHighlight(boolean selected) {
		if (selected) {
			setBackground(kSelectedBackground);
			setOpaque(true);
		}
		else
			setOpaque(false);
			setTransferHandler(null);
			setDropTarget(null);
			repaint();
	}
	
	public DomElement getDomElement() {
		return domElement;
	}
	
	protected void setMenuDragMouseListener(CastEbook castEbook) {
		if (castEbook.canChangeStructure())
			addMouseListener(new MouseAdapter() {
															public void mousePressed(MouseEvent me) {
																JComponent comp = (JComponent)me.getComponent();
																select();
																if((me.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0 || me.isControlDown()) {
																	if (menu != null)
																		menu.show(comp, me.getX(), me.getY());
																}
																else if (!domElement.isLockedPreface()) {
																	comp.setTransferHandler(new ElementTransferHandler());
																	TransferHandler handler = comp.getTransferHandler();
																	handler.exportAsDrag(comp, me, TransferHandler.COPY);
																}
															}
												});
	}
	
	protected void setTitleForeground(Color enabledColor, Color disabledColor, CastEbook castEbook) {
		title.setForeground(castEbook.canEditBook() && castEbook.canChangeStructure() ? enabledColor : disabledColor);
	}
	
	protected void deleteSelf(CastEbook castEbook) {
		DomElement domBook = castEbook.getDomBook();
		int nChapters = domBook.noOfChildren();			//	includes parts
		for (int i=0 ; i<nChapters ; i++) {
			DomElement domChapterI = domBook.getChild(i);
			if (domChapterI == domElement)  {
				domBook.cutChild(i);
				((BookContents)getContentsPanel()).relayout();
				return;
			}
			int nSections = domChapterI.noOfChildren();
			for (int j=0 ; j<nSections ; j++) {
				DomElement domSectionJ = domChapterI.getChild(j);
				if (domSectionJ == domElement)  {
					domChapterI.cutChild(j);
					((BookContents)getContentsPanel()).relayout();
					return;
				}
			}
		}
	}
}