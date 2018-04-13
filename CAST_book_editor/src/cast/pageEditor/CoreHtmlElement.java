package cast.pageEditor;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;


abstract public class CoreHtmlElement {
	private List<CoreHtmlElement> htmlElements = null;
	
	private boolean changed = false;
	
	public String getHtml() {
		if (htmlElements == null)
			return "";
		else {
			String s = "";
			for (CoreHtmlElement element : htmlElements)
				s += element.getHtml();
			return s;
		}
	}
	
	public boolean hasChanged() {
		if (htmlElements != null)
			for (CoreHtmlElement element : htmlElements)
				if (element.hasChanged())
					return true;
		return changed;
	}
	
	protected void insertElement(CoreHtmlElement e) {
		if (htmlElements == null)
			htmlElements = new ArrayList<CoreHtmlElement>();
		htmlElements.add(e);
	}
	
	protected int findNestedEndIndex(String htmlString, String tag) {		//	assumes that initial tag has been removed
		int endDivIndex = htmlString.indexOf("</" + tag + ">");
		int startDivIndex = 0;
		while (true) {
			startDivIndex = htmlString.indexOf("<" + tag, startDivIndex);
			if (startDivIndex >= 0 && startDivIndex < endDivIndex) {
				startDivIndex += 4;
				endDivIndex = htmlString.indexOf("</" + tag + ">", endDivIndex + tag.length() + 3);
			}
			else
				break;
		}
		return endDivIndex + tag.length() + 3;
	}
	
	protected int minHitPos(int pos1, int pos2) {
		if (pos1 < 0)
			return pos2;
		else if (pos2 < 0)
			return pos1;
		else
			return Math.min(pos1, pos2);
	}
	
	protected int maxHitPos(int pos1, int pos2) {
		if (pos1 < 0)
			return pos2;
		else if (pos2 < 0)
			return pos1;
		else
			return Math.max(pos1, pos2);
	}
	
	protected JTextField createMonitoredTextField(String text) {
		JTextField theText = new JTextField(text, 0);
		theText.getDocument().addDocumentListener(new DocumentListener() {
													public void changedUpdate(DocumentEvent arg0) {
														changed = true;
													}
													
													public void insertUpdate(DocumentEvent arg0) {
														changed = true;
													}

													public void removeUpdate(DocumentEvent arg0) {
														changed = true;
													}
											});
		return theText;
	}
	
	protected JTextArea createMonitoredTextArea(String text) {
		JTextArea theArea = new JTextArea(text);
		theArea.setLineWrap(true);
		theArea.setWrapStyleWord(true);
		theArea.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
			
		theArea.getDocument().addDocumentListener(new DocumentListener() {
													public void changedUpdate(DocumentEvent arg0) {
														changed = true;
													}
													
													public void insertUpdate(DocumentEvent arg0) {
														changed = true;
													}

													public void removeUpdate(DocumentEvent arg0) {
														changed = true;
													}
											});
		return theArea;
	}
}
