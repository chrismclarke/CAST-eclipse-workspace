package mixture;

import java.awt.*;

import dataView.*;

public class ConstrainedTriangleView extends EquiTriangleView {
//	static public final String CONSTRAINED_TRIANGLE = "constrainedTrangle";
	
	static final private Color kConstraintFillColor = new Color(0xDDDDDD);
	static final private Color kConstraintBorderColor = new Color(0xBBBBBB);
	
	private Value axisLabel[][] = new Value[3][];
	private double axisPropn[][] = new double[3][];
	
	protected double minConstraint[] = {Double.NaN, Double.NaN, Double.NaN};
	protected double maxConstraint[] = {Double.NaN, Double.NaN, Double.NaN};
	
	public ConstrainedTriangleView(DataSet theData, XApplet applet,
													String xKey, String yKey, String zKey) {
		super(theData, applet, xKey, yKey, zKey);
	}
	
	public void setAxisMinMax(int varIndex, double min, double max) {
		if (varIndex == X_AXIS) {
			xMin = min;
			xMax = max;
		}
		else if (varIndex == Y_AXIS) {
			yMin = min;
			yMax = max;
		}
		else {
			zMin = min;
			zMax = max;
		}
	}
	
	public void setAxisLabels(int varIndex, Value[] label, double[] propn) {
		axisLabel[varIndex] = label;
		axisPropn[varIndex] = propn;
	}
	
	public void setSumXYZ(double sumXYZ) {
		this.sumXYZ = sumXYZ;
	}
	
	public void setConstraints(int varIndex, double varMin, double varMax) {
		minConstraint[varIndex] = varMin;
		maxConstraint[varIndex] = varMax;
	}
	
	protected Value[] getAxisLabel(int varIndex) {
		return axisLabel[varIndex];
	}
	
	protected double[] getAxisPropn(int varIndex) {
		return axisPropn[varIndex];
	}
	
	private boolean satisfiesConstraint(double value, int varIndex) {
		if (!Double.isNaN(minConstraint[varIndex]) && value < minConstraint[varIndex])
			return false;
		if (!Double.isNaN(maxConstraint[varIndex]) && value > maxConstraint[varIndex])
			return false;
		return true;
	}
	
	protected boolean validDesignPoint(double x, double y, double z) {
		boolean inTriangle =  x >= xMin && x <= xMax && z >= zMin && z <= zMax &&
																														y >= yMin && y <= yMax;
		if (!inTriangle)
			return false;
		
		return satisfiesConstraint(x, X_AXIS) && satisfiesConstraint(y, Y_AXIS)
																							&& satisfiesConstraint(z, Z_AXIS);
	}
	
	private void fillMaxConstraint(Graphics g, int[] xCoord, int[] yCoord, int apexIndex,
																													double constraint, Point p) {
		double min[] = {xMin, yMin, zMin};
		double max[] = {xMax, yMax, zMax};
		int otherIndex1 = (apexIndex + 1) % 3;
		int otherIndex2 = (apexIndex + 2) % 3;
		
		double xyz[] = {xMin, yMin, zMin};
		xyz[apexIndex] = max[apexIndex];
		p = getScreenPoint(xyz[0], xyz[1], xyz[2], p);
		xCoord[0] = xCoord[3] = p.x;
		yCoord[0] = yCoord[3] = p.y;
		
		xyz[apexIndex] = constraint;
		xyz[otherIndex1] = sumXYZ - constraint - min[otherIndex2];
		xyz[otherIndex2] = min[otherIndex2];
		p = getScreenPoint(xyz[0], xyz[1], xyz[2], p);
		xCoord[1] = p.x;
		yCoord[1] = p.y;
		
		xyz[otherIndex2] = sumXYZ - constraint - min[otherIndex1];
		xyz[otherIndex1] = min[otherIndex1];
		p = getScreenPoint(xyz[0], xyz[1], xyz[2], p);
		xCoord[2] = p.x;
		yCoord[2] = p.y;
		
		g.fillPolygon(xCoord, yCoord, 4);
	}
	
	private void fillMinConstraint(Graphics g, int[] xCoord, int[] yCoord, int apexIndex,
																													double constraint, Point p) {
		double min[] = {xMin, yMin, zMin};
		double max[] = {xMax, yMax, zMax};
		int otherIndex1 = (apexIndex + 1) % 3;
		int otherIndex2 = (apexIndex + 2) % 3;
		
		double xyz[] = {xMin, yMin, zMin};
		
		xyz[otherIndex1] = max[otherIndex1];
		p = getScreenPoint(xyz[0], xyz[1], xyz[2], p);
		xCoord[0] = xCoord[4] = p.x;
		yCoord[0] = yCoord[4] = p.y;
		
		xyz[otherIndex1] = min[otherIndex1];
		xyz[otherIndex2] = max[otherIndex2];
		p = getScreenPoint(xyz[0], xyz[1], xyz[2], p);
		xCoord[1] = p.x;
		yCoord[1] = p.y;
		
		xyz[apexIndex] = constraint;
		xyz[otherIndex2] = sumXYZ - constraint - min[otherIndex1];
		p = getScreenPoint(xyz[0], xyz[1], xyz[2], p);
		xCoord[2] = p.x;
		yCoord[2] = p.y;
		
		xyz[otherIndex1] = sumXYZ - constraint - min[otherIndex2];
		xyz[otherIndex2] = min[otherIndex2];
		p = getScreenPoint(xyz[0], xyz[1], xyz[2], p);
		xCoord[3] = p.x;
		yCoord[3] = p.y;
		
		g.fillPolygon(xCoord, yCoord, 5);
	}
	
