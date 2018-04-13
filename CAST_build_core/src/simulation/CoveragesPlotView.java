package simulation;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class CoveragesPlotView extends DotPlotView {
//	static public final String COVERAGEPLOT = "coveragePlot";
	
	private static final int kMaxVertJitter = 30;
	
	private String[] dataKey;
	private NumCatAxis groupAxis;
	
	public CoveragesPlotView(DataSet theData, XApplet applet, NumCatAxis xAxis, NumCatAxis groupAxis,
																																String[] dataKey) {
		super(theData, applet, xAxis, 0.5);
		this.groupAxis = groupAxis;
		this.dataKey = dataKey;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, int jitterOffset, Point thePoint) {
		if (Double.isNaN(theVal.toDouble()))
			return null;
		try {
			int horizPos = axis.numValToPosition(theVal.toDouble());
			int vertPos = jitterOffset + ((currentJitter * jittering[index]) >> 14);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	public void paintView(Graphics g) {
		checkJittering();
		Point thePoint = null;
		
		for (int i=0 ; i<dataKey.length ; i++) {
			NumVariable variable = (NumVariable)getVariable(dataKey[i]);
			
			int jitterOffset = getJitterOffset(i);
			
			g.setColor(Color.red);
			ValueEnumeration e = variable.values();
			FlagEnumeration fe = getSelection().getEnumeration();
			int index = 0;
			while (e.hasMoreValues()) {
				NumValue nextVal = (NumValue)e.nextValue();
				boolean nextSel = fe.nextFlag();
				if (nextSel) {
					thePoint = getScreenPoint(index, nextVal, jitterOffset, thePoint);
					if (thePoint != null)
						drawCrossBackground(g, thePoint);
				}
				index++;
			}
			g.setColor(getForeground());
			e = variable.values();
			index = 0;
			while (e.hasMoreValues()) {
				NumValue nextVal = (NumValue)e.nextValue();
				thePoint = getScreenPoint(index, nextVal, jitterOffset, thePoint);
				if (thePoint != null)
					drawCross(g, thePoint);
				index++;
			}
		}
	}
	
	protected int getMaxJitter() {
		int noOfGroups = dataKey.length;
		return Math.min(kMaxVertJitter, getDisplayWidth() / noOfGroups / 2);
	}
	
	private int getJitterOffset(int groupIndex) {
		return groupAxis.catValToPosition(groupIndex) - currentJitter / 2;
	}

//-----------------------------------------------------------------------------------
	
	private Point crossPos[][];
	private static final int kMinHitDist = 9;
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (crossPos == null) {
			crossPos = new Point[dataKey.length][];
			for (int i=0 ; i<dataKey.length ; i++) {
				NumVariable variable = (NumVariable)getVariable(dataKey[i]);
				int jitterOffset = getJitterOffset(i);
				int noOfVals = variable.noOfValues();
				crossPos[i] = new Point[noOfVals];
				for (int j=0 ; j<noOfVals ; j++)
					crossPos[i][j] = getScreenPoint(j, (NumValue)(variable.valueAt(j)),
																								jitterOffset, null);
			}
		}
		
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<crossPos.length ; i++)
		for (int j=0 ; j<crossPos[i].length ; j++)
			if (crossPos[i][j] != null) {
				int xDist = crossPos[i][j].x - x;
				int yDist = crossPos[i][j].y - y;
				int dist = xDist*xDist + yDist*yDist;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = j;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = j;
					minDist = dist;
				}
			}
		if (gotPoint && minDist < kMinHitDist)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		super.endDrag(startPos, endPos);
		crossPos = null;
	}
	
}
	
