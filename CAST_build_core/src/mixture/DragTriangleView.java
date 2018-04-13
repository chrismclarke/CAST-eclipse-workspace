package mixture;

import java.awt.*;

import dataView.*;

public class DragTriangleView extends ConstrainedTriangleView {
//	static public final String DRAG_TRIANGLE = "dragTrangle";
	
	public DragTriangleView(DataSet theData, XApplet applet, String xKey, String yKey, String zKey) {
		super(theData, applet, xKey, yKey, zKey);
		setRetainLastSelection(false);
	}
	
	protected void drawData(Graphics g) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable zVariable = (NumVariable)getVariable(zKey);
		
		double x = xVariable.doubleValueAt(0);
		double y = yVariable.doubleValueAt(0);
		double z = zVariable.doubleValueAt(0);
		
		if (!Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z)) {
			Point crossPos = getScreenPoint(x, y, z, null);
			g.setColor(Color.red);
			drawBlob(g, crossPos);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point p = translateFromScreen(x, y, null);
		
		double yInTriangle = (p.y - vertOrigin) / (double)unitPix;
		double xInTriangle = (kRootThree * (p.x - horizOrigin) / (double)unitPix - yInTriangle) / 2.0;
		
		double yPropn = yInTriangle * (yMax - yMin) + yMin;
		double xPropn = xInTriangle * (xMax - xMin) + xMin;
		double zPropn = sumXYZ - yPropn - xPropn;
		
		if (validDesignPoint(xPropn, yPropn, zPropn))
			return new MixturePosInfo(xPropn, yPropn, zPropn);
		else
			return null;
	}
	
	private double minAllowed(double min, double minConstr) {
		if (!Double.isNaN(minConstr))
			min = Math.max(min, minConstr);
		return min;
	}
	
	private double maxAllowed(double max, double maxConstr) {
		if (!Double.isNaN(maxConstr))
			max = Math.min(max, maxConstr);
		return max;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point p = translateFromScreen(x, y, null);
		
		double yInTriangle = (p.y - vertOrigin) / (double)unitPix;
		double xInTriangle = (kRootThree * (p.x - horizOrigin) / (double)unitPix - yInTriangle) / 2.0;
		
		double yPropn = yInTriangle * (yMax - yMin) + yMin;
		double xPropn = xInTriangle * (xMax - xMin) + xMin;
		double zPropn = sumXYZ - yPropn - xPropn;
		
		double xRange = 0.1 * (xMax - xMin);
		if (xPropn < xMin - xRange || xPropn > xMax + xRange)
			return null;
		double yRange = 0.1 * (yMax - yMin);
		if (yPropn < yMin - yRange || yPropn > yMax + yRange)
			return null;
		double zRange = 0.1 * (zMax - zMin);
		if (zPropn < zMin - zRange || zPropn > zMax + zRange)
			return null;
		
		double minXP = minAllowed(xMin, minConstraint[X_AXIS]);
		double minYP = minAllowed(yMin, minConstraint[Y_AXIS]);
		double minZP = minAllowed(zMin, minConstraint[Z_AXIS]);
		
		double maxXP = maxAllowed(xMax, maxConstraint[X_AXIS]);
		double maxYP = maxAllowed(yMax, maxConstraint[Y_AXIS]);
		double maxZP = maxAllowed(zMax, maxConstraint[Z_AXIS]);
		
		if (xPropn >= maxXP && yPropn <= minYP && zPropn <= minZP) {
			xPropn = maxXP;
			yPropn = minYP;
			zPropn = minZP;
		}
		else if (yPropn >= maxYP && xPropn <= minXP && zPropn <= minZP) {
			yPropn = maxYP;
			xPropn = minXP;
			zPropn = minZP;
		}
		else if (zPropn >= maxZP && yPropn <= minYP && xPropn <= minXP) {
			zPropn = maxZP;
			yPropn = minYP;
			xPropn = minXP;
		}
		
		while (true) {
			if (xPropn < minXP || xPropn > maxXP) {
				double change = (xPropn < minXP) ? (minXP - xPropn) : (maxXP - xPropn);
				xPropn += change;
				yPropn -= 0.5 * change;
				zPropn -= 0.5 * change;
			}
			else if (yPropn < minYP || yPropn > maxYP) {
				double change = (yPropn < minYP) ? (minYP - yPropn) : (maxYP - yPropn);
				yPropn += change;
				xPropn -= 0.5 * change;
				zPropn -= 0.5 * change;
			}
			else if (zPropn < minZP || zPropn > maxZP) {
				double change = (zPropn < minZP) ? (minZP - zPropn) : (maxZP - zPropn);
				zPropn += change;
				yPropn -= 0.5 * change;
				xPropn -= 0.5 * change;
			}
			else
				break;
		}
		
		return new MixturePosInfo(xPropn, yPropn, zPropn);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		MixturePosInfo posInfo = (MixturePosInfo)startInfo;
		
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable zVariable = (NumVariable)getVariable(zKey);
		
		((NumValue)xVariable.valueAt(0)).setValue(posInfo.x);
		((NumValue)yVariable.valueAt(0)).setValue(posInfo.y);
		((NumValue)zVariable.valueAt(0)).setValue(posInfo.z);
		
		getData().valueChanged(0);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null)
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable zVariable = (NumVariable)getVariable(zKey);
		
		((NumValue)xVariable.valueAt(0)).setValue(Double.NaN);
		((NumValue)yVariable.valueAt(0)).setValue(Double.NaN);
		((NumValue)zVariable.valueAt(0)).setValue(Double.NaN);
		
		getData().valueChanged(0);
	}
}
