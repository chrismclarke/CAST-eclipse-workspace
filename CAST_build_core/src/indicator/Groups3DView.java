package indicator;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class Groups3DView extends RotateDotPlaneView {
//	static public final String GROUPS_3D_PLOT = "groups3D";
	
	static final private Color kGroupLineColor[] = {new Color(0xBBBBBB), new Color(0xFFAAAA),
																							new Color(0x99CCFF), new Color(0x339933)};
	
	public Groups3DView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
		setSelectCrosses(false);
	}
	
	protected Color getPlaneColor() {
		double yAxisAngle = map.getTheta2();
		if (yAxisAngle > 180)
			yAxisAngle = 360 - yAxisAngle;
		
		double colourPropn = yAxisAngle / 90;
		
		return mixColors(Color.lightGray, Color.white, colourPropn);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = getModel();
		boolean fromPlaneTop = viewingPlaneFromTop();
		
		Point crossPos = null;
		Value xVals[] = new Value[2];
		
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration xe = ((NumVariable)getVariable(explanKey[0])).values();
		CatVariable zVar = (CatVariable)getVariable(explanKey[1]);
		ValueEnumeration ze = zVar.values();
		
		g.setColor(Color.black);
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			xVals[0] = xe.nextValue();
			double x = ((NumValue)xVals[0]).toDouble();
			xVals[1] = ze.nextValue();
			int zCat = zVar.labelIndex(xVals[1]);
			crossPos = getScreenPoint(x, y, zCat, crossPos);
			
			double fit = model.evaluateMean(xVals);
			Color baseColor = getCrossColor(zCat);
			if (shadeHandling == USE_OPAQUE)
				g.setColor(((y >= fit) == fromPlaneTop) ? baseColor
														: mixColors(getPlaneColor(), baseColor, 0.5));
			else
				g.setColor(baseColor);
				
			drawCross(g, crossPos);
		}
		g.setColor(getForeground());
	}
	
	protected void drawForeground(Graphics g) {
		MultipleRegnModel model = getModel();
		NumValue minX = new NumValue(xAxis.getMinOnAxis());
		NumValue maxX = new NumValue(xAxis.getMaxOnAxis());
		
		CatVariable zVar = (CatVariable)getVariable(explanKey[1]);
		Value z0Val = zVar.getLabel(0);
		Value z1Val = zVar.getLabel(1);
		
		Value xz[] = new Value[2];
		Point pMin = null;
		Point pMax = null;
		
		xz[0] = minX;
		xz[1] = z0Val;
		double fit = model.evaluateMean(xz);
		pMin = getScreenPoint(minX.toDouble(), fit, 0.0, pMin);
		xz[0] = maxX;
		fit = model.evaluateMean(xz);
		pMax = getScreenPoint(maxX.toDouble(), fit, 0.0, pMax);
		g.setColor(kGroupLineColor[0]);
		g.drawLine(pMin.x, pMin.y, pMax.x, pMax.y);
		
		xz[0] = minX;
		xz[1] = z1Val;
		fit = model.evaluateMean(xz);
		pMin = getScreenPoint(minX.toDouble(), fit, 1.0, pMin);
		xz[0] = maxX;
		fit = model.evaluateMean(xz);
		pMax = getScreenPoint(maxX.toDouble(), fit, 1.0, pMax);
		g.setColor(kGroupLineColor[1]);
		g.drawLine(pMin.x, pMin.y, pMax.x, pMax.y);
	}

}
	
