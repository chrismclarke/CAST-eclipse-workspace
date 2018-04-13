package cast.sectionEditor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;


abstract public class CorePagePanel extends JPanel {
	
	static final public Color kPageBackground = new Color(0xFFFFCC);
	static final protected Color kGreyColor = new Color(0x666666);
	
	static final protected int TITLE_FROM_FILE = 0;
	static final protected int TITLE_CUSTOMISED = 1;
	static final protected int TITLE_EXERCISE = 2;
	
	protected CastSection castSection;
	protected Dom2Page pageDom;
	protected int pageNo;
	protected String dir, filePrefix, pageTitle, description, note, customTitle;
	
	protected boolean showNote;
	protected int titleType;
	
	protected JPopupMenu menu = null;
	
	protected JLabel titleInFile, location;
	
	public CorePagePanel(Dom2Page pageDom, CastSection castSection, int pageNo) {
		this.pageDom = pageDom;
		this.castSection = castSection;
		this.pageNo = pageNo;
		
		if (castSection != null) {						//	castSection == null for NewPagePanel
			dir = pageDom.getDirFromXml();
			filePrefix = pageDom.getFilePrefixFromXml();
			note = pageDom.getNoteFromXml();
			customTitle = pageDom.getCustomTitleFromXml();
			
			pageTitle = HtmlHelper.getTagInFile(dir, filePrefix, castSection.getCastEbook(), "title");
			pageTitle = XmlHelper.decodeHtml(pageTitle, XmlHelper.WITHOUT_PARAGRAPHS);
			
			description = pageDom.getDescriptionFromXml();
			
			showNote = note != null && note.length() > 0;
			if (customTitle != null && customTitle.length() > 0)
				titleType = (customTitle.indexOf("#r#") == 0) ? TITLE_EXERCISE : TITLE_CUSTOMISED;
			else
				titleType = TITLE_FROM_FILE;
		}
	}
	
	public Insets getInsets() {
		return new Insets(3, 6, 3, 3);
	}
	
	public Dom2Page getPageDom() {
		return pageDom;
	}
	
	public CastSection getCastSection() {
		return castSection;
	}
	
	abstract public void updatePageDom();
	
	public void updateAfterResize() {
		SectionContents sectionContents = (SectionContents)getParent();
		if (sectionContents != null) {
			sectionContents.resetPageBoundaries();
			sectionContents.repaint();
		}
	}
	
	
	public void select() {
		SectionContents.select(this);
	}
	
	public void doHighlight(boolean selected) {
	}
	
	protected class MouseDragListener extends MouseAdapter {
		public void mousePressed(MouseEvent me) {
			JComponent comp = (JComponent)me.getComponent();
			select();
			if((me.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0 || me.isControlDown()) {
				if (menu != null)
					menu.show(comp, me.getX(), me.getY());
			}
			else {
				while (!(comp instanceof CorePagePanel)) {
					me.translatePoint(comp.getX(), comp.getY());
					comp = (JComponent)comp.getParent();
				}
				comp.setTransferHandler(new PageTransferHandler());
				TransferHandler handler = comp.getTransferHandler();
				handler.exportAsDrag(comp, me, TransferHandler.COPY);
			}
		}
	}
}
