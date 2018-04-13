package cast.pageEditor;

import java.awt.*;

import javax.swing.*;


public class HtmlHeadingElement extends CoreHtmlElement {
	static final private Color kHeadingColor = Color.blue;
	
	private JTextField headingEdit;
	
	public HtmlHeadingElement(String headingString, JPanel parent) {
		headingEdit = createMonitoredTextField(headingString.replace("&amp;", "&"));
		headingEdit.setForeground(kHeadingColor);
		headingEdit.setFont(new Font("SansSerif", Font.BOLD, 16));
		parent.add(headingEdit);
	}
	
	public String getHtml() {
		return headingEdit.getText().replace("&", "&amp;");
	}
}
