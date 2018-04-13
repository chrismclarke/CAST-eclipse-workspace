package ebook;

import java.awt.*;
import javax.swing.*;


abstract public class CoreBannerTitles extends JPanel {
	static final public Color kBannerBackground = new Color(0xEEEEEE);
	static final public Color kGreyTextColor = new Color(0x999999);
	static final public Color kBlackTextColor = new Color(0x000000);
	static final public Color kBorderColor = new Color(0xBBBBBB);
	
	protected BookFrame theWindow;
	
	public CoreBannerTitles(BookFrame theWindowParam) {
		this.theWindow = theWindowParam;
		
		setBackground(kBannerBackground);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CoreBannerTitles.kBorderColor));
	}
	
	abstract public void updateTitles();
	
	private boolean firstDraw = true;
	public void paintComponent(Graphics g) {
		if (firstDraw) {						//	banner does not seem to get laid out when first drawn
			firstDraw = false;
			invalidate();
		}
		else
			super.paintComponent(g);
	}
}