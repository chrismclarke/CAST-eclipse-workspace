package time;

import java.awt.*;

import dataView.*;


public class SmoothingKey extends DataView {
	static private final int kTopBottomSpacing = 2;
	static private final int kLineLength = 40;
	static private final int kLineTextGap = 5;
	static private final int kBorder = 2;
	
//	static public final String SMOOTH_KEY = "smoothKey";
	
	private String[] variableKey;
	private TimeView mainView;
	private boolean[] draw;
	
	private int ascent, descent, itemHeight, textOffset, maxStringWidth;
	private Dimension bestDimension;
	
	private boolean initialised = false;
	
	public SmoothingKey(DataSet theData, String[] variableKey, XApplet applet, TimeView mainView) {
		super(theData, applet, null);
		this.variableKey = variableKey;
		this.mainView = mainView;
		draw = new boolean[variableKey.length];
	}
	
	public void setKeyDraw(boolean[] draw) {
		this.draw = draw;
		repaint();
	}
	
	private boolean initialise() {
		if (initialised)
			return false;
		
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
		
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		itemHeight = ascent + descent + 2 * kTopBottomSpacing;
		textOffset = (itemHeight + fm.getAscent() - fm.getDescent()) / 2;
		
		maxStringWidth = 0;
		for (int i=0 ; i<variableKey.length ; i++) {
			CoreVariable v = getVariable(variableKey[i]);
			maxStringWidth = Math.max(maxStringWidth, fm.stringWidth(v.name));
		}
		
		bestDimension = new Dimension(2 * kBorder + kLineLength + kLineTextGap + maxStringWidth,
																					2 * kBorder + variableKey.length * itemHeight);
		initialised = true;
		return true;
	}
	
	protected void drawOneSmoother(Graphics g, String name, int left, int top,
																																				Color lineColor) {
		g.setColor(lineColor);
		g.drawLine(left, top + itemHeight / 2, left + kLineLength, top + itemHeight / 2);
		g.drawString(name, left + kLineLength + kLineTextGap, top + textOffset);
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		int noOfKeys = 0;
		for (int i=0 ; i<draw.length ; i++)
			if (draw[i])
				noOfKeys++;
		
		int heightUsed = 2 * kBorder + noOfKeys * itemHeight;
		int top = (getSize().height - heightUsed) / 2;
		int left = (getSize().width - bestDimension.width) / 2;
		
		g.drawRect(left, top, bestDimension.width - 1, heightUsed - 1);
		g.setColor(Color.white);
		g.fillRect(left + 1, top + 1, bestDimension.width - 2, heightUsed - 2);
		
		top += kBorder;
		left += kBorder;
		int colorIndex = 0;
		for (int i=0 ; i<draw.length ; i++)
			if (draw[i]) {
				drawOneSmoother(g, getVariable(variableKey[i]).name, left, top,
																														mainView.getLineColor(colorIndex));
				colorIndex ++;
				top += itemHeight;
			}
	}
	
	public Dimension getMinimumSize() {
		initialise();
		return bestDimension;
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeSelection(Graphics g) {
	}
	
	protected void doChangeValue(Graphics g, int index) {
	}
	
	protected void doChangeVariable(Graphics g, String key) {
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
