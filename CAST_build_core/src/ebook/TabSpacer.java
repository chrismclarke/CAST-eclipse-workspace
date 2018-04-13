package ebook;

import java.awt.*;
import javax.swing.*;


public class TabSpacer extends JPanel {
	static final private int kSpacerWidth = 6;
	
	private OneTab leftTab, rightTab;
	
	public TabSpacer(OneTab leftTab, OneTab rightTab) {
		this.leftTab = leftTab;
		this.rightTab = rightTab;
		setOpaque(false);
	}
	
	public void paintComponent(Graphics g) {
		if (getSize().width == 0)
			return;
		Graphics2D g2 = (Graphics2D)g;
		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
																										RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHints(hints);
		
		if (leftTab != null && leftTab.isVisible() && leftTab.getBackground() == OneTab.kDimBackground)
			drawLeftTriangle(OneTab.kDimBackground, g);
		if (rightTab != null && rightTab.isVisible() && rightTab.getBackground() == OneTab.kDimBackground)
			drawRightTriangle(OneTab.kDimBackground, g);
			
		if (leftTab != null && leftTab.isVisible() && leftTab.getBackground() == OneTab.kSelectedBackground)
			drawLeftTriangle(OneTab.kSelectedBackground, g);
		if (rightTab != null && rightTab.isVisible() && rightTab.getBackground() == OneTab.kSelectedBackground)
			drawRightTriangle(OneTab.kSelectedBackground, g);
	}
	
	private void drawLeftTriangle(Color c, Graphics g) {
		int height = getSize().height;
		g.setColor(c);
		int[] xCoord = {0, 0, kSpacerWidth - 1};
		int[] yCoord = {0, height, height};
		g.fillPolygon(xCoord, yCoord, 3);
		g.setColor(OneTab.kTabBorderColor);
		g.drawLine(0, 0, kSpacerWidth - 1, height);
	}
	
	private void drawRightTriangle(Color c, Graphics g) {
		int height = getSize().height;
		g.setColor(c);
		int[] xCoord = {kSpacerWidth, kSpacerWidth, 0};
		int[] yCoord = {0, height, height};
		g.fillPolygon(xCoord, yCoord, 3);
		g.setColor(OneTab.kTabBorderColor);
		g.drawLine(kSpacerWidth - 1, 0, 0, height);
	}
	
	public Dimension getMinimumSize() {
		boolean leftVisible = leftTab != null && leftTab.isVisible();
		boolean rightVisible = rightTab != null && rightTab.isVisible();
		if (!leftVisible && !rightVisible)
			return new Dimension(0, 0);
		else
			return new Dimension(kSpacerWidth, getTabHeight());
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	private int getTabHeight() {
		OneTab aTab = (leftTab == null) ? rightTab : leftTab;
		return aTab.getPreferredSize().height;
	}
}