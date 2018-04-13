package cast.pageEditor;

import java.awt.*;

import javax.swing.*;


public class HtmlParamElement extends CoreHtmlElement {
	private String paramName;
	private JTextField valueEdit;
	
	public HtmlParamElement(String paramName, String paramValue, JPanel parent) {
		this.paramName = paramName;
		
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setPreferredSize(new Dimension(500, 30));
		
			JLabel nameLabel = new JLabel(paramName + " = ", JLabel.LEFT);
			valueEdit = createMonitoredTextField(paramValue);
			nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
			valueEdit.setFont(new Font("SansSerif", Font.BOLD, 12));
			nameLabel.setLabelFor(valueEdit);
			
		thePanel.add("West", nameLabel);
		thePanel.add("Center", valueEdit);
			
		parent.add(thePanel);
	}
	
	public String getHtml() {
		return "<param name=\"" + paramName + "\" value=\"" + valueEdit.getText() + "\">";
	}
}
