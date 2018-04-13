package ebook;

import java.awt.*;
import javax.swing.*;
//import javax.swing.border.*;

import ebookStructure.*;


public class TabPanel extends JPanel {
	
	private BookFrame theWindow;
	
	private OneTab tab[] = new OneTab[DomElement.kNoOfVersions];
	private TabSpacer spacer[] = new TabSpacer[DomElement.kNoOfVersions + 1];
	
	public TabPanel(BookFrame theWindowParam) {
		this.theWindow = theWindowParam;
		
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		
			JPanel actualTabPanel = new JPanel();
			actualTabPanel.setOpaque(false);
			actualTabPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 40));
			actualTabPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
//			Border tabLineBorder = BorderFactory.createMatteBorder(1, 1, 0, 1, OneTab.kTabBorderColor);
//			Border tabSpacingBorder = BorderFactory.createEmptyBorder(3, 8, 2, 8);
//			Border tabBorder = BorderFactory.createCompoundBorder(tabLineBorder, tabSpacingBorder);
			
				tab[DomElement.SUMMARY_VERSION] = new OneTab(theWindow.translate("Brief"), DomElement.SUMMARY_VERSION, theWindow);
				tab[DomElement.FULL_VERSION] = new OneTab(theWindow.translate("Full"), DomElement.FULL_VERSION, theWindow);
				tab[DomElement.VIDEO_VERSION] = new OneTab(theWindow.translate("Video"), DomElement.VIDEO_VERSION, theWindow);
			
			spacer[0] = new TabSpacer(null, tab[0]);
			actualTabPanel.add(spacer[0]);
			for (int i=0 ; i<tab.length ; i++) {
				actualTabPanel.add(tab[i]);
				spacer[i+1] = new TabSpacer(tab[i], i<tab.length-1 ? tab[i+1] : null);
				actualTabPanel.add(spacer[i+1]);
			}
			
		add("South", actualTabPanel);
	}
	
	public void updateTabs() {
		DomElement e = theWindow.currentElement;
		int currentVersion = theWindow.getEffectivePageVersion();
		
		boolean hasVersion[] = e.versionsAllowed();
		int nAllowed = 0;
		for (int i=0 ; i<hasVersion.length ; i++)
			if (hasVersion[i])
				nAllowed ++;
		if (nAllowed <= 1) {
			for (int i=0 ; i<tab.length ; i++)
				tab[i].setVisible(false);
			return;
		}
		
		for (int i=0 ; i<tab.length ; i++) {
			if (hasVersion[i]) {
				boolean isSelectedTab = (currentVersion == i);
				tab[i].setBackground(isSelectedTab ? OneTab.kSelectedBackground : OneTab.kDimBackground);
				tab[i].setForeground(isSelectedTab ? OneTab.kSelectedForeground : OneTab.kDimForeground);
//				tab[i].setCursor(Cursor.getPredefinedCursor(isSelectedTab ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
				tab[i].setVisible(true);
			}
			else
				tab[i].setVisible(false);
		}
		
		for (int i=0 ; i<spacer.length ; i++) {
			spacer[i].revalidate();
			spacer[i].repaint();
		}
	}
}