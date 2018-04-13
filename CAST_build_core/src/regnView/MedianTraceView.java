package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import regn.*;


public class MedianTraceView extends CoreMedianTraceView {
//	static public final String MEDIAN_TRACE_PLOT = "medianTrace";
	
	static final private int NO_SELECTED_CLASS = -1;
	private ClassMedianView xMedianView, yMedianView;
	
	private int selectedClass = NO_SELECTED_CLASS;
	
	public MedianTraceView(DataSet theData, XApplet applet,
									HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, 
									double[] initialBoundaries) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, initialBoundaries);
	}
	
	public MedianTraceView(DataSet theData, XApplet applet, HorizAxis xAxis,
								VertAxis yAxis, String xKey, String yKey, String initialBoundaries) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, initialBoundaries);
	}
	
	public void setMedianViews(ClassMedianView xMedianView, ClassMedianView yMedianView) {
		this.xMedianView = xMedianView;
		this.yMedianView = yMedianView;
	}
	
	protected boolean canDrawX(double x) {
		switch (selectedClass) {
			case NO_SELECTED_CLASS:
				return true;
			case 0:
				return x <= boundary[0];
			default:
				if (selectedClass == boundary.length)
					return x > boundary[boundary.length - 1];
				else
					return x > boundary[selectedClass - 1] && x <= boundary[selectedClass];
		}
	}
	
	protected void drawBackground(Graphics g) {
		double xMed[] = getXMedians();
		double yMed[] = getYMedians();
		
		if (selectedClass == NO_SELECTED_CLASS) {
			g.setColor(Color.gray);
			Point p = null;
			for (int i=0 ; i<boundary.length ; i++)
				try {
					int horizPos = xAxis.numValToPosition(boundary[i]);
					p = translateToScreen(horizPos, 0, p);
					g.drawLine(p.x, 0, p.x, getSize().height - 1);
				} catch (AxisException e) {
				}
		}
		else {
			g.setColor(Color.lightGray);
			try {
				if (selectedClass > 0) {
					int horizPos = xAxis.numValToPosition(boundary[selectedClass - 1]);
					int leftPos = translateToScreen(horizPos, 0, null).x;
					g.fillRect(0, 0, leftPos, getSize().height);
				}
				if (selectedClass < boundary.length) {
					int horizPos = xAxis.numValToPosition(boundary[selectedClass]);
					int rightPos = translateToScreen(horizPos, 0, null).x;
					g.fillRect(rightPos, 0, getSize().width, getSize().height);
				}
			} catch (AxisException e) {
			}
		}
		
		if (selectedClass == NO_SELECTED_CLASS) 
			drawMedianTrace(g, xMed, yMed);
		else {
			g.setColor(Color.red);
			if (!Double.isNaN(xMed[selectedClass]) && !Double.isNaN(yMed[selectedClass]))
				try {
					int vertPos = yAxis.numValToPosition(yMed[selectedClass]);
					int horizPos = xAxis.numValToPosition(xMed[selectedClass]);
					Point crossPos = translateToScreen(horizPos, vertPos, null);
					drawCircle(g, crossPos);
					g.drawLine(0, crossPos.y, getSize().width - 1, crossPos.y);
					g.drawLine(0, crossPos.y, 3, crossPos.y - 3);
					g.drawLine(0, crossPos.y, 3, crossPos.y + 3);
					
					g.drawLine(crossPos.x, 0, crossPos.x, getSize().height - 1);
					g.drawLine(crossPos.x, getSize().height - 1, crossPos.x - 3, getSize().height - 4);
					g.drawLine(crossPos.x, getSize().height - 1, crossPos.x + 3, getSize().height - 4);
				} catch (AxisException e) {
				}
		}
		
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	private void changeSelectedMedians() {
		NumValue xMedian = null;
		NumValue yMedian = null;
		if (selectedClass != NO_SELECTED_CLASS) {
			NumVariable xVar = (NumVariable)getVariable(xKey);
			int xDecimals = xVar.getMaxDecimals();
			double xMed[] = getXMedians();
			xMedian = new NumValue(xMed[selectedClass], xDecimals);
			
			NumVariable yVar = (NumVariable)getVariable(yKey);
			int yDecimals = yVar.getMaxDecimals();
			double yMed[] = getYMedians();
			yMedian = new NumValue(yMed[selectedClass], yDecimals);
		}
		xMedianView.setValue(xMedian);
		yMedianView.setValue(yMedian);
	}

//-----------------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		try {
			Point hitPos = translateFromScreen(x, y, null);
			double hitVal = xAxis.positionToNumVal(hitPos.x);
			int classNo = 0;
			for ( ; classNo<boundary.length ; classNo++)
				if (hitVal <= boundary[classNo])
					break;
			return new IndexPosInfo(classNo);
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		selectedClass = ((IndexPosInfo)startInfo).itemIndex;
		changeSelectedMedians();
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		selectedClass = (toPos == null) ? NO_SELECTED_CLASS : ((IndexPosInfo)toPos).itemIndex;
		changeSelectedMedians();
		repaint();
	}
	
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (selectedClass != NO_SELECTED_CLASS) {
			selectedClass = NO_SELECTED_CLASS;
			changeSelectedMedians();
		}
		repaint();
	}
	
	
}
	
