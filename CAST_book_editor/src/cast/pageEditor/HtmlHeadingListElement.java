package cast.pageEditor;

import java.awt.*;

import javax.swing.*;

import cast.utils.*;


public class HtmlHeadingListElement extends CoreHtmlElement {
//	static final private int kBulletWidth = 20;
//	static final private int kBulletHeight = 20;
//	static final private int kBulletDiameter = 10;
	
	public HtmlHeadingListElement(String listString, JPanel parent, final boolean withNumbers) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
		
		while (true) {
			int firstDdIndex = listString.indexOf("<dd");
			int firstDtIndex = listString.indexOf("<dt");
			int firstElementIndex = minHitPos(firstDdIndex, firstDtIndex);
			
			if (firstElementIndex < 0)
				break;
			
			boolean isHeading = (firstElementIndex == firstDdIndex);
			firstElementIndex = listString.indexOf(">", firstElementIndex) + 1;
			insertElement(new HtmlStringElement(listString.substring(0, firstElementIndex)));
			listString = listString.substring(firstElementIndex);
			
			JPanel elementPanel = new JPanel();
			elementPanel.setOpaque(false);
			elementPanel.setLayout(new BorderLayout(0, 0));
			
			int endIndex;
			if (isHeading) {
				endIndex = findNestedEndIndex(listString, "dd") - 5;			//	steps back to start of </dd>
				String content = listString.substring(0, endIndex);
				
				elementPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
				insertElement(new HtmlParaElement(content, elementPanel));
			}
			else {
				endIndex = findNestedEndIndex(listString, "dt") - 5;			//	steps back to start of </dt>
				String content = listString.substring(0, endIndex);
				
				elementPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
				insertElement(new HtmlHeadingElement(content, elementPanel));
			}
			
			listString = listString.substring(endIndex);
			
			thePanel.add(elementPanel);
		}
		
		if (listString.length() > 0)
			insertElement(new HtmlStringElement(listString));
		
		parent.add(thePanel);
	}
}
