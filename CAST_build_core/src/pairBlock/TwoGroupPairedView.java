package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;


public class TwoGroupPairedView extends CorePairedView {
//	static public final String TWO_GROUP_PAIRED = "twoGroupPaired";
	
	static final private int kHalfOverlap = 15;
	static final private int kArrowSize = 5;
	
	static final private Color kDimCrossColor = new Color(0xDDDDDD);
	static final private Color kJoiningColor = new Color(0xCCCCFF);
	
	protected CatVariable groupVariable;
	protected boolean inSample;
	
	public TwoGroupPairedView(DataSet theData, XApplet applet,
												String preKey, String postKey, String groupKey, NumCatAxis theAxis,
												NumCatAxis groupAxis, double initialJittering) {
		super(theData, applet, preKey, postKey, theAxis, groupAxis, initialJittering);
		groupVariable = (CatVariable)theData.getVariable(groupKey);
	}
	
	protected double getMean(int i) {
		NumVariable y1Var = getNumVariable();
		NumVariable y2Var = (NumVariable)getVariable(postKey);
		CatVariable groupVar = getCatVariable();
		int n = 0;
		double sy = 0.0;
		
		ValueEnumeration y1e = y1Var.values();
		ValueEnumeration y2e = y2Var.values();
		ValueEnumeration ge = groupVar.values();
		while (y1e.hasMoreValues() && y2e.hasMoreValues() && ge.hasMoreValues()) {
			double y1 = y1e.nextDouble();
			double y2 = y2e.nextDouble();
			int g = groupVar.labelIndex(ge.nextValue());
			if (g == i) {
				n ++;
				sy += (i == 0) ? y1 : y2;
			}
		}
		return sy / n;
	}
	
	protected void drawBackground(Graphics g) {
		super.drawBackground(g);
		
		int xSpacing = groupAxis.catValToPosition(1) - groupAxis.catValToPosition(0);
		int offset = Math.min(xSpacing / 2 + kHalfOverlap, xSpacing - currentJitter / 2);
		Point thePoint = null;
		int previousY = 0;
		for (int i=0 ; i<2 ; i++)
			try {
				double mean = getMean(i);
				int yPos = axis.numValToPosition(mean);
				int xCenter = groupAxis.catValToPosition(i);
				thePoint = translateToScreen(yPos, xCenter - offset, thePoint);
				g.setColor(Color.blue);
				g.drawLine(thePoint.x, thePoint.y, thePoint.x + 2 * offset, thePoint.y);
				if (i > 0) {
					int xPos = xCenter - xSpacing / 2;
					thePoint = translateToScreen(yPos, xPos, thePoint);
					g.setColor(Color.red);
					if (thePoint.y >= previousY + 2) {
						int yEnd = thePoint.y - 1;
						int yStart = previousY + 1;
						g.drawLine(thePoint.x, yStart, thePoint.x, yEnd);
						g.drawLine(thePoint.x + 1, yStart, thePoint.x + 1, yEnd);
						g.drawLine(thePoint.x - kArrowSize, yEnd - kArrowSize, thePoint.x, yEnd);
						g.drawLine(thePoint.x + 1 + kArrowSize, yEnd - kArrowSize, thePoint.x + 1, yEnd);
						
						g.drawLine(thePoint.x + 1 - kArrowSize, yEnd - kArrowSize, thePoint.x, yEnd - 1);
						g.drawLine(thePoint.x + kArrowSize, yEnd - kArrowSize, thePoint.x + 1, yEnd - 1);
					}
					else if (thePoint.y <= previousY - 2) {
						int yEnd = thePoint.y + 1;
						int yStart = previousY - 1;
						g.drawLine(thePoint.x + 1, yStart, thePoint.x + 1, yEnd);
						g.drawLine(thePoint.x - kArrowSize, yEnd + kArrowSize, thePoint.x, yEnd);
						g.drawLine(thePoint.x + 1 + kArrowSize, yEnd + kArrowSize, thePoint.x + 1, yEnd);
						
						g.drawLine(thePoint.x + 1 - kArrowSize, yEnd + kArrowSize, thePoint.x, yEnd + 1);
						g.drawLine(thePoint.x + kArrowSize, yEnd + kArrowSize, thePoint.x + 1, yEnd + 1);
					}
				}
				previousY = thePoint.y;
			} catch (AxisException e) {
			}
		g.setColor(getForeground());
	}
	
	protected Color getJoiningColor(int i) {
		if (showPairing)
			return kJoiningColor;
		else
			return null;
	}
	
	protected void fiddleColor(Graphics g, int index) {
		int group = groupVariable.getItemCategory(index);
		inSample = (group == groupIndex);			//	groupIndex is set at start of paintView()
	}
	
	public void drawMark(Graphics g, Point thePos, int markIndex) {
		if (inSample) {								//	set by previous call to fiddleColor()
			g.setColor(getForeground());
			drawCross(g, thePos);
		}
		else if (showPairing) {
			g.setColor(kDimCrossColor);
			drawBlob(g, thePos);
		}
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		int group = groupVariable.getItemCategory(index);
		if (group >= 2)
			return;
		
		if (group == 1) {
			NumVariable postVariable = (NumVariable)getVariable(postKey);
			int oldGroupCentre = groupCentre;
			groupCentre = groupAxis.catValToPosition(1);
			thePoint = getScreenPoint(index, (NumValue)postVariable.valueAt(index), thePoint);
			groupCentre = oldGroupCentre;
		}
		
		if (thePoint != null)
			drawCrossBackground(g, thePoint);
	}
}
	
