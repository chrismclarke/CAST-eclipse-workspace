package exercise2;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

import utils.*;
import formula.*;
import images.*;


public class MessageTextPane extends JTextPane {
	static final private Color kDarkBlue = new Color(0x000099);
	
	private Style textStyle, textBoldStyle, headingStyle, subStyle, superStyle;
	private Style textRedStyle, textBoldRedStyle, headingRedStyle;
	private Style textBoldBlueStyle;
	private Style leftParaStyle, centredParaStyle;
	
	public MessageTextPane(int baseFontSize, String fontName, float aboveBelowPixels) {
		setBackground(Color.white);
		
		StyledDocument doc = (StyledDocument)getDocument();
		
		leftParaStyle = doc.addStyle("leftParaStyle", null);
		StyleConstants.setSpaceAbove(leftParaStyle, aboveBelowPixels);
		StyleConstants.setSpaceBelow(leftParaStyle, aboveBelowPixels);
		StyleConstants.setLeftIndent(leftParaStyle, 8.0f);
		StyleConstants.setRightIndent(leftParaStyle, 8.0f);
		
		centredParaStyle = doc.addStyle("centredParaStyle", leftParaStyle);
		StyleConstants.setAlignment(centredParaStyle, StyleConstants.ALIGN_CENTER);
		 
		textStyle = doc.addStyle("textStyle", leftParaStyle);
		StyleConstants.setFontFamily(textStyle, fontName);
		StyleConstants.setFontSize(textStyle, baseFontSize);
		
		textRedStyle = doc.addStyle("textRedStyle", textStyle);
		StyleConstants.setForeground(textRedStyle, Color.red);
		
		textBoldRedStyle = doc.addStyle("textBoldRedStyle", textRedStyle);
		StyleConstants.setBold(textBoldRedStyle, true);
		
		textBoldBlueStyle = doc.addStyle("textBoldBlueStyle", textStyle);
		StyleConstants.setForeground(textBoldBlueStyle, kDarkBlue);
		StyleConstants.setBold(textBoldBlueStyle, true);
		
		textBoldStyle = doc.addStyle("textBoldStyle", textStyle);
		StyleConstants.setBold(textBoldStyle, true);
		
		headingStyle = doc.addStyle("headingStyle", textStyle);
		StyleConstants.setFontSize(headingStyle, 18);
		StyleConstants.setBold(headingStyle, true);
		
		headingRedStyle = doc.addStyle("headingRedStyle", headingStyle);
		StyleConstants.setForeground(headingRedStyle, Color.red);
		
		subStyle = doc.addStyle("subStyle", textStyle);
		StyleConstants.setSubscript(subStyle, true);
		
		superStyle = doc.addStyle("superStyle", textStyle);
		StyleConstants.setSuperscript(superStyle, true);
		
		setParagraphAttributes(leftParaStyle, true);
		setEditable(false);
	}
	
	public MessageTextPane(int baseFontSize) {
		this(baseFontSize, "SansSerif", 5.0f);
	}

	private void setSelectionToStart() {
//			StyledDocument doc = (StyledDocument)getDocument();
		setCaretPosition(0);
	}

	private void setSelectionToEnd() {
		StyledDocument doc = (StyledDocument)getDocument();
		setCaretPosition(doc.getLength());
	}
	
	private void insert(String text, Style style) {
		setSelectionToEnd();
		setCharacterAttributes(style, true);
		try {
			replaceSelection(MText.expandText(text));
		} catch (Exception e) {
			throw new RuntimeException("Error displaying page in MessagePanel: " + e);
		}
		setSelectionToStart();
	}
	
	public void insertHeading(String text) {
		insert(text, headingStyle);
	}
	
	public void insertText(String text) {
		insert(text, textStyle);
	}
	
	public void insertBoldText(String text) {
		insert(text, textBoldStyle);
	}
	
	public void insertRedHeading(String text) {
		insert(text, headingRedStyle);
	}
	
	public void insertRedText(String text) {
		insert(text, textRedStyle);
	}
	
	public void insertBoldRedText(String text) {
		insert(text, textBoldRedStyle);
	}
	
	public void insertBoldBlueText(String text) {
		insert(text, textBoldBlueStyle);
	}
	
	public void insertSubscript(String text) {
		insert(text, subStyle);
	}
	
	public void insertSuperscript(String text) {
		insert(text, superStyle);
	}
	
	public void insertImage(String gifName) {
		setSelectionToEnd();
		Image img = CoreImageReader.getImage(gifName);
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(img, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image");
		}
		
		try {
			insertIcon(new ImageIcon(img));
		} catch (Exception e) {
			throw new RuntimeException("Error displaying page in MessagePanel: " + e);
		}
		setSelectionToStart();
	}
	
	public void insertFormula(MFormula formula) {
		setSelectionToEnd();
		try {
			insertComponent(formula);
		} catch (Exception e) {
			throw new RuntimeException("Error displaying page in MessagePanel: " + e);
		}
		setSelectionToStart();
	}
	
	public void insertEdit(XNumberEditPanel edit) {
		setSelectionToEnd();
		try {
			insertComponent(edit);
			edit.setAlignmentY(0.7f);
		} catch (Exception e) {
			throw new RuntimeException("Error displaying page in MessagePanel: " + e);
		}
		setSelectionToStart();
	}
	
	public void insertMenu(XChoice choice) {
		setSelectionToEnd();
		try {
			insertComponent(choice);
		} catch (Exception e) {
			throw new RuntimeException("Error displaying page in MessagePanel: " + e);
		}
		setSelectionToStart();
	}
	
	public void setAlignment(boolean leftNotCentre) {
		setSelectionToEnd();
		setParagraphAttributes(leftNotCentre ? leftParaStyle : centredParaStyle, true);
		setSelectionToStart();
	}
}