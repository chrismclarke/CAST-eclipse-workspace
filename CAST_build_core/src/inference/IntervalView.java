package inference;

import java.awt.*;

import dataView.*;
import axis.*;
import coreSummaries.*;


public class IntervalView extends MarginalDataView {
//	static final public String INTERVAL_VIEW = "interval";
	
	static final private int kMinIntervalWidth = 30;
	private static final int kMinHitDist = 4;
	private static final int kMaxIntervalSpacing = 5;
	
	private String variableKey;
	private NumValue targetValue;
	
	public IntervalView(DataSet theData, XApplet applet, NumCatAxis theAxis, String variableKey,
																																			NumValue targetValue) {
		super(theData, applet, new Insets(0, 0, 0, 0), theAxis);
		this.variableKey = variableKey;
		this.targetValue = targetValue;
	}
	
	public void setVariableKey(String variableKey) {
		this.variableKey = variableKey;
		repaint();
	}

//---------------------------------------------------------------
	
	public int minDisplayWidth() {
		return kMinIntervalWidth;
	}
	
	private int getLinePos(int index, int noOfValues) {
		int availableHt = getDisplayWidth();
		if (kMaxIntervalSpacing * noOfValues <= availableHt)
			return index * kMaxIntervalSpacing + (kMaxIntervalSpacing + 1) / 2; 
		else
			return (availableHt * index + availableHt / 2) / noOfValues;
	}
	
	private int getHighlightWidth(int noOfValues) {
		int availableHt = getDisplayWidth();
		if (kMaxIntervalSpacing * noOfValues <= availableHt)
			return kMaxIntervalSpacing / 2;
		else
			return (availableHt + noOfValues - 1) / (2 * noOfValues);
	}
	
	public void paintView(Graphics g) {
		Point p0 = null;
		Point p1 = null;
		
		int targetPos = axis.numValToRawPosition(targetValue.toDouble());
		p0 = translateToScreen(targetPos, getSize().height, p0);
		p1 = translateToScreen(targetPos, 0, p1);
		g.setColor(Color.lightGray);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		g.setColor(getForeground());
		
		IntervalSummaryVariable variable = (IntervalSummaryVariable)getVariable(variableKey);
		int noOfValues = variable.noOfValues();
		int highlightWidth = getHighlightWidth(noOfValues);
		
		ValueEnumeration e = variable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while (e.hasMoreValues()) {
			IntervalValue nextVal = (IntervalValue)e.nextValue();
			boolean nextSel = fe.nextFlag();
			boolean includesTarget = nextVal.lowValue.toDouble() <= targetValue.toDouble()
											&& nextVal.highValue.toDouble() >= targetValue.toDouble();
			int linePos = getLinePos(index, noOfValues);
			if (nextSel) {
				g.setColor(Color.yellow);
				p0 = translateToScreen(0, linePos + highlightWidth, p0);
				p1 = translateToScreen(getSize().width, linePos - highlightWidth, p1);
				g.fillRect(p0.x, p0.y, p1.x - p0.x, p1.y - p0.y);
				g.setColor(getForeground());
			}
			if (!includesTarget)
				g.setColor(Color.red);
				
			
			int lowPos = axis.numValToRawPosition(nextVal.lowValue.toDouble());
			int highPos = axis.numValToRawPosition(nextVal.highValue.toDouble());
			p0 = translateToScreen(lowPos, linePos, p0);
			p1 = translateToScreen(highPos, linePos, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			if (!includesTarget)
				g.setColor(getForeground());
			index++;
		}
	}
	
//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		IntervalSummaryVariable variable = (IntervalSummaryVariable)getVariable(variableKey);
		int noOfValues = variable.noOfValues();
		
		Point p = translateFromScreen(x, y, null);
		
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<noOfValues ; i++) {
			int yDist = getLinePos(i, noOfValues) - p.y;
			if (yDist < 0)
				yDist = -yDist;
			if (!gotPoint) {
				gotPoint = true;
				minIndex = i;
				minDist = yDist;
			}
			else if (yDist < minDist) {
				minIndex = i;
				minDist = yDist;
			}
		}
		if (gotPoint && minDist < kMinHitDist)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
}
	
