package cast.variationEditor;

import java.awt.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;



class HighlightingStyledDocument extends DefaultStyledDocument {
	private SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
	private SimpleAttributeSet paramStyle = new SimpleAttributeSet();
	
	private Pattern paramReg;					//	Regular expression to detect parameters
	private Pattern anonReg;					//	Regular expression to detect anonymous variables
	
	HighlightingStyledDocument(Vector validParams) {
		super();
		compileParams(validParams);
		
    defaultStyle = new SimpleAttributeSet();
		
    paramStyle = new SimpleAttributeSet();
    paramStyle.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.red);
    paramStyle.addAttribute(StyleConstants.CharacterConstants.Background, Color.yellow);
		
//    StyleContext sc = StyleContext.getDefaultStyleContext();
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setSpaceBelow(sas, 7);
		setParagraphAttributes(0, getLength(), sas, false);
	}
	
	private void compileParams(Vector validParams) {
		String paramList = "(";
		for (int i = 0; i < validParams.size(); i++) {
			if (i == 0)
				paramList += ((String)validParams.elementAt(i)).trim();
			else
				paramList += "|" + ((String)validParams.elementAt(i)).trim();
		}
		paramList += ")";
		
		String exp = "#" + paramList + "(\\[" + paramList + "\\])?#";
		paramReg = Pattern.compile(exp);
		
		anonReg = Pattern.compile("#anon_\\d(\\d)*#");
	}
	
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offs, str, a);
		updateHighlightingInRange(offs, str.length());
	}
	
	protected void fireRemoveUpdate(DocumentEvent e) {
		int offset = e.getOffset();
//		int length = e.getLength();
		updateHighlightingInRange(offset - 1, 0);
		super.fireRemoveUpdate(e);
	}
	
	public void updateHighlightingInRange(int offset, int length) {
		try {
			Element defaultElement = getDefaultRootElement();
			int line = defaultElement.getElementIndex(offset);
			int lineend = defaultElement.getElementIndex(offset + length);
			int start = defaultElement.getElement(line).getStartOffset();
			int end = defaultElement.getElement(lineend).getEndOffset();
			
			String text = getText(start, end - start);
			setCharacterAttributes(start, end - start, defaultStyle, true);
			
			Matcher m = paramReg.matcher(text);
			while (m.find())
				setCharacterAttributes(start + m.start(), m.end() - m.start(), paramStyle, true);
			
			m = anonReg.matcher(text);
			while (m.find())
				setCharacterAttributes(start + m.start(), m.end() - m.start(), paramStyle, true);
		}
		catch( Exception e){}
	}
	
	public void setParamStyle(SimpleAttributeSet style) {
		paramStyle = style;
	}
	
	public void setDefaultStyle(SimpleAttributeSet style) {
		defaultStyle = style;
	}
}
	
//===============================================================================


public class QuestionPanel extends JTextPane {
	
	static HighlightingStyledDocument createDocument(Vector validParams) {
		return new HighlightingStyledDocument(validParams);
	}
	
	public QuestionPanel(Vector validParams) {
		super(createDocument(validParams));
		
		setBackground(Color.white);
		setEditable(true);
	}
	
}