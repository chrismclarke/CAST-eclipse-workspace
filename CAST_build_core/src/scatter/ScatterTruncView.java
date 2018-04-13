package scatter;

import java.awt.*;

import dataView.*;
import axis.*;


public class ScatterTruncView extends ScatterMoveView {
	
	private double truncX = Double.NEGATIVE_INFINITY;
	private double propnCutOff;
	
	public ScatterTruncView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey,
										String x2Key, String y2Key, double propnCutOff) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, x2Key, y2Key);
		this.propnCutOff = propnCutOff;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		double x = getX(index, theVal);
		if (x >= truncX)
			return super.getScreenPoint(index, theVal, thePoint);
		else
			return null;
	}
	
	protected int groupIndex(int itemIndex) {
		return isPass(itemIndex) ? 1 : 0;
	}
	
	public void paintView(Graphics g) {
		if (!Double.isInfinite(truncX))
			try {
				int horizPos = axis.numValToPosition(truncX);
				Point thePoint = translateToScreen(horizPos, 0, null);
				Color oldColor = g.getColor();
				g.setColor(Color.lightGray);
				g.fillRect(0, 0, thePoint.x, getSize().height);
				g.setColor(oldColor);
			} catch (AxisException e) {
			}
		
		super.paintView(g);
	}
	
	public void setTruncation(double truncX) {
		this.truncX = truncX;
		repaint();
	}
	
	public boolean isPass(int itemIndex) {
		double y = getY(itemIndex);
		return (y >= propnCutOff);
	}
	
	public boolean isNotTruncated(int itemIndex) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumValue xVal = (NumValue)(xVariable.valueAt(itemIndex));
		double x = getX(itemIndex, xVal);
		return (x >= truncX);
	}
}
	
