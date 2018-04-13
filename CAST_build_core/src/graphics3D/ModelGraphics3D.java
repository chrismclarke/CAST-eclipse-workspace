package graphics3D;

import java.awt.*;

import coreGraphics.*;



public class ModelGraphics3D extends ModelGraphics {
	
	static private void setEndPoints(D3Axis yAxis, D3Axis xAxis, double b0, double b1,
																			boolean reverse, PointArray p) {
		p.reset();
		double lowX = xAxis.getMinOnAxis();
		double highX = xAxis.getMaxOnAxis();
		double lowY = yAxis.getMinOnAxis();
		double highY = yAxis.getMaxOnAxis();
		
		double yLowX = b0 + b1 * lowX;
		double yHighX = b0 + b1 * highX;
		
		if ((yLowX < lowY) && (yHighX < lowY) || (yLowX > highY) && (yHighX > highY))
			return;							//		both endpoints are off y-axis on same side
		
		double x0, x1, y0, y1;
		
		if (b1 == 0.0 || (yLowX >= lowY) && (yLowX <= highY)) {
			x0 = lowX;
			y0 = yLowX;
		}
		else if (yLowX < lowY) {
			x0 = (lowY - b0) / b1;
			y0 = lowY;
		}
		else {
			x0 = (highY - b0) / b1;
			y0 = highY;
		}
		
		if (b1 == 0.0 || (yHighX >= lowY) && (yHighX <= highY)) {
			x1 = highX;
			y1 = yHighX;
		}
		else if (yHighX < lowY) {
			x1 = (lowY - b0) / b1;
			y1 = lowY;
		}
		else {
			x1 = (highY - b0) / b1;
			y1 = highY;
		}
		
		x0 = xAxis.numValToPosition(x0);
		y0 = yAxis.numValToPosition(y0);
		x1 = xAxis.numValToPosition(x1);
		y1 = yAxis.numValToPosition(y1);
		
		if (reverse) {
			p.addPoint(x1, y1);
			p.addPoint(x0, y0);
		}
		else {
			p.addPoint(x0, y0);
			p.addPoint(x1, y1);
		}
	}
	
	static public Polygon getCropped3DPlane(RotateMap map, Rotate3DView view, D3Axis yAxis,
										D3Axis xAxis, D3Axis zAxis, double b0, double bx, double bz) {
		PointArray p = new PointArray(2);
		Polygon poly = new Polygon();
		Point p0 = null;
		
		double zMin = zAxis.getMinOnAxis();
		setEndPoints(yAxis, xAxis, b0 + zMin * bz, bx, false, p);
		for (int i=0 ; i<p.nPoints ; i++) {
			p0 = view.translateToScreen(map.mapH3DGraph(p.y[i], p.x[i], 0.0),
															map.mapV3DGraph(p.y[i], p.x[i], 0.0), p0);
			addPolyPoint(p0, poly);
		}
		
		double xMax = xAxis.getMaxOnAxis();
		setEndPoints(yAxis, zAxis, b0 + xMax * bx, bz, false, p);
		for (int i=0 ; i<p.nPoints ; i++) {
			p0 = view.translateToScreen(map.mapH3DGraph(p.y[i], 1.0, p.x[i]),
															map.mapV3DGraph(p.y[i], 1.0, p.x[i]), p0);
			addPolyPoint(p0, poly);
		}
		
		
		double zMax = zAxis.getMaxOnAxis();
		setEndPoints(yAxis, xAxis, b0 + zMax * bz, bx, true, p);
		for (int i=0 ; i<p.nPoints ; i++) {
			p0 = view.translateToScreen(map.mapH3DGraph(p.y[i], p.x[i], 1.0),
															map.mapV3DGraph(p.y[i], p.x[i], 1.0), p0);
			addPolyPoint(p0, poly);
		}
		
		
		double xMin = xAxis.getMinOnAxis();
		setEndPoints(yAxis, zAxis, b0 + xMin * bx, bz, true, p);
		for (int i=0 ; i<p.nPoints ; i++) {
			p0 = view.translateToScreen(map.mapH3DGraph(p.y[i], 0.0, p.x[i]),
															map.mapV3DGraph(p.y[i], 0.0, p.x[i]), p0);
			addPolyPoint(p0, poly);
		}
		
		closePoly(poly);
		
		return poly;
	}
	
	static public Polygon getFull3DPlane(RotateMap map, Rotate3DView view, D3Axis yAxis,
										D3Axis xAxis, D3Axis zAxis, double b0, double bx, double bz) {
		double xMin = xAxis.getMinOnAxis();
		double xMax = xAxis.getMaxOnAxis();
		double zMin = zAxis.getMinOnAxis();
		double zMax = zAxis.getMaxOnAxis();
		Polygon poly = new Polygon();
		
		double fit = b0 + bx * xMin + bz * zMin;
		double yFract = yAxis.numValToPosition(fit);
		Point pOrigin = view.translateToScreen(map.mapH3DGraph(yFract, 0.0, 0.0),
															map.mapV3DGraph(yFract, 0.0, 0.0), null);
		poly.addPoint(pOrigin.x, pOrigin.y);
		
		fit = b0 + bx * xMax + bz * zMin;
		yFract = yAxis.numValToPosition(fit);
		Point p = view.translateToScreen(map.mapH3DGraph(yFract, 1.0, 0.0),
															map.mapV3DGraph(yFract, 1.0, 0.0), null);
		poly.addPoint(p.x, p.y);
		
		fit = b0 + bx * xMax + bz * zMax;
		yFract = yAxis.numValToPosition(fit);
		p = view.translateToScreen(map.mapH3DGraph(yFract, 1.0, 1.0),
															map.mapV3DGraph(yFract, 1.0, 1.0), p);
		poly.addPoint(p.x, p.y);
		
		fit = b0 + bx * xMin + bz * zMax;
		yFract = yAxis.numValToPosition(fit);
		p = view.translateToScreen(map.mapH3DGraph(yFract, 0.0, 1.0),
															map.mapV3DGraph(yFract, 0.0, 1.0), p);
		poly.addPoint(p.x, p.y);
		
		poly.addPoint(pOrigin.x, pOrigin.y);		//		closes polygon
		
		return poly;
	}
	
	static private void addPolyPoint(Point p, Polygon poly) {
		if ((poly.npoints == 0) || (poly.xpoints[poly.npoints - 1] != p.x)
													|| (poly.ypoints[poly.npoints - 1] != p.y))
			poly.addPoint(p.x, p.y);
	}
	
	static private void closePoly(Polygon poly) {
		if ((poly.npoints != 0) && ((poly.xpoints[poly.npoints - 1] != poly.xpoints[0])
												|| (poly.ypoints[poly.npoints - 1] != poly.ypoints[0])))
			poly.addPoint(poly.xpoints[0], poly.ypoints[0]);
	}
}
