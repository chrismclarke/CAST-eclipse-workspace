package responseSurface;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class SurfaceDesignView extends Rotate3DView {
	
	static final private Color kCubeFillColor = new Color(0xEEEEEE);
	static final private Color kCubeLineColor = new Color(0x999999);
	static final private Color kCubeLineDimColor = new Color(0xDDDDDD);
	
	static final private Color kFactorialColor = Color.black;
	static final private Color kFactorialDimColor = new Color(0x999999);
	
	static final private Color kStarColor = new Color(0x990000);		//	dark red
	static final private Color kStarDimColor = new Color(0xB39198);
	
	static final private Color kStarLineColor = new Color(0xBC948D);
	static final private Color kStarLineDimColor = new Color(0xCBC0B8);
	
	static final private Color kCenterColor = new Color(0x000099);	//	dark blue
	
	static final private Color kBbColor = new Color(0x006600);				//	dark green
	static final private Color kBbDimColor = new Color(0x99FF99);
	
	static final private int kCountHorizOffset = 5;
	static final private int kCountVertOffset = 4;
	
	private double minX, maxX, minY, maxY, minZ, maxZ;
	
	private int factorialReplicates, starReplicates, centerReplicates, bbReplicates;
	
	public SurfaceDesignView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
						int factorialReplicates, int starReplicates, int centerReplicates, int bbReplicates) {
		super(theData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minZ = minZ;
		this.maxZ = maxZ;
		this.factorialReplicates = factorialReplicates;
		this.starReplicates = starReplicates;
		this.centerReplicates = centerReplicates;
		this.bbReplicates = bbReplicates;
	}
	
	public void setFactorialReplicates(int factorialReplicates) {
		this.factorialReplicates = factorialReplicates;
	}
	
	public void setStarReplicates(int starReplicates) {
		this.starReplicates = starReplicates;
	}
	
	public void setCenterReplicates(int centerReplicates) {
		this.centerReplicates = centerReplicates;
	}
	
	public void setBbReplicates(int bbReplicates) {
		this.bbReplicates = bbReplicates;
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		boolean highFrontX = map.zAxisBehind();
		boolean highFrontZ = map.xAxisBehind();
		boolean highFrontY = map.getTheta2() <= 90;
		
		int xPolyCoord[] = new int[7];
		int yPolyCoord[] = new int[7];
		
		double farX = highFrontX ? minX : maxX;
		double farZ = highFrontZ ? minZ : maxZ;
		double farY = highFrontY ? minY : maxY;
		double nearX = highFrontX ? maxX : minX;
		double nearZ = highFrontZ ? maxZ : minZ;
		double nearY = highFrontY ? maxY : minY;
		
		Point p0 = getScreenPoint(farX, nearY, farZ, null);
		xPolyCoord[0] = xPolyCoord[6] = p0.x;
		yPolyCoord[0] = yPolyCoord[6] = p0.y;
		
		p0 = getScreenPoint(farX, nearY, nearZ, p0);
		xPolyCoord[1] = p0.x;
		yPolyCoord[1] = p0.y;
		
		p0 = getScreenPoint(farX, farY, nearZ, p0);
		xPolyCoord[2] = p0.x;
		yPolyCoord[2] = p0.y;
		
		p0 = getScreenPoint(nearX, farY, nearZ, p0);
		xPolyCoord[3] = p0.x;
		yPolyCoord[3] = p0.y;
		
		p0 = getScreenPoint(nearX, farY, farZ, p0);
		xPolyCoord[4] = p0.x;
		yPolyCoord[4] = p0.y;
		
		p0 = getScreenPoint(nearX, nearY, farZ, p0);
		xPolyCoord[5] = p0.x;
		yPolyCoord[5] = p0.y;
		
		g.setColor(kCubeFillColor);
		g.fillPolygon(xPolyCoord, yPolyCoord, 7);
		return new Polygon(xPolyCoord, yPolyCoord, 7);
	}
	
	private void drawCube(Graphics g, int shadeHandling) {
		boolean highFrontX = map.zAxisBehind();
		boolean highFrontZ = map.xAxisBehind();
		boolean highFrontY = map.getTheta2() <= 90;
		
		g.setColor(kCubeLineColor);
		Point p0 = null;
		Point p1 = null;
		
		for (double i=0 ; i<2 ; i++) {
			double x = (i == 0) ? minX : maxX;
			boolean backPlane = (i == 0) == highFrontX;
			
			p0 = getScreenPoint(x, minY, minZ, p0);
			p1 = getScreenPoint(x, minY, maxZ, p1);
			boolean behind = shadeHandling == USE_OPAQUE && backPlane && highFrontY;
			g.setColor(behind ? kCubeLineDimColor : kCubeLineColor);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			p0 = getScreenPoint(x, maxY, maxZ, p0);
			behind = shadeHandling == USE_OPAQUE && backPlane && !highFrontZ;
			g.setColor(behind ? kCubeLineDimColor : kCubeLineColor);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			p1 = getScreenPoint(x, maxY, minZ, p1);
			behind = shadeHandling == USE_OPAQUE && backPlane && !highFrontY;
			g.setColor(behind ? kCubeLineDimColor : kCubeLineColor);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			p0 = getScreenPoint(x, minY, minZ, p0);
			behind = shadeHandling == USE_OPAQUE && backPlane && highFrontZ;
			g.setColor(behind ? kCubeLineDimColor : kCubeLineColor);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		
		for (double i=0 ; i<2 ; i++) {
			double y = (i == 0) ? minY : maxY;
			for (double j=0 ; j<2 ; j++) {
				double z = (j == 0) ? minZ : maxZ;
				
				boolean behind = shadeHandling == USE_OPAQUE && (highFrontY == (i == 0)) && (highFrontZ == (j == 0));
				g.setColor(behind ? kCubeLineDimColor : kCubeLineColor);
				p0 = getScreenPoint(minX, y, z, p0);
				p1 = getScreenPoint(maxX, y, z, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
	}
	
	private void drawBlobAndCount(Graphics g, Point p, Color c, int replicates) {
		g.setColor(c);
		drawBlob(g, p);
		g.drawString(String.valueOf(replicates), p.x + kCountHorizOffset, p.y - kCountVertOffset);
	}
	
	private void drawFactorialPoints(Graphics g, boolean useGrayShading, boolean highFrontX,
						boolean highFrontZ, boolean highFrontY) {
		if (factorialReplicates == 0)
			return;
		Point p0 = null;
		
		for (double i=0 ; i<2 ; i++) {
			double x = (i == 0) ? minX : maxX;
			for (double j=0 ; j<2 ; j++) {
				double y = (j == 0) ? minY : maxY;
				for (double k=0 ; k<2 ; k++) {
					double z = (k == 0) ? minZ : maxZ;
					p0 = getScreenPoint(x, y, z, p0);
					boolean behind = useGrayShading && (highFrontX == (i == 0))
																		&& (highFrontY == (j == 0)) && (highFrontZ == (k == 0));
					drawBlobAndCount(g, p0, behind ? kFactorialDimColor : kFactorialColor,
																																	factorialReplicates);
				}
			}
		}
	}
	
	private void drawCenterPoints(Graphics g, boolean useGrayShading, boolean highFrontX,
						boolean highFrontZ, boolean highFrontY) {
		if (centerReplicates > 0) {
			Point p0 = getScreenPoint((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2, null);
			drawBlobAndCount(g, p0, kCenterColor, centerReplicates);
		}
	}
	
	private void drawStarPoints(Graphics g, boolean useGrayShading, boolean highFrontX,
						boolean highFrontZ, boolean highFrontY) {
		if (starReplicates == 0)
			return;
		Point p0 = null;
		Point p1 = null;
		
		double lowXStar = minX - 0.34089641525 * (maxX - minX);
		double highXStar = maxX + 0.34089641525 * (maxX - minX);
		
		p0 = getScreenPoint(lowXStar, (minY + maxY) / 2, (minZ + maxZ) / 2, p0);
		p1 = getScreenPoint(minX, (minY + maxY) / 2, (minZ + maxZ) / 2, p1);
		boolean behind = useGrayShading && highFrontX;
		g.setColor(behind ? kStarLineDimColor : kStarLineColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		drawBlobAndCount(g, p0, behind ? kStarDimColor : kStarColor, starReplicates);
		
		p0 = getScreenPoint(highXStar, (minY + maxY) / 2, (minZ + maxZ) / 2, p0);
		p1 = getScreenPoint(maxX, (minY + maxY) / 2, (minZ + maxZ) / 2, p1);
		behind = useGrayShading && !highFrontX;
		g.setColor(behind ? kStarLineDimColor : kStarLineColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		drawBlobAndCount(g, p0, behind ? kStarDimColor : kStarColor, starReplicates);
		
		double lowYStar = minY - 0.34089641525 * (maxY - minY);
		double highYStar = maxY + 0.34089641525 * (maxY - minY);
		
		p0 = getScreenPoint((minX + maxX) / 2, lowYStar, (minZ + maxZ) / 2, p0);
		p1 = getScreenPoint((minX + maxX) / 2, minY, (minZ + maxZ) / 2, p1);
		behind = useGrayShading && highFrontY;
		g.setColor(behind ? kStarLineDimColor : kStarLineColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		drawBlobAndCount(g, p0, behind ? kStarDimColor : kStarColor, starReplicates);
		
		p0 = getScreenPoint((minX + maxX) / 2, highYStar, (minZ + maxZ) / 2, p0);
		p1 = getScreenPoint((minX + maxX) / 2, maxY, (minZ + maxZ) / 2, p1);
		behind = useGrayShading && !highFrontY;
		g.setColor(behind ? kStarLineDimColor : kStarLineColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		drawBlobAndCount(g, p0, behind ? kStarDimColor : kStarColor, starReplicates);
		
		double lowZStar = minZ - 0.34089641525 * (maxZ - minZ);
		double highZStar = maxZ + 0.34089641525 * (maxZ - minZ);
		
		p0 = getScreenPoint((minX + maxX) / 2, (minY + maxY) / 2, lowZStar, p0);
		p1 = getScreenPoint((minX + maxX) / 2, (minY + maxY) / 2, minZ, p1);
		behind = useGrayShading && highFrontZ;
		g.setColor(behind ? kStarLineDimColor : kStarLineColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		drawBlobAndCount(g, p0, behind ? kStarDimColor : kStarColor, starReplicates);
		
		p0 = getScreenPoint((minX + maxX) / 2, (minY + maxY) / 2, highZStar, p0);
		p1 = getScreenPoint((minX + maxX) / 2, (minY + maxY) / 2, maxZ, p1);
		behind = useGrayShading && !highFrontZ;
		g.setColor(behind ? kStarLineDimColor : kStarLineColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		drawBlobAndCount(g, p0, behind ? kStarDimColor : kStarColor, starReplicates);
	}
	
	private void drawBbPoints(Graphics g, boolean useGrayShading, boolean highFrontX,
						boolean highFrontZ, boolean highFrontY) {
		if (bbReplicates == 0)
			return;
		Point p0 = null;
		
		for (double i=0 ; i<2 ; i++) {
			double x = (i == 0) ? minX : maxX;
			for (double j=0 ; j<2 ; j++) {
				double y = (j == 0) ? minY : maxY;
				boolean backPlanes = (i == 0) == highFrontX && (j == 0) == highFrontY;
				p0 = getScreenPoint(x, y, (minZ + maxZ) / 2, p0);
				boolean behind = useGrayShading && backPlanes;
				drawBlobAndCount(g, p0, behind ? kBbDimColor : kBbColor, bbReplicates);
			}
		}
		
		for (double i=0 ; i<2 ; i++) {
			double x = (i == 0) ? minX : maxX;
			for (double j=0 ; j<2 ; j++) {
				double z = (j == 0) ? minZ : maxZ;
				boolean backPlanes = (i == 0) == highFrontX && (j == 0) == highFrontZ;
				p0 = getScreenPoint(x, (minY + maxY) / 2, z, p0);
				boolean behind = useGrayShading && backPlanes;
				drawBlobAndCount(g, p0, behind ? kBbDimColor : kBbColor, bbReplicates);
			}
		}
		
		for (double i=0 ; i<2 ; i++) {
			double y = (i == 0) ? minY : maxY;
			for (double j=0 ; j<2 ; j++) {
				double z = (j == 0) ? minZ : maxZ;
				boolean backPlanes = (i == 0) == highFrontY && (j == 0) == highFrontZ;
				p0 = getScreenPoint((minX + maxX) / 2, y, z, p0);
				boolean behind = useGrayShading && backPlanes;
				drawBlobAndCount(g, p0, behind ? kBbDimColor : kBbColor, bbReplicates);
			}
		}
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		drawCube(g, shadeHandling);
		
		boolean highFrontX = map.zAxisBehind();
		boolean highFrontZ = map.xAxisBehind();
		boolean highFrontY = map.getTheta2() <= 90;
		
		Font oldFont = g.getFont();
		g.setFont(getApplet().getBigBoldFont());
		
		drawFactorialPoints(g, shadeHandling == USE_OPAQUE, highFrontX, highFrontZ, highFrontY);
		
		drawCenterPoints(g, shadeHandling == USE_OPAQUE, highFrontX, highFrontZ, highFrontY);
		
		drawStarPoints(g, shadeHandling == USE_OPAQUE, highFrontX, highFrontZ, highFrontY);
		
		drawBbPoints(g, shadeHandling == USE_OPAQUE, highFrontX, highFrontZ, highFrontY);
		
		g.setFont(oldFont);
	}
}
	