	private void drawConstraintLine(Graphics g, int apexIndex, double constraint,
																																		Point p0, Point p1) {
		double min[] = {xMin, yMin, zMin};
//		double max[] = {xMax, yMax, zMax};
		int otherIndex1 = (apexIndex + 1) % 3;
		int otherIndex2 = (apexIndex + 2) % 3;
		
		double xyz[] = {constraint, constraint, constraint};
		xyz[otherIndex1] = min[otherIndex1];
		xyz[otherIndex2] = sumXYZ - constraint - min[otherIndex1];
		p0 = getScreenPoint(xyz[0], xyz[1], xyz[2], p0);
		
		xyz[otherIndex2] = min[otherIndex2];
		xyz[otherIndex1] = sumXYZ - constraint - min[otherIndex2];
		p1 = getScreenPoint(xyz[0], xyz[1], xyz[2], p1);
		
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
	}
	
	protected void shadeConstraints(Graphics g) {
		g.setColor(kConstraintFillColor);
		int xCoord[] = new int[5];
		int yCoord[] = new int[5];
		Point p = new Point(0, 0);
		
		double xMaxConstr = maxConstraint[X_AXIS];
		if (!Double.isNaN(xMaxConstr)) {
			xMaxConstr = Math.min(xMax, Math.max(xMin, xMaxConstr));
			fillMaxConstraint(g, xCoord, yCoord, 0, xMaxConstr, p);
		}
		
		double xMinConstr = minConstraint[X_AXIS];
		if (!Double.isNaN(xMinConstr)) {
			xMinConstr = Math.min(xMax, Math.max(xMin, xMinConstr));
			fillMinConstraint(g, xCoord, yCoord, 0, xMinConstr, p);
		}
		
		double yMaxConstr = maxConstraint[Y_AXIS];
		if (!Double.isNaN(yMaxConstr)) {
			yMaxConstr = Math.min(yMax, Math.max(yMin, yMaxConstr));
			fillMaxConstraint(g, xCoord, yCoord, 1, yMaxConstr, p);
		}
		
		double yMinConstr = minConstraint[Y_AXIS];
		if (!Double.isNaN(yMinConstr)) {
			yMinConstr = Math.min(yMax, Math.max(yMin, yMinConstr));
			fillMinConstraint(g, xCoord, yCoord, 1, yMinConstr, p);
		}
		
		double zMaxConstr = maxConstraint[Z_AXIS];
		if (!Double.isNaN(zMaxConstr)) {
			zMaxConstr = Math.min(zMax, Math.max(zMin, zMaxConstr));
			fillMaxConstraint(g, xCoord, yCoord, 2, zMaxConstr, p);
		}
		
		double zMinConstr = minConstraint[Z_AXIS];
		if (!Double.isNaN(zMinConstr)) {
			zMinConstr = Math.min(zMax, Math.max(zMin, zMinConstr));
			fillMinConstraint(g, xCoord, yCoord, 2, zMinConstr, p);
		}
		
		g.setColor(kConstraintBorderColor);
		Point p2 = new Point(0, 0);
		
		if (!Double.isNaN(xMaxConstr) && xMaxConstr > xMin && xMaxConstr < xMax)
			drawConstraintLine(g, 0, xMaxConstr, p, p2);
		
		if (!Double.isNaN(xMinConstr) && xMinConstr > xMin && xMinConstr < xMax)
			drawConstraintLine(g, 0, xMinConstr, p, p2);
		
		if (!Double.isNaN(yMaxConstr) && yMaxConstr > yMin && yMaxConstr < yMax)
			drawConstraintLine(g, 1, yMaxConstr, p, p2);
		
		if (!Double.isNaN(yMinConstr) && yMinConstr > xMin && yMinConstr < xMax)
			drawConstraintLine(g, 1, yMinConstr, p, p2);
		
		if (!Double.isNaN(zMaxConstr) && zMaxConstr > zMin && zMaxConstr < zMax)
			drawConstraintLine(g, 2, zMaxConstr, p, p2);
		
		if (!Double.isNaN(zMinConstr) && zMinConstr > zMin && zMinConstr < zMax)
			drawConstraintLine(g, 2, zMinConstr, p, p2);
	}
}
