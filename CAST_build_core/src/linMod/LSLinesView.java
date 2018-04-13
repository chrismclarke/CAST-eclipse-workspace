package linMod;

import java.awt.*;

import dataView.*;
import axis.*;


public class LSLinesView extends DataView {
//	static public final String LS_LINES_PLOT = "lsLinesPlot";
	
	private String interceptKey, slopeKey;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private double lowX, highX;
	
	public LSLinesView(SummaryDataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String interceptKey, String slopeKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.interceptKey = interceptKey;
		this.slopeKey = slopeKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		
		double axisSlop = (xAxis.maxOnAxis - xAxis.minOnAxis) * 0.1;
		lowX = xAxis.minOnAxis - axisSlop;
		highX = xAxis.maxOnAxis + axisSlop;
	}
	
	private Point getScreenPos(double x, double y) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = xAxis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, null);
	}
	
	private void drawLSLine(Graphics g, double intercept, double slope) {
		double lowY = intercept + slope * lowX;
		double highY = intercept + slope * highX;
		
		Point startPos = getScreenPos(lowX, lowY);
		Point endPos = getScreenPos(highX, highY);
		g.drawLine(startPos.x, startPos.y, endPos.x, endPos.y);
	}
	
	public void paintView(Graphics g) {
		NumVariable interceptVar = (NumVariable)getVariable(interceptKey);
		NumVariable slopeVar = (NumVariable)getVariable(slopeKey);
		ValueEnumeration interceptE = interceptVar.values();
		ValueEnumeration slopeE = slopeVar.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		
		g.setColor(Color.blue);
		while (interceptE.hasMoreValues() && slopeE.hasMoreValues()) {
			double intercept = interceptE.nextDouble();
			double slope = slopeE.nextDouble();
			boolean nextSel = fe.nextFlag();
			if (!nextSel)
				drawLSLine(g, intercept, slope);
		}
		
		interceptE = interceptVar.values();
		slopeE = slopeVar.values();
		fe = getSelection().getEnumeration();
		
		g.setColor(Color.red);
		while (interceptE.hasMoreValues() && slopeE.hasMoreValues()) {
			double intercept = interceptE.nextDouble();
			double slope = slopeE.nextDouble();
			boolean nextSel = fe.nextFlag();
			if (nextSel)
				drawLSLine(g, intercept, slope);
		}
		g.setColor(getForeground());
	}
	
//-----------------------------------------------------------------------------------
	
	private static final int kMinHitDist = 9;
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		
		try {
			Point thePoint = translateFromScreen(x, y, null);
			double xVal = xAxis.positionToNumVal(thePoint.x);
			
			NumVariable interceptVar = (NumVariable)getVariable(interceptKey);
			NumVariable slopeVar = (NumVariable)getVariable(slopeKey);
			ValueEnumeration interceptE = interceptVar.values();
			ValueEnumeration slopeE = slopeVar.values();
			int i = 0;
			while (interceptE.hasMoreValues() && slopeE.hasMoreValues()) {
				double intercept = interceptE.nextDouble();
				double slope = slopeE.nextDouble();
				int yOnLine = yAxis.numValToRawPosition(intercept + slope * xVal);
				int dist = (thePoint.y - yOnLine) * (thePoint.y - yOnLine);
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = i;
					minDist = dist;
				}
				i++;
			}
		} catch (AxisException e) {
		}
		if (gotPoint && minDist < kMinHitDist)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
}
	
