package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class Model3ResidView extends ModelDot3View {
	
	static final private Color kPaleRed = getShadedColor(Color.red);
//	static final private Color kPaleBlue = getShadedColor(Color.blue);
	static final private Color kDarkGreen = new Color(0x009900);
	static final private Color kPaleGreen = getShadedColor(kDarkGreen);
	
	private boolean showSelectedArrows = true;
	
	public Model3ResidView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
		setSelectCrosses(true);
	}
	
	public void setShowSelectedArrows(boolean showSelectedArrows) {
		this.showSelectedArrows = showSelectedArrows;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = getModel();
		
		boolean fromPlaneTop = viewingPlaneFromTop();
		
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		FlagEnumeration fe = getSelection().getEnumeration();
		
		Point dataPos = null;
		double xVals[] = new double[explanKey.length];
		
		Point fitPos = null;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			double x = xe[0].nextDouble();
			double z = xe[1].nextDouble();
			xVals[0] = x;
			xVals[1] = z;
			dataPos = getScreenPoint(xVals[0], y, xVals[1], dataPos);
			double fit = model.evaluateMean(xVals);
			fitPos = getScreenPoint(xVals[0], fit, xVals[1], fitPos);
			
			boolean nextSel = fe.nextFlag();
			
			if (shadeHandling == USE_OPAQUE)
				g.setColor(((y >= fit) == fromPlaneTop) ? Color.red : kPaleRed);
			else
				g.setColor(Color.red);
			g.drawLine(fitPos.x, fitPos.y, dataPos.x, dataPos.y);
			
			if (nextSel)
				if (showSelectedArrows) {
					xVals[0] = xAxis.getMinOnAxis();
					xVals[1] = zAxis.getMinOnAxis();
					double fitAtOrigin = model.evaluateMean(xVals);
					boolean arrowAbovePlane = fit >= fitAtOrigin;
					
					g.setColor((fromPlaneTop == arrowAbovePlane || shadeHandling != USE_OPAQUE)
																						? Color.black : Color.gray);
					Point zeroFitPoint = getScreenPoint(xVals[0], fit, xVals[1], null);
					drawLine(g, fitPos.x, fitPos.y, zeroFitPoint.x, zeroFitPoint.y, STANDARD, FILLED_HEAD);
					
					Point zeroPoint = getScreenPoint(xVals[0], y, xVals[1], null);
					boolean allOnSameSide = (fitAtOrigin >= y) == (fit >= y);
					g.setColor((fromPlaneTop == arrowAbovePlane || shadeHandling != USE_OPAQUE)
																							? kDarkGreen : kPaleGreen);
					if (allOnSameSide)
						drawLine(g, dataPos.x, dataPos.y, zeroPoint.x, zeroPoint.y, STANDARD, FILLED_HEAD);
					else {
						double zMin = zAxis.getMinOnAxis();
						double xMin = xAxis.getMinOnAxis();
						double a = model.getParameter(0).toDouble();
						double b = model.getParameter(1).toDouble();
						double c = model.getParameter(2).toDouble();
						double zCross = zMin + (y - a - b * xMin - c * zMin) * (z - zMin)
																		/ (b * (x - xMin) + c * (z - zMin));
						double xCross = xMin + (x - xMin) * (zCross - zMin) / (z - zMin);
						Point crossPoint = getScreenPoint(xCross, y, zCross, null);
						g.drawLine(dataPos.x, dataPos.y, crossPoint.x, crossPoint.y);
						
						g.setColor((fromPlaneTop != arrowAbovePlane || shadeHandling != USE_OPAQUE)
																							? kDarkGreen : kPaleGreen);
						drawLine(g, crossPoint.x, crossPoint.y, zeroPoint.x, zeroPoint.y, STANDARD, FILLED_HEAD);
					}
				}
				else
					drawCrossBackground(g, dataPos);
		}
		
		super.drawData(g, shadeHandling);
	}
}
	
