package cast.utils;

import java.awt.*;

import javax.swing.*;


public class Separator extends JPanel {
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL = 1;
	
	private double proportion;
	private int minWidth = 200;
	private int minHeight = 50;
	private int spacing;
	private int orientation;
	
	public Separator(double proportion, int spacing, int orientation) {
		this.proportion = proportion;
		this.spacing = spacing;
		this.orientation = orientation;
		setOpaque(false);
	}
	
	public Separator(double proportion, int spacing) {
		this(proportion, spacing, HORIZONTAL);
	}
	
	public Separator(double proportion) {
		this(proportion, 2);				//		zero proprtion --> no line
	}
	
	public void setMinWidth(int minWidth) {
		this.minWidth = minWidth;
	}
	
	public void setMinHeight(int minHeight) {
		this.minHeight = minHeight;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (proportion <= 0.0)
			return;
		if (orientation == HORIZONTAL) {
			int startX = (int)Math.round(getSize().width * (1.0 - proportion)) / 2;
			int endX = getSize().width - startX;
			int startY = (getSize().height - 2) / 2;
			g.setColor(Color.white);
			g.drawLine(startX, startY, endX - 2, startY);
			g.setColor(getForeground());
			g.drawLine(startX + 1, startY + 1, endX - 1, startY + 1);
		}
		else {
			int startY = (int)Math.round(getSize().height * (1.0 - proportion)) / 2;
			int endY = getSize().height - startY;
			int startX = (getSize().width - 2) / 2;
			g.setColor(Color.white);
			g.drawLine(startX, startY, startX, endY - 2);
			g.setColor(getForeground());
			g.drawLine(startX + 1, startY + 1, startX + 1, endY - 1);
		}
	}
	
	public Dimension getMinimumSize() {
		return (orientation == HORIZONTAL) ? new Dimension(minWidth, 2 + 2 * spacing)
																			: new Dimension(2 + 2 * spacing, minHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}