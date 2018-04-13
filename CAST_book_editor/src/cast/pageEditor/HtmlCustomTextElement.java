package cast.pageEditor;

import java.awt.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;


public class HtmlCustomTextElement extends CoreHtmlElement {
	private ArrayList<String> englishName = new ArrayList<String>();
	private ArrayList<JTextField> foreignEdit = new ArrayList<JTextField>();
	
	public HtmlCustomTextElement(String customText, JPanel parent) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new GridLayout(0, 2, 10, 4));
		
		Pattern thePattern = Pattern.compile("([^#]*)=([^#]*)");
		Matcher theMatcher = thePattern.matcher(customText);
		while (theMatcher.find()) {
			@SuppressWarnings("unused")
			String englishString = theMatcher.group(1);
			@SuppressWarnings("unused")
			String foreignString = theMatcher.group(2);
			String paramName = theMatcher.group(1);
			String paramValue = theMatcher.group(2);
			
			JTextField englishField = new JTextField(paramName);
			englishField.setFont(new Font("SansSerif", Font.BOLD, 12));
			englishField.setEditable(false);
			englishName.add(paramName);
			thePanel.add(englishField);
			
			JTextField foreignField = createMonitoredTextField(paramValue);
			foreignField.setFont(new Font("SansSerif", Font.BOLD, 12));
			foreignEdit.add(foreignField);
			thePanel.add(foreignField);
		}
			
		parent.add(thePanel);
	}
	
	public String getHtml() {
		String valueString = "";
		Iterator<String> nameIterator = englishName.iterator();
		Iterator<JTextField> editIterator = foreignEdit.iterator();
		
		while (nameIterator.hasNext() && editIterator.hasNext()) {
			if (valueString.length() > 0)
				valueString += "#";
			valueString += nameIterator.next() + "=" + editIterator.next().getText();
		}
		
		return "<param name=\"customText\" value=\"" + valueString + "\">";
	}
}
