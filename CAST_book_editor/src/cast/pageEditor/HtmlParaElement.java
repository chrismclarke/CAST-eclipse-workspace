package cast.pageEditor;

import java.awt.*;

import javax.swing.*;


public class HtmlParaElement extends CoreHtmlElement {
	static final private int kLineHeight = 15;
	static final private int kEditBorder = 4;
	
	private JTextArea paraEdit;
	
	public HtmlParaElement(String paraString, JPanel parent, int width) {
		paraString = paraString.replaceAll("\\s+", " ");
		paraString = paraString.replaceAll("<br>", "\n");
		
		paraEdit = createMonitoredTextArea(paraString);
		paraEdit.setFont(new Font("SansSerif", Font.BOLD, 12));
		paraEdit.setLineWrap(true);
		paraEdit.setWrapStyleWord(true);
		
		int approxLines = paraString.length() / 80 + 1;
		
		JScrollPane scrollPane = new JScrollPane(paraEdit);
		scrollPane.setPreferredSize(new Dimension(width, (approxLines + 1) * kLineHeight + 2 * kEditBorder));
		
		if (parent.getLayout() instanceof BorderLayout)
			parent.add("Center", scrollPane);
		else
			parent.add(scrollPane);
	}
	
	public HtmlParaElement(String paraString, JPanel parent) {
		this(paraString, parent, 600);
	}
	
	public String getHtml() {
		String paraString = paraEdit.getText();
		paraString = paraString.replaceAll("\n", "<br>");
		return paraString;
	}
}
