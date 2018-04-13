package cat;

import java.awt.*;

import dataView.*;
import axis.*;


public class QuantBarView extends DataView {
//	static final public String QUANTBARCHART = "quantBarChart";
	
	static final public int BAR_VIEW = 0;
	static final public int CROSS_VIEW = 1;
	
	static final private int kHalfBarWidth = 4;
	
	private String numKey;
	private IndexTimeAxis timeAxis;
	private MultiVertAxis valueAxis;
	
	private int viewType;
	
	public QuantBarView(DataSet theData, XApplet applet, String numKey, IndexTimeAxis timeAxis,
																																			MultiVertAxis valueAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.timeAxis = timeAxis;
		this.valueAxis = valueAxis;
		this.numKey = numKey;
	}
	
	public void setNumVariable(String numKey) {
		this.numKey = numKey;
		repaint();
	}
	
	public void setViewType(int viewType) {
		this.viewType = viewType;
		repaint();
	}
	
	private Point getScreenPoint(int index, double theVal, Point thePoint) {
		if (Double.isNaN(theVal))
			return null;
		else
			try {
				int vertPos = valueAxis.numValToPosition(theVal);
				int horizPos = timeAxis.timePosition(index);
				return translateToScreen(horizPos, vertPos, thePoint);
			} catch (AxisException ex) {
				return null;
			}
	}
	
	private void drawTimeLine(Graphics g) {
		NumVariable yVariable = (NumVariable)getVariable(numKey);
		g.setColor(Color.blue);
		Point lastPoint = null;
		Point thisPoint = null;
		int index = 0;
		ValueEnumeration e = yVariable.values();
		while (e.hasMoreValues()) {
			thisPoint = getScreenPoint(index, e.nextDouble(), thisPoint);
			if (thisPoint != null && lastPoint != null)
				g.drawLine(lastPoint.x, lastPoint.y, thisPoint.x, thisPoint.y);
			index++;
			Point temp = lastPoint;
			lastPoint = thisPoint;
			thisPoint = temp;
		}
	}
	
	private void drawTimeCrosses(Graphics g) {
		NumVariable yVariable = (NumVariable)getVariable(numKey);
		g.setColor(Color.black);
		Point thePoint = null;
		int index = 0;
		ValueEnumeration e = yVariable.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null)
				drawSquare(g, thePoint);
			index++;
		}
	}
	
	private void drawBars(Graphics g) {
		NumVariable yVariable = (NumVariable)getVariable(numKey);
		
		int halfBarWidth = Math.min(getSize().width / (yVariable.noOfValues() * 3) - 1, kHalfBarWidth);
		
		g.setColor(Color.blue);
		Point thePoint = null;
		int index = 0;
		ValueEnumeration e = yVariable.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null)
				g.fillRect(thePoint.x - halfBarWidth, thePoint.y, halfBarWidth * 2 + 1,
																																getSize().height - thePoint.y);
			index++;
		}
	}
	
	public void paintView(Graphics g) {
		if (viewType == BAR_VIEW)
			drawBars(g);
		else {
			drawTimeLine(g);
			drawTimeCrosses(g);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	
}