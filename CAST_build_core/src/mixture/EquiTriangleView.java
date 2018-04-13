package mixture;

import java.awt.*;

import dataView.*;

public class EquiTriangleView extends DataView {
//	static public final String EQUI_TRIANGLE = "equiTrangle";
	
	static final public int Y_AXIS = 0;
	static final public int X_AXIS = 1;
	static final public int Z_AXIS = 2;
	
	static final private int kLabelHorizGap = 4;
	static final private int kLabelVertGap = 2;
	
	static final protected double kRootThree = Math.sqrt(3.0);
	
	static final private Color kYScaleColor = Color.blue;
	static final private Color kYScaleDimColor = new Color(0xBBBBFF);
	static final private Color kXScaleColor = Color.red;
	static final private Color kXScaleDimColor = new Color(0xFF9999);
	static final private Color kZScaleColor = new Color(0x009900);		//	dark green
	static final private Color kZScaleDimColor = new Color(0x55FF55);
	
	static final private NumValue kDefaultAxisLabel[] = new NumValue[6];
	static final private double kDefaultAxisPropn[] = new double[6];
	static {
		for (int i=0 ; i<kDefaultAxisLabel.length ; i++) {
			kDefaultAxisPropn[i] = i * 0.2;
			kDefaultAxisLabel[i] = new NumValue(kDefaultAxisPropn[i], 1);
		}
	}
	
	protected String xKey, yKey, zKey;
	
	protected double xMin = 0.0, xMax = 1.0;
	protected double yMin = 0.0, yMax = 1.0;
	protected double zMin = 0.0, zMax = 1.0;
	
	protected double sumXYZ = 1.0;
	
	private boolean initialised = false;
	
	private int maxLabelWidth;
	private int ascent, descent;
	protected double unitPix, horizOrigin, vertOrigin;
	
	private boolean gridOverTriangle = true;
	
	public EquiTriangleView(DataSet theData, XApplet applet, String xKey, String yKey, String zKey) {
		super(theData, applet, null);
		this.xKey = xKey;
		this.yKey = yKey;
		this.zKey = zKey;
		setRetainLastSelection(true);
	}
	
	public void setGridOverTriangle(boolean gridOverTriangle) {
		this.gridOverTriangle = gridOverTriangle;
	}
	
	protected Value[] getAxisLabel(int varIndex) {
		return kDefaultAxisLabel;
	}
	
	protected double[] getAxisPropn(int varIndex) {
		return kDefaultAxisPropn;
	}
	
	protected final void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		maxLabelWidth = 0;
		for (int i=0 ; i<3 ; i++) {
			Value axisLabels[] = getAxisLabel(i);
			for (int j=0 ; j<axisLabels.length ; j++)
				maxLabelWidth = Math.max(maxLabelWidth, axisLabels[j].stringWidth(g));
		}
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		int maxAvailHt = getSize().height - 3 * ascent  - 2 * (descent + kLabelVertGap);
		int maxAvailWidth = getSize().width - 2 * (maxLabelWidth + kLabelHorizGap);
		unitPix = Math.min(maxAvailHt / 1.5, maxAvailWidth * kRootThree / 2.5);
		
