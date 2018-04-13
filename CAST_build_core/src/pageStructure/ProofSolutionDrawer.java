package pageStructure;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.*;

import utils.*;
import ebook.*;


public class ProofSolutionDrawer extends CoreDrawer {	
	private int blockType;
	
	private Color theoremBackgroundColor, proofBackgroundColor;
	private BookFrame theWindow;
	
	private JPanel proofButtonPanel;
	private JLabel proofLabel;
	private JPanel proofPanel;
	
	public ProofSolutionDrawer(String htmlString, String dirString, BookFrame theBookFrame,
																																						int blockType, Map namedApplets) {
		this.blockType = blockType;
		theoremBackgroundColor = getBackgroundColor(ExpandingBlockDrawer.getTheoremQuestionStyleSheet(blockType));
		StyleSheet proofSolutionStyleSheet = ExpandingBlockDrawer.getProofSolutionStyleSheet(blockType);
		proofBackgroundColor = getBackgroundColor(proofSolutionStyleSheet);
		theWindow = theBookFrame;
		
		addChild(new HtmlDrawer(htmlString, dirString, proofSolutionStyleSheet, theBookFrame, namedApplets));
	}
	
	private Color getButtonColor(boolean stdNotHit) {
		boolean proofVisible = proofPanel.isVisible();
		return (stdNotHit == proofVisible) ? proofBackgroundColor : theoremBackgroundColor;
	}
	
	private String proofSolutionString() {
		return theWindow.translate(blockType == ExpandingBlockDrawer.THEOREM ? "Proof" : "Solution");
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		thePanel.setOpaque(false);
		
			proofButtonPanel = new JPanel();
			proofButtonPanel.setBackground(theoremBackgroundColor);
			proofButtonPanel.setLayout(new BorderLayout(0, 0));
				Border overlineBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, ExpandingBlockDrawer.getBorderColor(blockType));
				Border innerSpacingBorder = BorderFactory.createEmptyBorder(2, 10, 2, 0);
			proofButtonPanel.setBorder(new CompoundBorder(overlineBorder, innerSpacingBorder));
				proofLabel = new JLabel(proofSolutionString() + " ...", JLabel.LEFT);
				Font proofHeadingFont = new Font(kSerifFontName, Font.BOLD|Font.ITALIC,
																								scaledSize(TheoremQuestionDrawer.kBaseTitleFontSize));
				proofLabel.setFont(proofHeadingFont);
			proofButtonPanel.add("Center", proofLabel);
			
			
			proofButtonPanel.addMouseListener(new MouseListener() {
								public void mouseReleased(MouseEvent e) {}
								public void mousePressed(MouseEvent e) {}
								public void mouseExited(MouseEvent e) {
									proofButtonPanel.setBackground(getButtonColor(true));
								}
								public void mouseEntered(MouseEvent e) {
									proofButtonPanel.setBackground(getButtonColor(false));
								}
								public void mouseClicked(MouseEvent e) {
									proofPanel.setVisible(!proofPanel.isVisible());
									proofButtonPanel.setBackground(getButtonColor(false));
								}
							});
			proofButtonPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		thePanel.add(proofButtonPanel);
		
			proofPanel = getChild(0).createPanel();
			proofPanel.setVisible(false);
		thePanel.add(proofPanel);
		
		return thePanel;
	}
}
