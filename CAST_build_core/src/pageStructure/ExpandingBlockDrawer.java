package pageStructure;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.*;

import dataView.DataView;
import ebook.*;


public class ExpandingBlockDrawer extends HtmlDrawer {
	static final public int THEOREM = 0;
	static final public int EXERCISE = 1;
	
	
	static public StyleSheet getTheoremQuestionStyleSheet(int blockType) {
		return (blockType == THEOREM) ? getTheoremStyleSheet() : getQuestionStyleSheet();
	}
	
	static public StyleSheet getProofSolutionStyleSheet(int blockType) {
		return (blockType == THEOREM) ? getProofStyleSheet() : getSolutionStyleSheet();
	}
	
	static public Color getBorderColor(int blockType) {
		Color theoremBackgroundColor = getBackgroundColor(getTheoremQuestionStyleSheet(blockType));
		return DataView.mixColors(theoremBackgroundColor, Color.black, 0.7);
	}
		
	private int blockType;
	
	public ExpandingBlockDrawer(String htmlString, String dirString, StyleSheet theStyleSheet,
																										BookFrame theBookFrame, int blockType, Map namedApplets) {
		super(htmlString, dirString, theStyleSheet, theBookFrame, namedApplets);
		this.blockType = blockType;
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = super.createPanel();
					
			Border outerBorder = BorderFactory.createEmptyBorder(0, 30, 0, 30);
			Border innerBorder = BorderFactory.createLineBorder(getBorderColor(blockType));
		thePanel.setBorder(new CompoundBorder(outerBorder, innerBorder));
		
		return thePanel;
	}
}
