package simulation;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;



public class HiliteLastDotPlotView extends DotPlotView {
	static final private int kTimeSeriesPoints = 100;
	
	private int timeSeriesPoints = -1;
	
	public HiliteLastDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis, 1.0);
		
	}
	
	public void setTimeSeries(boolean timeNotJitter) {
		if (timeNotJitter)
			timeSeriesPoints = kTimeSeriesPoints;
		else
			timeSeriesPoints = -1;
		repaint();
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	protected int groupIndex(int itemIndex) {
		return (timeSeriesPoints >= 0) ? 8 : 0;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		if (timeSeriesPoints < 0)
			return super.getScreenPoint(index, theVal, thePoint);
		else {
			int availableWidth = getDisplayWidth();
			int noOfValues = getNumVariable().noOfValues();
			if (index < noOfValues - timeSeriesPoints)
				return null;
			
			int timeIndex = (noOfValues <= timeSeriesPoints) ? index
															: (timeSeriesPoints - noOfValues + index);
			int timePos = availableWidth * (timeIndex + 1) / (timeSeriesPoints + 1);
			
			if (Double.isNaN(theVal.toDouble()))
				return null;
			try {
				int horizPos = axis.numValToPosition(theVal.toDouble());
				return translateToScreen(horizPos, timePos, thePoint);
			} catch (AxisException ex) {
				return null;
			}
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		Flags selection = getSelection();
		selection.setFlag(variable.noOfValues() - 1);
		
		if (timeSeriesPoints >= 0) {
			Point previousPoint = null;
			Point thisPoint = null;
			
			checkJittering();
			
			g.setColor(Color.blue);
			ValueEnumeration e = variable.values();
			int index = 0;
			while (e.hasMoreValues()) {
				NumValue nextVal = (NumValue)e.nextValue();
				Point tempPoint = previousPoint;
				previousPoint = thisPoint;
				thisPoint = getScreenPoint(index, nextVal, tempPoint);
				if (previousPoint != null && thisPoint != null)
					g.drawLine(previousPoint.x, previousPoint.y, thisPoint.x, thisPoint.y);
				index++;
			}
			g.setColor(getForeground());
		}
		
		super.paintView(g);
	}
}