		double widthUsed = 2.5 / kRootThree * unitPix;
		double horizSpare = (maxAvailWidth - widthUsed);
		horizOrigin = maxLabelWidth + kLabelHorizGap + horizSpare / 2.0;
		double vertSpare = maxAvailHt - 1.5 * unitPix;
		vertOrigin = ascent + descent + kLabelVertGap + 0.5 * unitPix + vertSpare / 2.0;
	}
	
	protected Point getScreenPoint(double x, double y, double z, Point p) {
		double scaledX = (x - xMin) / (xMax - xMin);
		double scaledY = (y - yMin) / (yMax - yMin);
//		double scaledZ = (z - zMin) / (zMax - zMin);
		
		int vert = (int)Math.round(vertOrigin + scaledY * unitPix);
		int horiz = (int)Math.round(horizOrigin + (scaledY + 2.0 * scaledX) * unitPix / kRootThree);
		return translateToScreen(horiz, vert, p);
	}
	
	protected void shadeConstraints(Graphics g) {
	}
	
	protected void fillTriangle(Graphics g) {
		int xCoord[] = new int[4];
		int yCoord[] = new int[4];
		
		Point p = getScreenPoint(xMax, yMin, zMin, null);
		xCoord[0] = xCoord[3] = p.x;
		yCoord[0] = yCoord[3] = p.y;
		
		p = getScreenPoint(xMin, yMax, zMin, p);
		xCoord[1] = p.x;
		yCoord[1] = p.y;
		
		p = getScreenPoint(xMin, yMin, zMax, p);
		xCoord[2] = p.x;
		yCoord[2] = p.y;
		
		g.setColor(Color.white);
		g.fillPolygon(xCoord, yCoord, 4);
		
		shadeConstraints(g);
		
		g.setColor(Color.black);
		g.drawPolygon(xCoord, yCoord, 4);
	}
	
	private void drawYScale(Graphics g) {
		Point p0 = null;
		Point p1 = null;
		Value yAxisLabel[] = getAxisLabel(Y_AXIS);
		for (int i=0 ; i<yAxisLabel.length ; i++) {
			double y = getAxisPropn(Y_AXIS)[i];
			p0 = getScreenPoint(xMin - 0.5 * (y - yMin), y, sumXYZ - xMin - 0.5 * (y + yMin), p0);
			g.setColor(kYScaleColor);
			yAxisLabel[i].drawLeft(g, p0.x - kLabelHorizGap, p0.y + ascent / 2);
			
			if (y > yMin) {
				g.setColor(kYScaleDimColor);
				if (gridOverTriangle)
					p1 = getScreenPoint(sumXYZ - y - zMin, y, zMin, p1);
				else
					p1 = getScreenPoint(xMin, y, sumXYZ - y - xMin, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
		
		g.setColor(kYScaleColor);
		p0 = getScreenPoint(xMin, yMin, zMax, p0);
		p1 = getScreenPoint(xMin - 0.5 * (xMax - xMin), yMax, zMin + 0.5 * (zMax - zMin), p1);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		String yName = getVariable(yKey).name;
		g.drawString(yName, p1.x - kLabelHorizGap - maxLabelWidth,
																		p1.y - ascent / 2 - descent - kLabelVertGap);
	}
	
	private void drawXScale(Graphics g) {
		Point p0 = null;
		Point p1 = null;
		Value xAxisLabel[] = getAxisLabel(X_AXIS);
		for (int i=0 ; i<xAxisLabel.length ; i++) {
			double x = getAxisPropn(X_AXIS)[i];
			p0 = getScreenPoint(x, sumXYZ - zMin - 0.5 * (x + xMin), zMin - 0.5 * (x - xMin), p0);
			g.setColor(kXScaleColor);
			xAxisLabel[i].drawRight(g, p0.x, p0.y);
			
			if (x > xMin) {
				g.setColor(kXScaleDimColor);
				if (gridOverTriangle)
					p1 = getScreenPoint(x, yMin, sumXYZ - x - yMin, p1);
				else
					p1 = getScreenPoint(x, sumXYZ - x - zMin, zMin, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
		
		g.setColor(kXScaleColor);
		p0 = getScreenPoint(xMin, yMax, zMin, p0);
		p1 = getScreenPoint(xMax, yMin + 0.5 * (yMax - yMin), zMin - 0.5 * (zMax - zMin), p1);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		double nameX = 0.5 * (xMax + xMin);
		double nameZ = zMin - 0.25 * (zMax - zMin);
		p0 = getScreenPoint(nameX, sumXYZ - nameX - nameZ, nameZ, p0);
		String xName = getVariable(xKey).name;
		g.drawString(xName, p0.x + kLabelHorizGap + maxLabelWidth / 2, p0.y - ascent);
	}
	
	private void drawZScale(Graphics g) {
		Point p0 = null;
		Point p1 = null;
		Value zAxisLabel[] = getAxisLabel(Z_AXIS);
		for (int i=0 ; i<zAxisLabel.length ; i++) {
			double z = getAxisPropn(Z_AXIS)[i];
			p0 = getScreenPoint(sumXYZ - yMin - 0.5 * (z + zMin), yMin - 0.5 * (z - zMin), z, p0);
			g.setColor(kZScaleColor);
			zAxisLabel[i].drawRight(g, p0.x, p0.y + ascent);
			
			if (z > zMin) {
				g.setColor(kZScaleDimColor);
				if (gridOverTriangle)
					p1 = getScreenPoint(xMin, sumXYZ - z - xMin, z, p1);
				else
					p1 = getScreenPoint(sumXYZ - z - yMin, yMin, z, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
		
		g.setColor(kZScaleColor);
		p0 = getScreenPoint(xMax, yMin, zMin, null);
		p1 = getScreenPoint(xMin + 0.5 * (xMax - xMin), yMin - 0.5 * (yMax - yMin), zMax, null);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		double nameZ = 0.5 * (zMax + zMin);
		double nameY = yMin - 0.25 * (yMax - yMin);
		p0 = getScreenPoint(sumXYZ - nameY - nameZ, nameY, nameZ, p0);
		String zName = getVariable(zKey).name;
		g.drawString(zName, p0.x + kLabelHorizGap + maxLabelWidth / 2, p0.y + 2 * ascent);
	}
	
	protected void drawData(Graphics g) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable zVariable = (NumVariable)getVariable(zKey);
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		ValueEnumeration ze = zVariable.values();
		Point crossPos = null;
		
		FlagEnumeration fe = getSelection().getEnumeration();
		while (xe.hasMoreValues() && ye.hasMoreValues() && ze.hasMoreValues()) {
			boolean selected = fe.nextFlag();
			double x = xe.nextDouble();
			double y = ye.nextDouble();
			double z = ze.nextDouble();
			if (!Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z)) {
				crossPos = getScreenPoint(x, y, z, crossPos);
				g.setColor(selected ? Color.red : getForeground());
				if (crossPos != null)
					drawBlob(g, crossPos);
			}
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		fillTriangle(g);
		drawYScale(g);
		drawXScale(g);
		drawZScale(g);
		
		drawData(g);
	}
	
	public void resetDisplay() {
		initialised = false;
	}

//-----------------------------------------------------------------------------------
	
	static final private int kMinCrossHitDistance = 36;
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	private int distance(int x, int y, Point crossPos) {
		int xDist = crossPos.x - x;
		int yDist = crossPos.y - y;
		return xDist * xDist + yDist * yDist;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable zVariable = (NumVariable)getVariable(zKey);
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		ValueEnumeration ze = zVariable.values();
		
		Point crossPos = null;
		int index = 0;
		int minDist = Integer.MAX_VALUE;
		int minIndex = -1;
		
		while (xe.hasMoreValues() && ye.hasMoreValues() && ze.hasMoreValues()) {
			double xVal = xe.nextDouble();
			double yVal = ye.nextDouble();
			double zVal = ze.nextDouble();
			crossPos = getScreenPoint(xVal, yVal, zVal, crossPos);
			int dist = distance(x, y, crossPos);
			if (dist < minDist) {
				minDist = dist;
				minIndex = index;
			}
			index ++;
		}
		
		if (minDist <= kMinCrossHitDistance)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		return getInitialPosition(x, y);
	}
}
