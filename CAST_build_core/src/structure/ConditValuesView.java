package structure;

import java.awt.*;
import dataView.*;


public class ConditValuesView extends DataView {
	static public final int kLeftRightBorder = 10;
	static public final int kTopBottomBorder = 3;
	static public final int kMinValueGap = 15;
	static public final int kLineGap = 4;
	
	private String yKey, xKey;
	private int xCondit, noOfColumns;
	
	private boolean initialised = false;
	
	private int ascent, descent;
	private int leftRightBorder, valueGap, maxYWidth;
	
	public ConditValuesView(DataSet theData, XApplet applet, String yKey, String xKey,
																													int xCondit, int noOfColumns) {
		super(theData, applet, null);
		lockBackground(Color.white);
		this.yKey = yKey;
		this.xKey = xKey;
		this.xCondit = xCondit;
		this.noOfColumns = noOfColumns;
		repaint();
	}
	
	private int getBestWidth(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		maxYWidth = yVar.getMaxWidth(g);
		return maxYWidth * noOfColumns + 2 * kLeftRightBorder
																										+ (noOfColumns - 1) * kMinValueGap;
	}

	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		if (g == null)
			return new Dimension(20, 20);
		
		CatVariable groupVar = (CatVariable)getVariable(xKey);
		int counts[] = groupVar.getCounts();
		int noOfRows = counts[xCondit] / noOfColumns + 1;
		
		FontMetrics fm = g.getFontMetrics();
		int bestWidth = getBestWidth(g);
		int minHeight = 2 * kTopBottomBorder + noOfRows * (fm.getAscent() + fm.getDescent())
																										+ (noOfRows - 1) * kLineGap;
		
		return new Dimension(bestWidth, minHeight);
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	private boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		int horizError = getSize().width - getBestWidth(g);
		leftRightBorder = (horizError <= 0) ? kLeftRightBorder
																				: kLeftRightBorder + horizError / (2 * noOfColumns); 
		valueGap = (horizError <= 0) ? kMinValueGap + horizError / (noOfColumns - 1)
																					: kMinValueGap + horizError / noOfColumns;
		
		initialised = true;
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable groupVar = (CatVariable)getVariable(xKey);
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = groupVar.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int col = 0;
		int baseline = kTopBottomBorder + ascent;
		int valueRight = kLeftRightBorder + maxYWidth;
		
		while (ye.hasMoreValues()) {
			NumValue nextY = (NumValue)ye.nextValue();
			boolean nextSel = fe.nextFlag();
			int nextCat = groupVar.labelIndex(xe.nextValue());
			if (nextCat == xCondit) {
				if (nextSel) {
					g.setColor(Color.yellow);
					g.fillRect(valueRight - maxYWidth - valueGap / 2, baseline - ascent - kLineGap,
																maxYWidth + valueGap, ascent + descent + kLineGap);
					g.setColor(getForeground());
				}
				
				nextY.drawLeft(g, valueRight, baseline);
				col ++;
				valueRight += (maxYWidth + valueGap);
				if (col >= noOfColumns) {
					col = 0;
					baseline += (ascent + descent + kLineGap);
					valueRight = kLeftRightBorder + maxYWidth;
				}
			}
		}
		
//		g.setColor(Color.black);
//		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
	}


//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (y < kTopBottomBorder || y >= getSize().height - kTopBottomBorder
												|| x < leftRightBorder || x >= getSize().width - leftRightBorder)
			return null;
		
		int col = (x - leftRightBorder + valueGap / 2) / (maxYWidth + valueGap);
		int row = (y - kTopBottomBorder + kLineGap / 2) / (ascent + descent + kLineGap);
		int hitIndex = col + row * noOfColumns;
		
		CatVariable groupVar = (CatVariable)getVariable(xKey);
		ValueEnumeration xe = groupVar.values();
		int index = 0;
		int groupIndex = 0;
		while (xe.hasMoreValues()) {
			int nextCat = groupVar.labelIndex(xe.nextValue());
			if (nextCat == xCondit) {
				if (groupIndex == hitIndex)
					return new IndexPosInfo(index);
				groupIndex ++;
			}
			index ++;
		}
		
		return null;
	}
}
