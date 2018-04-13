package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class ConditModel3View extends ModelDot3View {
	
	static final public int X_CONDIT = 0;
	static final public int Z_CONDIT = 1;
	
	private int conditType;
	private double conditValue;
	
	public ConditModel3View(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
		
		conditType = Z_CONDIT;
		conditValue = (zAxis.getMinOnAxis() + zAxis.getMaxOnAxis()) * 0.5;
	}
	
	public void setCondit(int conditType, double conditValue) {
		this.conditType = conditType;
		this.conditValue = conditValue;
		repaint();
	}

//--------------------------------------------------------------------------------
	
	protected Polygon drawShadeRegion(Graphics g) {
		Polygon p = super.drawShadeRegion(g);
		
		MultipleRegnModel model = getModel();
		Point p0, p1;
		double xVals[] = new double[2];
		if (conditType == X_CONDIT) {
			xVals[0] = conditValue;
			double zAxisRange = zAxis.getMaxOnAxis() - zAxis.getMinOnAxis();
			xVals[1] = zAxis.getMinOnAxis() - zAxisRange;
			p0 = getModelPoint(xVals, model);
			xVals[1] = zAxis.getMaxOnAxis() + zAxisRange;
			p1 = getModelPoint(xVals, model);
		}
		else {
			xVals[1] = conditValue;
			double xAxisRange = xAxis.getMaxOnAxis() - xAxis.getMinOnAxis();
			xVals[0] = xAxis.getMinOnAxis() - xAxisRange;
			p0 = getModelPoint(xVals, model);
			xVals[0] = xAxis.getMaxOnAxis() + xAxisRange;
			p1 = getModelPoint(xVals, model);
		}
		g.setColor(Color.red);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		g.setColor(getForeground());
		
		return p;
	}
}
	
