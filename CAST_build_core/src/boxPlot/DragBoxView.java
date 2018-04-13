package boxPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class DragBoxView extends CoreDragBoxView {
//	static public final String DRAG_BOX_PLOT = "dragBoxPlot";
	
	static final private int kValueSpacing = 3;
	
	protected int counts[] = new int[4];
	private int minPos[] = new int[5];
	private int maxPos[] = new int[5];
	private boolean hints = false;
	
	public DragBoxView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	public void showHints(boolean hints) {
		this.hints = hints;
		repaint();
	}
	
	protected void initialiseBox(NumValue sortedVal[], BoxInfo boxInfo) {
		super.initialiseBox(sortedVal, boxInfo);
		
		int noOfVals = sortedVal.length;
		minPos[LOW_EXT] = maxPos[LOW_EXT] = getAxisPos(sortedVal[0].toDouble());
		minPos[HIGH_EXT] = maxPos[HIGH_EXT] = getAxisPos(sortedVal[noOfVals - 1].toDouble());
		if ((noOfVals & 0x1) == 1)		//	odd
			minPos[MEDIAN] = maxPos[MEDIAN] = getAxisPos(sortedVal[noOfVals / 2].toDouble());
		else {
			int lowPos = getAxisPos(sortedVal[noOfVals / 2 - 1].toDouble());
			int highPos = getAxisPos(sortedVal[noOfVals / 2].toDouble());
			if (lowPos + 1 < highPos) {
				minPos[MEDIAN] = lowPos + 1;
				maxPos[MEDIAN] = highPos - 1;
			}
			else {
				minPos[MEDIAN] = lowPos;
				maxPos[MEDIAN] = highPos;
			}
		}
		if (noOfVals % 4 == 0) {
			int lowPos = getAxisPos(sortedVal[noOfVals / 4 - 1].toDouble());
			int highPos = getAxisPos(sortedVal[noOfVals / 4].toDouble());
			if (lowPos + 1 < highPos) {
				minPos[LOW_QUART] = lowPos + 1;
				maxPos[LOW_QUART] = highPos - 1;
			}
			else {
				minPos[LOW_QUART] = lowPos;
				maxPos[LOW_QUART] = highPos;
			}
		}
		else
			minPos[LOW_QUART] = maxPos[LOW_QUART] = getAxisPos(sortedVal[noOfVals / 4].toDouble());
		
		if (noOfVals % 4 == 0) {
			int lowPos = getAxisPos(sortedVal[noOfVals - noOfVals / 4 - 1].toDouble());
			int highPos = getAxisPos(sortedVal[noOfVals - noOfVals / 4].toDouble());
			if (lowPos + 1 < highPos) {
				minPos[HIGH_QUART] = lowPos + 1;
				maxPos[HIGH_QUART] = highPos - 1;
			}
			else {
				minPos[HIGH_QUART] = lowPos;
				maxPos[HIGH_QUART] = highPos;
			}
		}
		else
			minPos[HIGH_QUART] = maxPos[HIGH_QUART] = getAxisPos(sortedVal[noOfVals - noOfVals / 4 - 1].toDouble());
	}
	
	protected int getDragTop() {
		return 4 * currentJitter;
	}
	
	protected void shadeBackground(Graphics g) {
		super.shadeBackground(g);
		
		Color oldColor = g.getColor();
		g.setColor(Color.red);
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++) {
			Point p = translateToScreen(boxInfo.boxPos[i], 0, null);
			if (hints && (boxInfo.boxPos[i] < minPos[i] || boxInfo.boxPos[i] > maxPos[i]))
				g.fillOval(p.x - 2, getSize().height - getDragTop() - 6, 5, 5);
		}
		
		g.setColor(oldColor);
	}

//-----------------------------------------------------------------------------------
	
	protected void initialiseCounts() {
		for (int i=LOW_OUTER ; i<=HIGH_OUTER ; i++)
			counts[i] = 0;
	}
	
	protected void countPosition(int screenHoriz) {
		int x = screenHoriz - getViewBorder().left;
		for (int i=LOW_OUTER ; i<=HIGH_OUTER ; i++)
			if (x >= boxInfo.boxPos[i] && x <= boxInfo.boxPos[i+1])
				counts[i]++;
	}
	
	protected void drawOneCount(Graphics g, Point p, int count, int noOfValues) {
		NumValue proportion = new NumValue(((double)count) / noOfValues, 3);
		proportion.drawCentred(g, p.x, p.y);
	}
	
	protected void drawCounts(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
//		int descent = fm.getDescent();
//		int leading = fm.getLeading();
		
		NumVariable variable = getNumVariable();
		int noOfValues = variable.noOfValues();
		
		Color oldColor = g.getColor();
		g.setColor(Color.blue);
		
		Point p = null;
		p = translateToScreen((boxInfo.boxPos[LOW_EXT] + boxInfo.boxPos[LOW_QUART]) / 2,
																			boxInfo.vertMidLine + kValueSpacing, p);
		drawOneCount(g, p, counts[LOW_OUTER], noOfValues);
		
		p = translateToScreen((boxInfo.boxPos[LOW_QUART] + boxInfo.boxPos[MEDIAN]) / 2,
																		boxInfo.boxBottom - kValueSpacing - ascent, p);
		drawOneCount(g, p, counts[LOW_INNER], noOfValues);
		
		p = translateToScreen((boxInfo.boxPos[MEDIAN] + boxInfo.boxPos[HIGH_QUART]) / 2,
																	boxInfo.boxBottom + boxInfo.getBoxHeight() + kValueSpacing, p);
		drawOneCount(g, p, counts[HIGH_INNER], noOfValues);
		
		p = translateToScreen((boxInfo.boxPos[HIGH_QUART] + boxInfo.boxPos[HIGH_EXT]) / 2,
																		boxInfo.vertMidLine - kValueSpacing - ascent, p);
		drawOneCount(g, p, counts[HIGH_OUTER], noOfValues);
		g.setColor(oldColor);
	}
}
	
