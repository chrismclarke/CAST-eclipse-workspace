package map;

import java.awt.*;
import java.util.*;

import dataView.*;


public class NumKeyView extends DataView {
//	static final public String NUMKEY = "numKey";
	
	static final private int kTopBottomBorder = 2;
	static final private int kLeftRightBorder = 4;
	static final private int kKeyRectWidth = 18;
	static final private int kKeyRectHeight = 100;
	static final private int kRectValueGap = 3;
	
	static final public Color mixColors(double p1, Color[] c) {
		if (p1 > 1.0)
			p1 = 1.0;
		else if (p1 < 0.0)
			p1 = 0.0;
		if (c.length == 2)
			return mixColors(c[1], c[0], p1);
		else if (p1 < 0.5)
			return mixColors(c[1], c[0], p1 * 2);
		else
			return mixColors(c[2], c[1], (p1 - 0.5) * 2);
 	}
 	
	private String minString, maxString;
	private double min, max;
	
	protected int ascent;
	protected int valueWidth, tableHeight;
	
	private Color numColour[];
	
	private boolean initialised = false;
	
	public NumKeyView(DataSet theData, XApplet applet, String minString, String maxString, Color[] numColour) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.minString = minString;
		this.maxString = maxString;
		this.numColour = numColour;
		StringTokenizer st = new StringTokenizer(minString);
		min = Double.parseDouble(st.nextToken());
		st = new StringTokenizer(maxString);
		max = Double.parseDouble(st.nextToken());
	}
	
	public Color findColor(double value) {
		double p = Math.min(1.0, Math.max(0.0, (value - min) / (max - min)));
		return mixColors(p, numColour);
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		initialised = true;
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		
		valueWidth = Math.max(fm.stringWidth(minString), fm.stringWidth(maxString));
		tableHeight = kKeyRectHeight + 2 * kTopBottomBorder;
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		double mixDenom = kKeyRectHeight - 1;
		for (int i=0 ; i<kKeyRectHeight ; i++) {
			g.setColor(mixColors((1.0 - i / mixDenom), numColour));
			g.drawLine(kLeftRightBorder, kTopBottomBorder + i, kLeftRightBorder + kKeyRectWidth,
																																	kTopBottomBorder + i);
		}
		
		g.setColor(getForeground());
		g.drawRect(kLeftRightBorder - 1, kTopBottomBorder - 1, kKeyRectWidth + 1, kKeyRectHeight + 1);
		
		int valueHoriz = kLeftRightBorder + kKeyRectWidth + kRectValueGap;
		g.setColor(numColour[numColour.length - 1]);
		g.drawString(maxString, valueHoriz, kTopBottomBorder + ascent);
		g.setColor(numColour[0]);
		g.drawString(minString, valueHoriz, kTopBottomBorder + kKeyRectHeight);
		
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(2 * kLeftRightBorder + kKeyRectWidth + kRectValueGap + valueWidth,
																									tableHeight);
	}
}