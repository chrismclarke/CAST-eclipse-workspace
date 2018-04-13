package mixture;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class TriangleConstraintView extends Rotate3DView {
	
	static final private Color kTriangleFillColor = new Color(0xEEEEEE);
	static final private Color kTriangleLineColor = new Color(0x999999);
	
	static final private String k100Percent = "100% ";
	static final private int kHorizLabelOffset = 6;
	static final private int kVertLabelOffset = 4;
	
	private String xVarName, yVarName, zVarName;
	
	private boolean drawCorners = true;
	private boolean alwaysDrawAxesLabels = true;
	
	public TriangleConstraintView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xVarName, String yVarName, String zVarName) {
		super(theData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.xVarName = xVarName;
		this.yVarName = yVarName;
		this.zVarName = zVarName;
	}
	
	public void setAlwaysDrawAxesLabels(boolean alwaysDrawAxesLabels) {
		this.alwaysDrawAxesLabels = alwaysDrawAxesLabels;
	}
	
	public void setDrawCorners(boolean drawCorners) {
		this.drawCorners = drawCorners;
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		int xPolyCoord[] = new int[4];
		int yPolyCoord[] = new int[4];
		
		Point p0 = getScreenPoint(1.0, 0.0, 0.0, null);
		xPolyCoord[0] = xPolyCoord[3] = p0.x;
		yPolyCoord[0] = yPolyCoord[3] = p0.y;
		
		p0 = getScreenPoint(0.0, 1.0, 0.0, p0);
		xPolyCoord[1] = p0.x;
		yPolyCoord[1] = p0.y;
		
		p0 = getScreenPoint(0.0, 0.0, 1.0, p0);
		xPolyCoord[2] = p0.x;
		yPolyCoord[2] = p0.y;
		
		g.setColor(kTriangleFillColor);
		g.fillPolygon(xPolyCoord, yPolyCoord, 4);
		g.setColor(kTriangleLineColor);
		g.drawPolygon(xPolyCoord, yPolyCoord, 4);
		
		return new Polygon(xPolyCoord, yPolyCoord, 4);
	}
	
	private void drawCorner(Point drawPt, Point p1, Point p2, String drawName, Graphics g) {
		drawBlob(g, drawPt);
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		int baseline = drawPt.y;
		int drawLeft = drawPt.x;
		if (drawPt.y <= p1.y && drawPt.y <= p2.y) {
			baseline -= kVertLabelOffset + descent;
			drawLeft -= fm.stringWidth(drawName) / 2.0;
		}
		else if (drawPt.y >= p1.y && drawPt.y >= p2.y) {
			baseline += kVertLabelOffset + ascent;
			drawLeft -= fm.stringWidth(drawName) / 2.0;
		}
		else if (drawPt.x <= p1.x && drawPt.x <= p2.x) {
			baseline -= ascent / 2.0;
			drawLeft -= kHorizLabelOffset + fm.stringWidth(drawName);
		}
		else {				// (drawPt.x <= p1.x && drawPt.x <= p2.x)
			baseline -= ascent / 2.0;
			drawLeft += kHorizLabelOffset;
		}
		g.drawString(drawName, drawLeft, baseline);
	}
	
	protected void drawAxes(Graphics g, boolean backNotFront, int colourType) {
		boolean onlyShowY = !alwaysDrawAxesLabels && map.getTheta2() > -0.1 && map.getTheta2() < 0.1;
		boolean onlyShowX = !alwaysDrawAxesLabels && map.getTheta1() > -0.1 && map.getTheta1() < 0.1;
		boolean onlyShowZ = !alwaysDrawAxesLabels && (map.getTheta1() > -90.1 && map.getTheta1() < -89.9 || map.getTheta1() > 269.9 && map.getTheta1() < 270.1);
		
		boolean doDrawYAxisLabels = alwaysDrawAxesLabels || !(onlyShowX || onlyShowZ);
		if (map.yAxisBehind() == backNotFront)
			yAxis.draw(g, map, this, colourType, doDrawYAxisLabels);
		
		boolean doDrawXAxisLabels = alwaysDrawAxesLabels  || !(onlyShowY || onlyShowZ);
		if (map.xAxisBehind() == backNotFront)
			xAxis.draw(g, map, this, colourType, doDrawXAxisLabels);
		
		boolean doDrawZAxisLabels = alwaysDrawAxesLabels  || !(onlyShowX || onlyShowY);
		if (map.zAxisBehind() == backNotFront)
			zAxis.draw(g, map, this, colourType, doDrawZAxisLabels);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (drawCorners) {
			Point px = getScreenPoint(1.0, 0.0, 0.0, null);
			Point py = getScreenPoint(0.0, 1.0, 0.0, null);
			Point pz = getScreenPoint(0.0, 0.0, 1.0, null);
			
			g.setColor(getForeground());
			Font oldFont = g.getFont();
			g.setFont(getApplet().getBigBoldFont());
			
			drawCorner(px, py, pz, k100Percent + xVarName, g);
			drawCorner(py, px, pz, k100Percent + yVarName, g);
			drawCorner(pz, py, px, k100Percent + zVarName, g);
			
			g.setFont(oldFont);
		}
	}
}
	
