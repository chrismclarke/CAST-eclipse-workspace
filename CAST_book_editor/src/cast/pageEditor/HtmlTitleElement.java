package cast.pageEditor;

import java.awt.*;

import javax.swing.*;


public class HtmlTitleElement extends CoreHtmlElement {
	private JTextField titleEdit;
	
	public HtmlTitleElement(String htmlString, JPanel parent, String label) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new BorderLayout(10, 0));
			JLabel titleLabel = new JLabel(label, JLabel.LEFT);
			titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
			titleLabel.setForeground(Color.red);
			titleEdit = createMonitoredTextField(htmlString);
			titleEdit.setFont(new Font("SansSerif", Font.BOLD, 16));
			titleEdit.setForeground(Color.red);
			titleLabel.setLabelFor(titleEdit);
		thePanel.add("West", titleLabel);
		thePanel.add("Center", titleEdit);
		
		parent.add(thePanel);
	}
	
	public String getHtml() {
		return titleEdit.getText();
	}
}
