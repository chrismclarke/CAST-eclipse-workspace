package estimation;

import java.awt.*;

import dataView.*;
import axis.*;

import distribution.*;


public class DeltaApproxView extends ContinuousProbView {
	static final private int kTransformSteps = 40;
	static final public Color kTransformColor = new Color(0x0000CC);
	static final private Color kLinearApproxColor = new Color(0xFF0000);
	
	private NumCatAxis transformAxis;
	private double paramValue;
	
	public DeltaApproxView(DataSet theData, XApplet applet, String distnKey,
												NumCatAxis horizAxis, NumCatAxis transformAxis, double paramValue) {
		super(theData, applet, distnKey, horizAxis, null);
		this.transformAxis = transformAxis;
		this.paramValue = paramValue;
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		double xVal[] = new double[kTransformSteps + 1];
		double yVal[] = new double[kTransformSteps + 1];
		
		double minX = getDrawMin(Double.NEGATIVE_INFINITY);
		double maxX = getDrawMax(Double.POSITIVE_INFINITY);
		
		for (int i=0 ; i<=kTransformSteps ; i++) {
			xVal[i] = minX + i * (maxX - minX) / kTransformSteps;
			yVal[i] = xVal[i] * xVal[i];
		}
		
		g.setColor(kTransformColor);
		drawCurve(g, xVal, yVal, horizAxis, transformAxis);
		
		double quadY = paramValue * paramValue;
		double slope = 2 * paramValue;
		double lowY = quadY - (paramValue - minX) * slope;
		double highY = quadY + (maxX - paramValue) * slope;
		
		g.setColor(kLinearApproxColor);
		int lowXPos = horizAxis.numValToRawPosition(minX);
		int highXPos = horizAxis.numValToRawPosition(maxX);
		int lowYPos = transformAxis.numValToRawPosition(lowY);
		int highYPos = transformAxis.numValToRawPosition(highY);
		Point p0 = translateToScreen(lowXPos, lowYPos, null);
		Point p1 = translateToScreen(highXPos, highYPos, null);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		int meanXPos = horizAxis.numValToRawPosition(paramValue);
		int meanYPos = transformAxis.numValToRawPosition(quadY);
		p0 = translateToScreen(meanXPos, meanYPos, p0);
		int meanAxisPos = transformAxis.numValToRawPosition(0.0);
		p1 = translateToScreen(meanXPos, meanAxisPos, p1);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}