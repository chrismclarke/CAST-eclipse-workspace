package sampDesign;

import java.awt.*;

import dataView.*;
import axis.*;


public class ExpandingTimeView extends DataView {
//	static public final String EXPANDING_TIME_PLOT = "expandingtimePlot";
	
	static final private Color kLineColor = Color.blue;
	
	private TimeAxis timeAxis;
	private VertAxis numAxis;
	
	private String catKey;
	
	public ExpandingTimeView(DataSet theData, XApplet applet,
											TimeAxis timeAxis, VertAxis numAxis, String catKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.timeAxis = timeAxis;
		this.numAxis = numAxis;
		this.catKey = catKey;
	}
	
	protected Point getScreenPoint(int index, double theVal, Point thePoint) {
		if (Double.isNaN(theVal))
			return null;
		else
			try {
				int vertPos = numAxis.numValToPosition(theVal);
				int horizPos = timeAxis.timePosition(index);
				return translateToScreen(horizPos, vertPos, thePoint);
			} catch (AxisException ex) {
				return null;
			}
	}
	
	public void paintView(Graphics g) {
		CatVariable variable = (CatVariable)getVariable(catKey);
		
		Point thePoint = null;
		Point previousPoint = null;
		Value successValue = variable.getLabel(1);
		
		g.setColor(kLineColor);
		ValueEnumeration e = variable.values();
		int nSuccess = 0;
		int nTrial = 0;
		while (e.hasMoreValues()) {
			if (e.nextValue() == successValue)			//	values point to same labels
				nSuccess ++;
			nTrial ++;
			double nextVal = nSuccess / (double)nTrial;
			thePoint = getScreenPoint(nTrial - 1, nextVal, thePoint);
			if (thePoint != null && previousPoint != null)
				g.drawLine(previousPoint.x, previousPoint.y, thePoint.x, thePoint.y);
			
			Point temp = thePoint;
			thePoint = previousPoint;
			previousPoint = temp;
		}
		
		g.setColor(getForeground());
		
		setCrossSize(variable.noOfValues() < 50 ? MEDIUM_CROSS
									: variable.noOfValues() < 100 ? SMALL_CROSS
									: DOT_CROSS);
		
		e = variable.values();
		nSuccess = 0;
		nTrial = 0;
		while (e.hasMoreValues()) {
			if (e.nextValue() == successValue)			//	values point to same labels
				nSuccess ++;
			nTrial ++;
			double nextVal = nSuccess / (double)nTrial;
			thePoint = getScreenPoint(nTrial - 1, nextVal, thePoint);
			if (thePoint != null)
				drawBlob(g, thePoint);
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
	
