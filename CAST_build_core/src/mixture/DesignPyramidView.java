package mixture;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class DesignPyramidView extends Rotate3DView {
	
	static final private Color kTriangleFillColor = new Color(0xEEEEEE);
	static final private Color kTriangleLineColor = new Color(0x999999);
	static final private Color kTriangleLineDimColor = new Color(0xDDDDDD);
	
	static final private Color kDataColor = Color.black;
	static final private Color kDataDimColor = new Color(0x999999);
	
	static final public Color kRColor = new Color(0x990000);
	static final public Color kRDimColor = new Color(0xFF99CC);
//	static final public Color kRDimColor = new Color(0xCC6666);
	static final public Color kSColor = Color.blue;
	static final public Color kSDimColor = new Color(0x99CCFF);
//	static final public Color kSDimColor = new Color(0x6699FF);
	static final public Color kTColor = new Color(0x006600);		//	green
	static final public Color kTDimColor = new Color(0x88FF88);
//	static final public Color kTDimColor = new Color(0x66CC66);
	static final public Color kUColor = new Color(0x6600CC);		//	purple
	static final public Color kUDimColor = new Color(0xCC99FF);
//	static final public Color kUDimColor = new Color(0xCC66FF);
	
	static final private double kRootThreeOverTwo = Math.sqrt(1.5);
	static final private double kRootTwo = Math.sqrt(2.0);
	
	static final private int kLabelVertGap = 5;
	static final private int kLabelHorizGap = 5;
	
	static final private String k100Percent = "100% ";
	
	static final private double yCentroid = 1.0 / 4.0;
	static final private double xCentroid = (1 + kRootTwo / 4.0) / 4.0;
	static final private double zCentroid = kRootThreeOverTwo * 2.0 / 4.0;
	
	private String rKey, sKey, tKey, uKey;
	
	public DesignPyramidView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String rKey, String sKey, String tKey, String uKey) {
		super(theData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.rKey = rKey;
		this.sKey = sKey;
		this.tKey = tKey;
		this.uKey = uKey;
		setRetainLastSelection(true);
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		Point pr = getScreenPoint(1, 0, 0, 0, null);
		Point ps = getScreenPoint(0, 1, 0, 0, null);
		Point pt = getScreenPoint(0, 0, 1, 0, null);
		Point pu = getScreenPoint(0, 0, 0, 1, null);
		
		boolean r0PlaneVisible = map.getTheta2() > 180;
		boolean s0PlaneVisible = clockwisePoints(pr, pt, pu);
		boolean t0PlaneVisible = clockwisePoints(pr, pu, ps);
		boolean u0PlaneVisible = clockwisePoints(pr, ps, pt);
		
		Polygon poly = null;
		if (firstDifferent(r0PlaneVisible, s0PlaneVisible,  t0PlaneVisible,  u0PlaneVisible))
			poly = createPoly(ps, pt, pu);
		else if (firstDifferent(s0PlaneVisible, r0PlaneVisible, t0PlaneVisible, u0PlaneVisible))
			poly = createPoly(pr, pt, pu);
		else if (firstDifferent(t0PlaneVisible, r0PlaneVisible, s0PlaneVisible, u0PlaneVisible))
			poly = createPoly(pr, ps, pu);
		else if (firstDifferent(u0PlaneVisible, r0PlaneVisible, s0PlaneVisible, t0PlaneVisible))
			poly = createPoly(pr, ps, pt);
		
		else if (r0PlaneVisible && s0PlaneVisible)
		 	poly = createPoly(pr, pt, ps, pu);
		else if (r0PlaneVisible && t0PlaneVisible)
		 	poly = createPoly(pr, ps, pt, pu);
		else if (r0PlaneVisible && u0PlaneVisible)
		 	poly = createPoly(pr, pt, pu, ps);
		else if (s0PlaneVisible && t0PlaneVisible)
		 	poly = createPoly(ps, pr, pt, pu);
		else if (s0PlaneVisible && u0PlaneVisible)
		 	poly = createPoly(ps, pr, pu, pt);
		else if (t0PlaneVisible && u0PlaneVisible)
		 	poly = createPoly(pt, pr, pu, ps);
		
		g.setColor(kTriangleFillColor);
		g.fillPolygon(poly);

		setEdgeColor(g, t0PlaneVisible, u0PlaneVisible);
		g.drawLine(pr.x, pr.y, ps.x, ps.y);
		
		setEdgeColor(g, s0PlaneVisible, u0PlaneVisible);
		g.drawLine(pr.x, pr.y, pt.x, pt.y);
		
		setEdgeColor(g, s0PlaneVisible, t0PlaneVisible);
		g.drawLine(pr.x, pr.y, pu.x, pu.y);
		
		setEdgeColor(g, r0PlaneVisible, u0PlaneVisible);
		g.drawLine(ps.x, ps.y, pt.x, pt.y);
		
		setEdgeColor(g, r0PlaneVisible, t0PlaneVisible);
		g.drawLine(ps.x, ps.y, pu.x, pu.y);
		
		setEdgeColor(g, r0PlaneVisible, s0PlaneVisible);
		g.drawLine(pt.x, pt.y, pu.x, pu.y);
		
		return poly;
	}
	
	private boolean firstDifferent(boolean b1, boolean b2, boolean b3, boolean b4) {
		return b1 != b2 && b1 != b3 && b1 != b4;
	}
	
	private void setEdgeColor(Graphics g, boolean face1Visible, boolean face2Visible) {
		g.setColor(!face1Visible && !face2Visible ? kTriangleLineDimColor : kTriangleLineColor);
	}
	
	private Polygon createPoly(Point p1, Point p2, Point p3) {
		int xPolyCoord[] = new int[4];
		int yPolyCoord[] = new int[4];
		
		xPolyCoord[0] = xPolyCoord[3] = p1.x;
		yPolyCoord[0] = yPolyCoord[3] = p1.y;
		
		xPolyCoord[1] = p2.x;
		yPolyCoord[1] = p2.y;
		
		xPolyCoord[2] = p3.x;
		yPolyCoord[2] = p3.y;
		
		return new Polygon(xPolyCoord, yPolyCoord, 4);
	}
	
	private Polygon createPoly(Point p1, Point p2, Point p3, Point p4) {
		int xPolyCoord[] = new int[5];
		int yPolyCoord[] = new int[5];
		
		xPolyCoord[0] = xPolyCoord[4] = p1.x;
		yPolyCoord[0] = yPolyCoord[4] = p1.y;
		
		xPolyCoord[1] = p2.x;
		yPolyCoord[1] = p2.y;
		
		xPolyCoord[2] = p3.x;
		yPolyCoord[2] = p3.y;
		
		xPolyCoord[3] = p4.x;
		yPolyCoord[3] = p4.y;
		
		return new Polygon(xPolyCoord, yPolyCoord, 5);
	}
	
/*
	private void fillTriangle(Graphics g, Point p1, Point p2, Point p3) {
		g.fillPolygon(createPoly(p1, p2, p3));
	}
*/
	
	private boolean clockwisePoints(Point p1, Point p2, Point p3) {
		while (p1.x > p2.x || p1.x > p3.x) {
			Point pTemp = p1;
			p1 = p2;
			p2 = p3;
			p3 = pTemp;
		}
		
		double grad2 = (p2.y - p1.y) / (double)(p2.x - p1.x);
		double grad3 = (p3.y - p1.y) / (double)(p3.x - p1.x);
		
		return grad2 > grad3;
	}
	
	private Point getScreenPoint(double r, double s, double t, double u, Point p) {
		double y = r;
		double x = s + r * kRootTwo / 4.0;
		double z = kRootThreeOverTwo * (u + s / 2.0 + r / 2.0);
		
		y += (0.5 - yCentroid);
		x += (0.5 - xCentroid);
		z += (0.5 - zCentroid);
		return getScreenPoint(x, y, z, p);
	}
	
	private boolean onFrontFace(double[] propn, boolean[] zeroVisible) {
		boolean pointVisible = false;
		for (int i=0 ; i<propn.length ;i++)
			if (propn[i] == 0.0 && zeroVisible[i])
				pointVisible = true;
		return pointVisible;
	}
	
	private void drawWithinScreen(Graphics g, String vertexName, int nameLength, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		x = Math.max(0, Math.min(getSize().width - nameLength, x));
		y = Math.max(fm.getAscent(), Math.min(getSize().height - fm.getDescent(), y));
		g.drawString(vertexName, x, y);
	}
	
	private void draw100PercentLabel(Graphics g, Point v, String vertexName,
																										Point v1, Point v2, Point v3) {
//		boolean topV = v.y < v1.y && v.y < v2.y && v.y < v3.y;
		boolean bottomV = v.y > v1.y && v.y > v2.y && v.y > v3.y;
		boolean leftV = v.x < v1.x && v.x < v2.x && v.x < v3.x;
		boolean rightV = v.x > v1.x && v.x > v2.x && v.x > v3.x;
		
		FontMetrics fm = g.getFontMetrics();
		vertexName = k100Percent + vertexName;
		int width = fm.stringWidth(vertexName);
		if (bottomV)
			drawWithinScreen(g, vertexName, width, v.x - width / 2,
																					v.y + fm.getAscent() + kLabelVertGap);
		else if (leftV)
			drawWithinScreen(g, vertexName, width, v.x - width - kLabelHorizGap,
																					v.y + fm.getAscent() / 2);
		else if (rightV)
			drawWithinScreen(g, vertexName, width, v.x + kLabelHorizGap,
																					v.y + fm.getAscent() / 2);
		else		// topV or other
			drawWithinScreen(g, vertexName, width, v.x - width / 2,
																					v.y - fm.getDescent() - kLabelVertGap);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		Point pr = getScreenPoint(1, 0, 0, 0, null);
		Point ps = getScreenPoint(0, 1, 0, 0, null);
		Point pt = getScreenPoint(0, 0, 1, 0, null);
		Point pu = getScreenPoint(0, 0, 0, 1, null);
		
		boolean zeroVisible[] = new boolean[4];
		zeroVisible[0] = map.getTheta2() > 180;
		zeroVisible[1] = clockwisePoints(pr, pt, pu);
		zeroVisible[2] = clockwisePoints(pr, pu, ps);
		zeroVisible[3] = clockwisePoints(pr, ps, pt);
		
		NumVariable var[] = new NumVariable[4];
		var[0] = (NumVariable)getVariable(rKey);
		var[1] = (NumVariable)getVariable(sKey);
		var[2] = (NumVariable)getVariable(tKey);
		var[3] = (NumVariable)getVariable(uKey);
		
		g.setColor(zeroVisible[0] && shadeHandling == USE_OPAQUE ? kRDimColor : kRColor);
		draw100PercentLabel(g, pr, var[0].name, ps, pt, pu);
		g.setColor(zeroVisible[1] && shadeHandling == USE_OPAQUE ? kSDimColor : kSColor);
		draw100PercentLabel(g, ps, var[1].name, pr, pt, pu);
		g.setColor(zeroVisible[2] && shadeHandling == USE_OPAQUE ? kTDimColor : kTColor);
		draw100PercentLabel(g, pt, var[2].name, pr, ps, pu);
		g.setColor(zeroVisible[3] && shadeHandling == USE_OPAQUE ? kUDimColor : kUColor);
		draw100PercentLabel(g, pu, var[3].name, pr, ps, pt);
		
		ValueEnumeration ve[] = new ValueEnumeration[4];
		for (int i=0 ; i<4 ; i++)
			ve[i] = var[i].values();
		Point crossPos = null;
		double propn[] = new double[4];
		
		FlagEnumeration fe = getSelection().getEnumeration();
		while (ve[0].hasMoreValues()) {
			boolean selected = fe.nextFlag();
			for (int i=0 ; i<4 ; i++)
				propn[i] = ve[i].nextDouble();
			crossPos = getScreenPoint(propn[0], propn[1], propn[2], propn[3], crossPos);
			g.setColor(selected ? Color.red
										: onFrontFace(propn, zeroVisible) || shadeHandling != USE_OPAQUE ? kDataColor
										: kDataDimColor);
			if (crossPos != null)
				drawBlob(g, crossPos);
		}
	}

//-----------------------------------------------------------------------------------

	static final private int kMinCrossHitDistance = 36;
	
	private boolean draggingPoints = false;
	
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
		NumVariable rVariable = (NumVariable)getVariable(rKey);
		NumVariable sVariable = (NumVariable)getVariable(sKey);
		NumVariable tVariable = (NumVariable)getVariable(tKey);
		NumVariable uVariable = (NumVariable)getVariable(uKey);
		ValueEnumeration re = rVariable.values();
		ValueEnumeration se = sVariable.values();
		ValueEnumeration te = tVariable.values();
		ValueEnumeration ue = uVariable.values();
		
		Point crossPos = null;
		int index = 0;
		int minDist = Integer.MAX_VALUE;
		int minIndex = -1;
		
		while (te.hasMoreValues() && se.hasMoreValues() && te.hasMoreValues() && ue.hasMoreValues()) {
			double rVal = re.nextDouble();
			double sVal = se.nextDouble();
			double tVal = te.nextDouble();
			double uVal = ue.nextDouble();
			crossPos = getScreenPoint(rVal, sVal, tVal, uVal, crossPos);
			int dist = distance(x, y, crossPos);
			if (dist < minDist) {
				minDist = dist;
				minIndex = index;
			}
			index ++;
		}
		
		if (minDist <= kMinCrossHitDistance)
			return new IndexPosInfo(minIndex);
		else if (!draggingPoints)
			return super.getInitialPosition(x, y);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (draggingPoints)
			return getInitialPosition(x, y);
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null || startInfo instanceof IndexPosInfo) {
			setArrowCursor();
			if (startInfo == null)
				getData().clearSelection();
			else {
				int selectionIndex = ((IndexPosInfo)startInfo).itemIndex;
				getData().setSelection(selectionIndex);
			}
			draggingPoints = true;
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (draggingPoints)
			startDrag(toPos);
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (draggingPoints)
			draggingPoints = false;
		else
			super.endDrag(startPos, endPos);
	}

}
	
