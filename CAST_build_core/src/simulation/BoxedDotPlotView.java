package simulation;

import java.awt.*;

import dataView.*;
import axis.*;


public class BoxedDotPlotView extends DataView {
//	static public final String BOXEDDOTPLOT = "boxedDotPlot";
	
	private double step;
	protected String yKey;
	protected NumCatAxis axis;
	
	private boolean initialised = false;
	
	private int iMinOnAxis, iMaxOnAxis;
//	private int noOfCrosses;
	
	protected int groupIndexOnAxis[] = null;
	protected int horizPos[] = null;
	private int maxCount;
	
	protected Color boxFillColor = Color.white;
	protected Color boxOutlineColor = Color.gray;
	protected Color boxHighlightColor = Color.red;
	
	public BoxedDotPlotView(DataSet theData, XApplet applet, NumCatAxis axis, String yKey, double step) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.yKey = yKey;
		this.step = step;
		this.axis = axis;
		
		iMinOnAxis = (int)Math.ceil(axis.minOnAxis / step - 0.00001);
		iMaxOnAxis = (int)Math.floor(axis.maxOnAxis / step + 0.00001);
	}
	
	protected boolean initialise() {
		if (initialised)
			return false;
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		
		ValueEnumeration ve = yVar.values();
		maxCount = 0;
		int noOfGroups = 0;
		while (ve.hasMoreValues()) {
			RepeatValue rv = ve.nextGroup();
			maxCount = Math.max(maxCount, rv.count);
			noOfGroups++;
		}
		
		groupIndexOnAxis = new int[noOfGroups];
		
		ve = yVar.values();
		int groupIndex = 0;
		while (ve.hasMoreValues()) {
			RepeatValue rv = ve.nextGroup();
			maxCount = Math.max(maxCount, rv.count);
			
			double y = ((NumValue)rv.val).toDouble();
			int iy = (int)Math.round(y / step);
			groupIndexOnAxis[groupIndex] = iy - iMinOnAxis;
			
			groupIndex++;
		}
		
		horizPos = new int[iMaxOnAxis - iMinOnAxis + 3];
		
		for (int i=0 ; i<horizPos.length ; i++) {
			double y = (iMinOnAxis + i - 1) * step;
			horizPos[i] = axis.numValToRawPosition(y);
		}
		
		initialised = true;
		return true;
	}
	
	protected int getCellVert() {
		int cellVert = horizPos[1] - horizPos[0];
		if (cellVert * maxCount >= getSize().height - 1)
			cellVert = (getSize().height - 1) / maxCount;
		return cellVert;
	}
	
	protected int vertForFreq(int i, int cellVert) {
		return i * cellVert;
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int selection = getSelection().findSingleSetFlag();
		
		int cellVert = getCellVert();
		Point p1 = null;
		Point p2 = null;
		
		ValueEnumeration ve = yVar.values();
		int groupIndex = 0;
		int selectionInColumn = -1;
		int selectionLeft = 0;
		int selectionRight = 0;
		
		while (ve.hasMoreValues()) {
			RepeatValue rv = ve.nextGroup();
			int horizIndex = groupIndexOnAxis[groupIndex];
			
			int leftHoriz = (horizPos[horizIndex] + horizPos[horizIndex + 1]) / 2;
			int rightHoriz = (horizPos[horizIndex + 1] + horizPos[horizIndex + 2]) / 2;
			
			g.setColor(boxFillColor);
			int columnHeight = vertForFreq(rv.count, cellVert);
			p1 = translateToScreen(leftHoriz, columnHeight, p1);
			p2 = translateToScreen(rightHoriz, 0, p2);
			g.fillRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
			
			g.setColor(boxOutlineColor);
			g.drawRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
			
			for (int i=1 ; i<rv.count ; i++) {
				p1 = translateToScreen(leftHoriz, vertForFreq(i, cellVert), p1);
				g.drawLine(p1.x, p1.y, p2.x, p1.y);
			}
			
			if (selection >= 0 && selection < rv.count) {
				selectionInColumn = selection;
				selectionLeft = leftHoriz;
				selectionRight = rightHoriz;
			}
			
			selection -= rv.count;
			groupIndex++;
		}
		if (selectionInColumn >= 0) {
			g.setColor(boxHighlightColor);
			Point p3 = translateToScreen(selectionLeft, vertForFreq(selectionInColumn + 1, cellVert), null);
			Point p4 = translateToScreen(selectionRight, vertForFreq(selectionInColumn, cellVert), null);
			g.fillRect(p3.x, p3.y, p4.x - p3.x, p4.y - p3.y);
			g.setColor(getForeground());
			g.drawRect(p3.x, p3.y, p4.x - p3.x, p4.y - p3.y);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (y > getSize().height)
			return null;
		
		int minHorizDist = 10000;		//		any large value
		int hitColumn = -1;
		for (int i=0 ; i<horizPos.length ; i++) {
			int thisDist = Math.abs(x - horizPos[i]);
			if (thisDist < minHorizDist) {
				minHorizDist = thisDist;
				hitColumn = i;
			}
		}
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ve = yVar.values();
		int cumulative = 0;
		for (int i=0 ; i<groupIndexOnAxis.length ; i++) {
			RepeatValue rv = ve.nextGroup();
			if (groupIndexOnAxis[i] == hitColumn - 1) {
				int cellVert = getCellVert();
				int cellVertIndex = (getSize().height - y - 1) / cellVert;
				if (cellVertIndex < rv.count)
					return new IndexPosInfo(cumulative + cellVertIndex);
				else
					return null;
			}
			cumulative += rv.count;
		}
		
		return null;
	}
}
	
