package cast.pageEditor;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;


public class HtmlTableElement extends CoreHtmlElement {
	static final private Color kTableBorderColor = new Color(0x666666);
	
	static final private int kLineHeight = 15;
	static final private int kEditBorder = 4;
	
	private JTextArea tableEdit;
	private String startText = "", endText = "";
	
	public HtmlTableElement(String tableString, JPanel parent) {
//		System.out.println("*****Table:\n" + tableString);
		
		tableString = tableString.replaceAll("\\s+", " ");
		int firstTdIndex = tableString.indexOf("<td");
		int firstThIndex = tableString.indexOf("<th");
		int firstHitPos = minHitPos(firstTdIndex, firstThIndex);
		
		int lastTdIndex = tableString.lastIndexOf("<td");
		int lastThIndex = tableString.lastIndexOf("<th");
		int lastHitPos = maxHitPos(lastTdIndex, lastThIndex);
		
		if (tableString.indexOf("<tr") == tableString.lastIndexOf("<tr")
																						&& firstHitPos == lastHitPos) {		//	single cell
			int startTdIndex = tableString.indexOf("<td");
			if (startTdIndex < 0)
				startTdIndex = tableString.indexOf("<th");
			startTdIndex = tableString.indexOf(">", startTdIndex) + 1;
			int endTdIndex = tableString.indexOf("</td>");
			if (endTdIndex < 0)
				endTdIndex = tableString.indexOf("</th>");
			startText = tableString.substring(0, startTdIndex);
			endText = tableString.substring(endTdIndex);
			tableString = tableString.substring(startTdIndex, endTdIndex);
		}
		
		tableEdit = createMonitoredTextArea(tableString);
		tableEdit.setFont(new Font("SansSerif", Font.BOLD, 12));
		
		int approxLines = tableString.length() / 80 + 1;
		
		JScrollPane scrollPane = new JScrollPane(tableEdit);
		scrollPane.setPreferredSize(new Dimension(600, (approxLines + 1) * kLineHeight + 2 * kEditBorder));
		
		Border tableBorder = BorderFactory.createLineBorder(kTableBorderColor, 3);
		scrollPane.setBorder(tableBorder);
		
		parent.add(scrollPane);
	}
	
	public String getHtml() {
		return startText + tableEdit.getText() + endText;
	}
}
