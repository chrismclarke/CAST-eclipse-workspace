package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


abstract public class CorePairedView extends DotPlotView {
	protected NumCatAxis groupAxis;
	protected String postKey;
	
	protected int groupIndex;
	protected int groupCentre;
	protected boolean showPairing = false;
	
	public CorePairedView(DataSet theData, XApplet applet,
												String preKey, String postKey, NumCatAxis theAxis, NumCatAxis groupAxis,
												double initialJittering) {
		super(theData, applet, theAxis, initialJittering);
		this.groupAxis = groupAxis;
		setActiveNumVariable(preKey);
		this.postKey = postKey;
	}
	
	public void setShowPairing(boolean showPairing) {
		this.showPairing = showPairing;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point p = super.getScreenPoint(index, theVal, thePoint);
		if (p != null)
			if (vertNotHoriz)
				p.x += groupCentre - currentJitter / 2;
			else
				p.y -= groupCentre - currentJitter / 2;
		
		return p;
	}
	
	abstract protected Color getJoiningColor(int i);
	
	protected void drawBackground(Graphics g) {
		int preGroupCentre = groupAxis.catValToPosition(0);
		int postGroupCentre = groupAxis.catValToPosition(1);
		
		NumVariable preVariable = getNumVariable();
		NumVariable postVariable = (NumVariable)getVariable(postKey);
		Point prePoint = null;
		Point postPoint = null;
		groupCentre = preGroupCentre;
		
		ValueEnumeration preE = preVariable.values();
		ValueEnumeration postE = postVariable.values();
		int index = 0;
		while (preE.hasMoreValues()) {
			NumValue preVal = (NumValue)preE.nextValue();
			NumValue postVal = (NumValue)postE.nextValue();
			prePoint = getScreenPoint(index, preVal, prePoint);
			groupCentre = postGroupCentre;
			postPoint = getScreenPoint(index, postVal, postPoint);
			groupCentre = preGroupCentre;
			if (prePoint != null && postPoint != null) {
				Color c = getJoiningColor(index);
				if (c !=  null) {
					g.setColor(c);
					g.drawLine(prePoint.x, prePoint.y, postPoint.x, postPoint.y);
				}
			}
			index++;
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		groupIndex = 0;
		groupCentre = groupAxis.catValToPosition(0);
		super.paintView(g);
		
		groupIndex = 1;
		groupCentre = groupAxis.catValToPosition(1);
		
		NumVariable postVariable = (NumVariable)getVariable(postKey);
		Point thePoint = null;
		
		g.setColor(getForeground());
		ValueEnumeration e = postVariable.values();
		int index = 0;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null) {
				fiddleColor(g, index);
				drawMark(g, thePoint, groupIndex(index));
			}
			index++;
		}
	}
	
	protected int getMaxJitter() {
		return Math.min(super.getMaxJitter(), (getDisplayWidth() - getDisplayBorderNearAxis() - getDisplayBorderAwayAxis()) / 4);
	}

//-----------------------------------------------------------------------------------
	
	private Point crossPos2[];
	private static final int kMinHitDist = 9;
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		groupCentre = groupAxis.catValToPosition(0);
		
		PositionInfo result = super.getPosition(x, y);
		if (result != null)
			return result;
		
		if (crossPos2 == null) {
			groupCentre = groupAxis.catValToPosition(1);
			NumVariable postVariable = (NumVariable)getVariable(postKey);
			int noOfVals = postVariable.noOfValues();
			crossPos2 = new Point[noOfVals];
			for (int i=0 ; i<noOfVals ; i++)
				crossPos2[i] = getScreenPoint(i, (NumValue)(postVariable.valueAt(i)), null);
		}
		
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<crossPos2.length ; i++)
			if (crossPos2[i] != null) {
				int xDist = crossPos2[i].x - x;
				int yDist = crossPos2[i].y - y;
				int dist = xDist*xDist + yDist*yDist;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = i;
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
		crossPos2 = null;
	}
	
}
	
