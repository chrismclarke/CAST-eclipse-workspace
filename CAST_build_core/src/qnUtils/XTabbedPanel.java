package qnUtils;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import dataView.*;
import utils.*;
import exercise2.*;

public class XTabbedPanel extends InsetPanel {
	static final private String[] kPartName = {"(a)", "(b)", "(c)", "(d)", "(e)", "(f)", "(g)"};
	
	private XTab[] button;
//	private ButtonGroup group;
	private JTextArea message;
	
	private String[] tabString;
	
	private class XTab extends JToggleButton {
		static final private int kLeftRightBorder = 12;
		static final private int kTopBottomBorder = 2;
		
		private int ascent, descent, labelWidth;
		
		XTab(String label) {
			super(label);
		}
		
		public void paintComponent(Graphics g) {
			if (isSelected()) {
				g.setColor(Color.white);
				g.fillRect(0, 0, getSize().width, getSize().height);
				g.setColor(Color.black);
				g.drawString(getText(), (getSize().width - labelWidth) / 2, kTopBottomBorder + ascent);
			}
			else {
				g.setColor(Color.lightGray);
				g.fillRect(0, 0, getSize().width, getSize().height);
				g.setColor(Color.gray);
				g.drawString(getText(), (getSize().width - labelWidth) / 2, kTopBottomBorder + ascent);
			}
		}
		
		public Dimension getMinimumSize() {
			Graphics g = getGraphics();
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			labelWidth = fm.stringWidth(getText());
			
			return new Dimension(labelWidth + 2 * kLeftRightBorder, ascent + descent + kTopBottomBorder);
		}
		
		public Dimension getPreferredSize() {
			return getMinimumSize();
		}
	}
	
	public XTabbedPanel(String title, int nTabs, int nTextLines, final ExercisePartsApplet applet) {
		super(5, 0);		

		setLayout(new BorderLayout(0, -2));
		
			XPanel tabPanel = new XPanel();
			tabPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			tabPanel.setOpaque(false);
			
				XLabel titleLabel = new XLabel(title, XLabel.LEFT, applet);
				titleLabel.setFont(applet.getStandardBoldFont());
				titleLabel.setOpaque(false);
			tabPanel.add(titleLabel);
			
			ButtonGroup group = new ButtonGroup();
			button = new XTab[nTabs];
			for (int i=0 ; i<nTabs ; i++) {
				button[i] = new XTab(kPartName[i]);
				tabPanel.add(button[i]);
				group.add(button[i]);
				final int buttonIndex = i;
				button[i].addChangeListener(new ChangeListener() {
																		public void stateChanged(ChangeEvent e) {
																			doChangedTab(buttonIndex, applet);
																		}
													});
			}
			
		add("North", tabPanel);
		
			message = new JTextArea(null, nTextLines, 0);
			
			Border emptyBorder = new EmptyBorder(4, 8, 4, 8);
			Border blackline = BorderFactory.createLineBorder(Color.gray);
			message.setBorder(new CompoundBorder(blackline, emptyBorder));
			message.setBackground(Color.white);
		add("Center", message);

		setComponentZOrder(message, 1);
		setComponentZOrder(tabPanel, 0);
	}
	
//================================================
	
	protected class TabTextPanel extends MessagePanel {
		public TabTextPanel(ExerciseApplet exerciseApplet) {
			super(null, exerciseApplet, MessagePanel.NO_SCROLL);
			changeContent();
		}
		
		protected void fillContent() {
//			insertMessageContent(this);
		}
	
		protected boolean hasBiggestContent() {
			return false;
		}
	}
	
//================================================
	
	
	public void setTabStrings(String[] tabString) {
		this.tabString = tabString;
		button[0].doClick();
	}
	
	protected void doChangedTab(int buttonIndex, ExercisePartsApplet applet) {
		message.setText(tabString[buttonIndex]);
		applet.noteChangedTab(buttonIndex);
	}
}