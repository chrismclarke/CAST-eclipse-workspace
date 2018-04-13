package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import cast.bookManager.*;


public class NewTitle extends ElementTitle {
	static final private Color kNewBackground = new Color(0xFFFFCC);
	static final private Color kSelectedBackground = new Color(0xBBBBFF);
	
	static final public int NEW_PART = 0;
	static final public int NEW_CHAPTER = 1;
	static final public int NEW_SECTION = 2;
	static final public int NEW_PAGE = 3;
	
//	static private Document localDoc;
	static private DomPart domPart;
	static private DomChapter domChapter;
	static private DomSection domSection;
	static private DomPage domPage;
	static {
		try {
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document localDoc = docBuilder.newDocument();

			Element root = localDoc.createElement("root");
			localDoc.appendChild(root);
			
			Element partElement = localDoc.createElement("part");
			partElement.setAttribute("name", "New Part");
			root.appendChild(partElement);
			domPart = new DomPart(partElement, null);
			
			Element chapterElement = localDoc.createElement("chapter");
			chapterElement.setAttribute("dir", "bk/general");
			chapterElement.setAttribute("file", "ch_new");
			root.appendChild(chapterElement);
			domChapter = new DomChapter(chapterElement, null);
			
			Element sectionElement = localDoc.createElement("section");
			sectionElement.setAttribute("dir", "bk/general");
			sectionElement.setAttribute("file", "sec_new");
			root.appendChild(sectionElement);
			domSection = new DomSection(sectionElement, null);
			
			Element pageElement = localDoc.createElement("page");
			pageElement.setAttribute("dir", "bk/general");
			pageElement.setAttribute("file", "newPage");
			root.appendChild(pageElement);
			domPage = new DomPage(pageElement, null);
		}
		catch (Exception e) {
			System.err.println("NewTitle: could not create new DOM elements");
		}
	}
	
	static private DomElement getDomElement(int elementType) {
		switch (elementType) {
			case NEW_PART:
				return domPart;
			case NEW_CHAPTER:
				return domChapter;
			case NEW_SECTION:
				return domSection;
			case NEW_PAGE:
				return domPage;
		}
		return null;
	}
	
	
//	private String tag;
	
	private JPanel titlePanel;
	
	public NewTitle(int elementType, String newName) {
		super(getDomElement(elementType));
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setOpaque(false);
		
		titlePanel = new JPanel();
		titlePanel.setBackground(kNewBackground);
		titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
			Border spaced = BorderFactory.createEmptyBorder(3, 10, 3, 10);
			Border compound = BorderFactory.createCompoundBorder(raisedetched, spaced);
			titlePanel.setBorder(compound);
		
				JLabel title = new JLabel(newName);
				title.setFont(new Font("SansSerif", Font.BOLD, 14));
			
			titlePanel.add(title);
		
		add(titlePanel);
		
		addMouseListener(new MouseAdapter() {
															public void mousePressed(MouseEvent me) {
																JComponent comp = (JComponent)me.getComponent();
																select();
																if((me.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == 0 && !me.isControlDown()) {
																	comp.setTransferHandler(new ElementTransferHandler());
																	TransferHandler handler = comp.getTransferHandler();
																	handler.exportAsDrag(comp, me, TransferHandler.COPY);
																}
															}
												});
	}
	
	public Insets getInsets() {
		return new Insets(0, 10, 0, 0);
	}
	
	public void doHighlight(boolean selected) {
		titlePanel.setBackground(selected ? kSelectedBackground : kNewBackground);
	}
}
