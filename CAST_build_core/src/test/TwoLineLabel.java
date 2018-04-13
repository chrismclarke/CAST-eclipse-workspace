package test;

import java.awt.*;

import dataView.*;


public class TwoLineLabel extends XPanel {
	private String text1, text2;
	
	static private final int kBorder = 2;
	
	public TwoLineLabel(String text1, String text2, XApplet applet) {
		this.text1 = text1;
		this.text2 = text2;
		setFont(applet.getStandardFont());
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		FontMetrics fm = g.getFontMetrics();
		
		int startVert = kBorder + fm.getAscent();
		int startHoriz = kBorder;
		g.drawString(text1, startHoriz, startVert);
		g.drawString(text2, startHoriz, startVert + fm.getHeight());
	}
	
	public Dimension getMinimumSize() {
		FontMetrics fm = getGraphics().getFontMetrics();
		
		int theWidth = 2 * kBorder + Math.max(fm.stringWidth(text1), fm.stringWidth(text2));
		int theHeight = 2 * kBorder + fm.getAscent() + fm.getDescent() + fm.getHeight();
		
		return new Dimension(theWidth, theHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}