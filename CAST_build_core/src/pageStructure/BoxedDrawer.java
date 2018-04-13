package pageStructure;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import utils.*;
import ebook.*;


public class BoxedDrawer extends CoreDrawer {
	public BoxedDrawer(String htmlString, String dirString, BookFrame theBookFrame, Map namedApplets) {
		addChild(new HtmlDrawer(htmlString, dirString, getBoxedStyleSheet(), theBookFrame, namedApplets));
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new ProportionLayout(0.125, 0));
		thePanel.setOpaque(false);
		
			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new ProportionLayout(0.85714, 0));
			rightPanel.setOpaque(false);
			
				JPanel contentPanel = getChild(0).createPanel();
				Border boxBorder = BorderFactory.createLineBorder(Color.black);
				Border rightInsideSpacingBorder = BorderFactory.createEmptyBorder(0, 0, 0, 16);
				contentPanel.setBorder(new CompoundBorder(boxBorder, rightInsideSpacingBorder));
				
			rightPanel.add(ProportionLayout.LEFT, contentPanel);
			
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		return thePanel;
	}
}
