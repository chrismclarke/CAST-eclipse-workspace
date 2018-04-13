package stemLeaf;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class CrossAndLeafView extends DataView {
//	static public final String CROSS_AND_LEAF_PLOT = "crossAndLeaf";
	
	static final private String digit[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
	
	private HorizAxis axis;
	
	private double leafFactor;		//	makes leaf units digit
	private double class0Start, classWidth;
	
	private boolean showLeaves = false;
	
	public CrossAndLeafView(DataSet theData, XApplet applet, HorizAxis axis, String groupingInfo) {
		super(theData, applet, new Insets(0,0,0,0));
		this.axis = axis;
		
		StringTokenizer st = new StringTokenizer(groupingInfo);
		class0Start = Double.parseDouble(st.nextToken());
		classWidth = Double.parseDouble(st.nextToken());
		leafFactor = Double.parseDouble(st.nextToken());
	}
	
	public void setShowLeaves(boolean showLeaves) {
		this.showLeaves = showLeaves;
		repaint();
	}
	
	public void paintView(Graphics g) {
		NumVariable xVar = getNumVariable();
		NumValue sortedVal[] = xVar.getSortedData();
		
		int classPix = axis.numValToRawPosition(class0Start + classWidth)
																									- axis.numValToRawPosition(class0Start);
		int crossSize = (classPix * 2) / 3;
		int halfCrossSize = crossSize / 2;
		Point p = null;
		
		double classTop = class0Start + classWidth;
		int xCenter = axis.numValToRawPosition(classTop - classWidth / 2);
		int yCenter = classPix / 2;
		
		int digitOffset = 0;
		int halfAscent = 0;
		if (showLeaves) {
			String fName = getFont().getName();
			int size = getFont().getSize() * 2;
			while (true) {
				g.setFont(new Font(fName, Font.PLAIN, size));
				FontMetrics fm = g.getFontMetrics();
				if (fm.getAscent() < classPix) {
					halfAscent = fm.getAscent() / 2;
					digitOffset = fm.charWidth('0') / 2;
					break;
				}
				size--;
			}
		}
		
		for (int i=0 ; i<sortedVal.length ; i++) {
			if (sortedVal[i].toDouble() >= classTop) {
				while (sortedVal[i].toDouble() >= classTop)
					classTop += classWidth;
				xCenter = axis.numValToRawPosition(classTop - classWidth / 2);
				yCenter = classPix / 2;
			}
			
			p = translateToScreen(xCenter, yCenter, p);
			
			int rawIndex = xVar.rankToIndex(i);
			boolean selected = getSelection().valueAt(rawIndex);
			if (selected) {
				g.setColor(Color.red);
				g.fillRect(p.x - halfCrossSize - 2, p.y - halfCrossSize - 2, 2 * halfCrossSize + 5,
																																				2 * halfCrossSize + 5);
				g.setColor(getForeground());
			}
			
			if (showLeaves) {
				int stemLeaf = (int)Math.floor(sortedVal[i].toDouble() * leafFactor);
				int leaf = stemLeaf % 10;
				g.drawString(digit[leaf], p.x - digitOffset, p.y + halfAscent);
			}
			else {
				g.drawLine(p.x - halfCrossSize, p.y - halfCrossSize, p.x + halfCrossSize,
																																					p.y + halfCrossSize);
				g.drawLine(p.x - halfCrossSize, p.y + halfCrossSize, p.x + halfCrossSize,
																																					p.y - halfCrossSize);
			}
			
			yCenter += classPix;
		}
	}
	
//-----------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		try {
			Point p = translateFromScreen(x, y, null);
			double xVal = axis.positionToNumVal(p.x);
			int classPix = axis.numValToRawPosition(class0Start + classWidth)
																									- axis.numValToRawPosition(class0Start);
			int hitIndex = p.y / classPix;
			
			NumVariable xVar = getNumVariable();
			NumValue sortedVal[] = xVar.getSortedData();
			
			double classBottom = class0Start;
			double classTop = class0Start + classWidth;
			int indexInStack = 0;
			
			for (int i=0 ; i<sortedVal.length ; i++) {
				if (sortedVal[i].toDouble() >= classTop) {
					while (sortedVal[i].toDouble() >= classTop) {
						classBottom = classTop;
						classTop += classWidth;
					}
					indexInStack = 0;
				}
				
				if (xVal < classBottom)
					break;
				if (xVal < classTop && indexInStack == hitIndex) {
					int index = xVar.rankToIndex(i);
					return new IndexPosInfo(index);
				}
				indexInStack ++;
			}
			
		} catch (AxisException e) {
		}
		
		return null;
	}
	
}