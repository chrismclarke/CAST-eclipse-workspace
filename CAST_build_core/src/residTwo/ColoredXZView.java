package residTwo;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;

import multiRegn.*;


public class ColoredXZView extends ModelDot3View {
	
	static final private Color kDarkRed = new Color(0xFF0000);
	static final private Color kDarkGreen = new Color(0x009900);
	static final private Color kDarkBlue = new Color(0x0000FF);
	
	static final private Color kPlaneColor = Color.lightGray;
	
	static final public int NO_COLOR = 0;
	static final public int X_COLOR = 1;
	static final public int Z_COLOR = 2;
	
	static final private Color getShade(double p, boolean behindPlane) {
		Color baseColor = (p < 0.5) ? mixColors(kDarkGreen, kDarkRed, 2.0 * p)
																: mixColors(kDarkBlue, kDarkGreen, 2.0 * (p - 0.5));
		if (behindPlane)
			baseColor = mixColors(darkenColor(baseColor, 0.2), kPlaneColor, 0.3);
		return baseColor;
	}
	
	private int coloredAxis = NO_COLOR;
	
	public ColoredXZView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
	}
	
	public void setColoredAxis(int coloredAxis) {
		this.coloredAxis = coloredAxis;
	}

//--------------------------------------------------------------------------------

	public Color getPointColor(double colorVal) {		//	only for use by ColoredScatterView
		if (coloredAxis == NO_COLOR)
			return null;
		else {
			double p = (coloredAxis == X_COLOR) ? xAxis.numValToPosition(colorVal)
																					: zAxis.numValToPosition(colorVal);
			return getShade(p, false);
		}
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = getModel();
		boolean fromPlaneTop = viewingPlaneFromTop();
		
		Point crossPos = null;
		double xVals[] = new double[explanKey.length];
		
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			double y = ((NumVariable)getVariable(yKey)).doubleValueAt(selectedIndex);
			for (int i=0 ; i<explanKey.length ; i++)
				xVals[i] = ((NumVariable)getVariable(explanKey[i])).doubleValueAt(selectedIndex);
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			g.setColor(Color.red);
			drawCrossBackground(g, crossPos);
		}
		
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		
		g.setColor(Color.black);
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			for (int i=0 ; i<explanKey.length ; i++)
				xVals[i] = xe[i].nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			
			double fit = model.evaluateMean(xVals);
			boolean fullColor = (shadeHandling != USE_OPAQUE) || ((y >= fit) == fromPlaneTop);
			if (coloredAxis == NO_COLOR)
				g.setColor(fullColor ? Color.black : Color.gray);
			else {
				double p = (coloredAxis == X_COLOR) ? xAxis.numValToPosition(xVals[0])
																						:  zAxis.numValToPosition(xVals[1]);
				g.setColor(getShade(p, !fullColor));
			}
				
			drawCross(g, crossPos);
		}
		g.setColor(getForeground());
	}
}
	
