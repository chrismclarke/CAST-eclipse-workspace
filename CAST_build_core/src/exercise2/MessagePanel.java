package exercise2;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import dataView.*;
import utils.*;
import formula.*;


abstract public class MessagePanel extends XPanel {

	static final public boolean LEFT_ALIGN = true;
	static final public boolean CENTER_ALIGN = false;

	static final public boolean CAN_SCROLL = true;
	static final public boolean NO_SCROLL = false;
	
	protected XApplet applet;
	
	private MessageTextPane textPane;
	private JScrollPane areaScrollPane = null;
	
//	private Style textStyle, headingStyle, subStyle, superStyle;
//	private Style leftParaStyle, centredParaStyle;
	
//	private int imageIndex = 0;
	
	
	public MessagePanel(String panelTitle, XApplet applet, boolean canScroll) {
		this(panelTitle, applet, canScroll, 12);
	}
	
	
	public MessagePanel(String panelTitle, XApplet applet, boolean canScroll, int baseFontSize) {
		this(panelTitle, applet, canScroll, baseFontSize, BorderFactory.createLineBorder(Color.gray));
	}
	
	public MessagePanel(String panelTitle, XApplet applet, boolean canScroll,
																									int baseFontSize, Border basicBorder) {
										//		*********************
										//		starts with no content. Must call changeContent() immediately.
										//		*********************
		setLayout(new BorderLayout(0, 0));
		this.applet = applet;
		textPane = new MessageTextPane(baseFontSize);
			
		if (canScroll) {
			areaScrollPane = createScrollingPane(textPane, panelTitle, basicBorder);
				
			add("Center", areaScrollPane);
		}
		else {
			textPane.setBorder(basicBorder);
			if (panelTitle != null) {
				Border emptyBorder = new EmptyBorder(0, 0, 0, 0);
				TitledBorder border = BorderFactory.createTitledBorder(emptyBorder, panelTitle);
				border.setTitlePosition(TitledBorder.ABOVE_TOP);
				border.setTitleFont(applet.getStandardBoldFont());
				setBorder(border);
			}
			add("Center", textPane);
		}
	}
	
	public void setBackground(Color c) {
		super.setBackground(c);
		if (!backgroundLocked && areaScrollPane != null)
			areaScrollPane.setBackground(c);
	}
	
	public void lockBackground(Color c) {
		super.lockBackground(c);
		if (areaScrollPane != null)
			areaScrollPane.setBackground(c);
	}
	
	protected JScrollPane createScrollingPane(JTextPane content, String panelTitle, Border basicBorder) {
		JScrollPane areaScrollPane = new JScrollPane(textPane);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		areaScrollPane.setViewportBorder(basicBorder);
		Border emptyBorder = new EmptyBorder(0, 0, 0, 0);
		if (panelTitle == null)
			areaScrollPane.setBorder(emptyBorder);
		else {
			TitledBorder border = BorderFactory.createTitledBorder(emptyBorder, panelTitle);
			border.setTitlePosition(TitledBorder.ABOVE_TOP);
			border.setTitleFont(applet.getStandardBoldFont());
			areaScrollPane.setBorder(border);
		}
		return areaScrollPane;
	}
	
	public void setTextBackground(Color c) {
		textPane.setBackground(c);
	}
	
	public void changeContent() {
		textPane.setEditable(true);
		textPane.selectAll();
		textPane.replaceSelection("");
		
		fillContent();
		
		textPane.setEditable(false);
	}
	
	public void showError(String errorString) {
		textPane.setEditable(true);
		textPane.selectAll();
		textPane.replaceSelection("");
		insertRedHeading("Error!\n");
		insertRedText(errorString);
		textPane.setEditable(false);
	}
	
	public void setAllowSelections(boolean canSelect) {
		textPane.setEnabled(canSelect);
		textPane.setDisabledTextColor(textPane.getForeground());
	}
	
	public void insertHeading(String text) {
		textPane.insertHeading(text);
	}
	
	public void insertText(String text) {
		textPane.insertText(text);
	}
	
	public void insertBoldText(String text) {
		textPane.insertBoldText(text);
	}
		
	public void insertRedHeading(String text) {
		textPane.insertRedHeading(text);
	}
	
	public void insertRedText(String text) {
		textPane.insertRedText(text);
	}
	
	public void insertBoldRedText(String text) {
		textPane.insertBoldRedText(text);
	}
	
	public void insertBoldBlueText(String text) {
		textPane.insertBoldBlueText(text);
	}
	
	public void insertSubscript(String text) {
		textPane.insertSubscript(text);
	}
	
	public void insertSuperscript(String text) {
		textPane.insertSuperscript(text);
	}
	
	public void insertImage(String gifName) {
		textPane.insertImage(gifName);
	}
	
	public void insertFormula(MFormula formula) {
		textPane.insertFormula(formula);
	}
	
	public void insertEdit(XNumberEditPanel edit) {
		textPane.insertEdit(edit);
	}
	
	public void insertMenu(XChoice choice) {
		textPane.insertMenu(choice);
	}
	
	public void setAlignment(boolean leftNotCentre) {
		textPane.setAlignment(leftNotCentre);
	}
	
	public Dimension getPreferredSize() {
		if (hasBiggestContent()) {
			textPane.setEditable(true);
			textPane.selectAll();
			textPane.replaceSelection("");
			
			fillBiggestContent();
			
			textPane.setEditable(false);
		}
		Dimension size = super.getPreferredSize();
		if (hasBiggestContent())
			changeContent();
		return size;
	}
	
	public Dimension getMinimumSize() {
		if (hasBiggestContent()) {
			textPane.setEditable(true);
			textPane.selectAll();
			textPane.replaceSelection("");
			
			fillBiggestContent();
			
			textPane.setEditable(false);
		}
		Dimension size = super.getMinimumSize();
		if (hasBiggestContent())
			changeContent();
		return size;
	}
	
	abstract protected void fillContent();
	
	protected boolean hasBiggestContent() {
		return false;
	}
	
	protected void fillBiggestContent() {
	}
	
	
}