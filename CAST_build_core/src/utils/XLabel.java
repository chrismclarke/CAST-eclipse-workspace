package utils;

import java.awt.*;

import dataView.*;


public class XLabel extends XPanel {
	protected String theText;
	protected int alignment;
	private boolean isVisible = true;
	
	static protected final int kBorder = 2;
	static public final int CENTER = 0;
	static public final int LEFT = 1;
	static public final int RIGHT = 2;
	
	public XLabel(String theText, int alignment, XApplet applet) {
		this.theText = theText;
		this.alignment = alignment;
		setFont(applet.getStandardFont());
	}
	
	public void setText(String newText) {
		theText = newText;
		repaint();
	}
	
	public String getText() {
		return theText;
	}
	
	public void show(boolean showNotHide) {
					//	private version of show() since hidden component is not laid out
		if (isVisible != showNotHide) {
			isVisible = showNotHide;
			repaint();
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (theText == null)
			return;
		
		if (isVisible)
			drawLabel(g);
	}
	
	protected void drawLabel(Graphics g) {
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
		FontMetrics fm = g.getFontMetrics();
		
//		int startVert = kBorder + fm.getAscent();
		int startVert = (getSize().height + fm.getAscent() - fm.getDescent()) / 2;
		int startHoriz = kBorder;
		if (alignment != LEFT) {
			int labelWidth = fm.stringWidth(theText);
			if (alignment == CENTER)
				startHoriz = (getSize().width - labelWidth) / 2;
			else
				startHoriz = getSize().width - labelWidth;
		}
		
		g.drawString(theText, startHoriz, startVert);
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		g.setFont(getFont());
		FontMetrics fm = g.getFontMetrics();
		
		int theWidth = 2 * kBorder + fm.stringWidth(theText);
		int theHeight = 2 * kBorder + fm.getAscent() + fm.getDescent();
		
		return new Dimension(theWidth, theHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}