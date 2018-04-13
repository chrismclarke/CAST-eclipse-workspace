package ebook;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import pageStructure.*;


public class OneTab extends JLabel {
//	static final public Color kSelectedBackground = PageContentView.kBgColor;
	static final public Color kSelectedBackground = CoreDrawer.getBackgroundColor(CoreDrawer.getStyleSheet());
	static final public Color kSelectedForeground = Color.red;
	static final public Color kDimBackground = CoreBannerTitles.kBannerBackground;
	static final public Color kDimForeground = new Color(0x000066);
	
	static final public Color kTabBorderColor = CoreBannerTitles.kBorderColor;
	
	static final private Font kTabFont = new Font("Arial", Font.BOLD, 15);
	
	private int tabVersion;
	private BookFrame theWindow;
	
	public OneTab(String tabText, int tabVersionParam, BookFrame theWindowParam) {
		super(tabText, JLabel.CENTER);
		tabVersion = tabVersionParam;
		theWindow = theWindowParam;
		
		Border tabLineBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, kTabBorderColor);
		Border tabSpacingBorder = BorderFactory.createEmptyBorder(3, 8, 2, 8);
		Border tabBorder = BorderFactory.createCompoundBorder(tabLineBorder, tabSpacingBorder);
		
		setFont(kTabFont);
		setBorder(tabBorder);
		setOpaque(true);
		addMouseListener(new MouseListener() {
							public void mouseReleased(MouseEvent e) {}
							public void mousePressed(MouseEvent e) {}
							public void mouseExited(MouseEvent e) {
								JLabel hitTab = (JLabel)e.getComponent();
								boolean isSelectedTab = (tabVersion == theWindow.getEffectivePageVersion());
								if (!isSelectedTab)
									hitTab.setForeground(kDimForeground);
							}
							public void mouseEntered(MouseEvent e) {
								JLabel hitTab = (JLabel)e.getComponent();
								boolean isSelectedTab = (tabVersion == theWindow.getEffectivePageVersion());
								if (!isSelectedTab)
									hitTab.setForeground(kSelectedForeground);
							}
							public void mouseClicked(MouseEvent e) {
								if (tabVersion != theWindow.getEffectivePageVersion())
									theWindow.showPageVersion(tabVersion);
							}
						});
	}
}