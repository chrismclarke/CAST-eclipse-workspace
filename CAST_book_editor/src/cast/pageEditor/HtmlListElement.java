package cast.pageEditor;

import java.awt.*;

import javax.swing.*;

import cast.utils.*;


public class HtmlListElement extends CoreHtmlElement {
	static final private int kBulletWidth = 20;
	static final private int kBulletHeight = 20;
//	static final private int kBulletDiameter = 10;
	
	private int elementIndex = 0;
	
	public HtmlListElement(String listString, JPanel parent, final boolean withNumbers) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
		
		while (true) {
			int firstElementIndex = listString.indexOf("<li");
			if (firstElementIndex < 0)
				break;
			
			elementIndex ++;
			
			JPanel elementPanel = new JPanel();
			elementPanel.setOpaque(false);
			elementPanel.setLayout(new BorderLayout(10, 0));
			
			firstElementIndex = listString.indexOf(">", firstElementIndex) + 1;
			insertElement(new HtmlStringElement(listString.substring(0, firstElementIndex)));
			listString = listString.substring(firstElementIndex);
			int endIndex = findNestedEndIndex(listString, "li") - 5;			//	steps back to start of </li>
			String content = listString.substring(0, endIndex);
			
			insertElement(new HtmlParaElement(content, elementPanel, 520));
			
		
			JPanel bulletCanvas = new JPanel() {
															public void paintComponent(Graphics g) {
																g.setColor(Color.black);
																g.setFont(new Font("SansSerif", Font.BOLD, 18));
																String label = withNumbers ? String.valueOf(elementIndex) : "\u2022";
																g.drawString(label, (getWidth() - g.getFontMetrics().stringWidth(label)) / 2, getHeight() / 2 - 4);
															}
															
															public Dimension getPreferredSize() {
																return new Dimension(kBulletWidth, kBulletHeight);
															}
														};
		
			elementPanel.add("West", bulletCanvas);
			
			listString = listString.substring(endIndex);
			
			thePanel.add(elementPanel);
		}
		
		if (listString.length() > 0)
			insertElement(new HtmlStringElement(listString));
		
		parent.add(thePanel);
	}
}
