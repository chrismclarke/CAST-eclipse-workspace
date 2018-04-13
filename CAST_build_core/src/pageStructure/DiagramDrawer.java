package pageStructure;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import ebook.*;


public class DiagramDrawer extends CoreDrawer {
	static final private Color kBorderColor = new Color(0xAAAAAA);
	static final private int kLeftRightBorder = 30;
	
	public DiagramDrawer(String htmlString, String dirString, BookFrame theBookFrame, Map namedApplets) {
		addChild(new HtmlDrawer(htmlString, dirString, getDiagramStyleSheet(), theBookFrame, namedApplets));
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setOpaque(false);
			
			JPanel contentPanel = getChild(0).createPanel();
			
		thePanel.add("Center", contentPanel);
			
			Border outerBorder = BorderFactory.createEmptyBorder(0, kLeftRightBorder, 0, kLeftRightBorder);
			Border lineBorder = BorderFactory.createLineBorder(kBorderColor);
			Border innerBorder = BorderFactory.createMatteBorder(0, 0, 20, 0, contentPanel.getBackground());
		thePanel.setBorder(new CompoundBorder(outerBorder, new CompoundBorder(lineBorder, innerBorder)));
		
		return thePanel;
	}
	
	
	public int getMinimumWidth() {
		int minWidth = super.getMinimumWidth();
		return minWidth + 2 * kLeftRightBorder + 2;
	}
}
