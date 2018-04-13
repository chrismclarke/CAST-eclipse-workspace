package mixture;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class TriangleDesignView extends RotateDotPlaneView {
	
	static final private Color kTriangleFillColor = new Color(0xEEEEEE);
	static final private Color kTriangleLineColor = new Color(0x999999);
	
	public TriangleDesignView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String yKey, String[] xzKey) {
		super(theData, applet, xAxis, yAxis, zAxis, null, xzKey, yKey);
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
	
	protected int minHitDistance() {
		return 36;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
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
			crossPos = getScreenPoint(x, y, z, crossPos);
			g.setColor(selected ? Color.red : getForeground());
			if (crossPos != null)
				drawBlob(g, crossPos);
		}
	}
}
	
