package graphics3D;

import java.awt.*;

import dataView.*;
import models.*;
import coreGraphics.*;


public class SurfaceView extends RotateDotPlaneView {
	
	static final public int SURFACE = 0;
	static final public int GRID = 1;
	static final public int CONTOURS = 2;
	
	static final private int kDefaultAxisSteps = 20;
	
	protected int drawType = SURFACE;
	
	private double[] fixedContours = null;
	
	protected int axisSteps = kDefaultAxisSteps;
	
	private int[][] horizGridCoord;
	private int[][] vertGridCoord;
	
	private SurfaceGridDrawer surfaceDrawer;
	private ContourControlView contourControl;
	
	public SurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
						D3Axis zAxis, String modelKey, String[] explanKey, String yKey, int axisSteps) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
		this.axisSteps = axisSteps;
		surfaceDrawer = new SurfaceGridDrawer(axisSteps, xAxis.getMinOnAxis(), xAxis.getMaxOnAxis(),
																												zAxis.getMinOnAxis(), zAxis.getMaxOnAxis());
	
		horizGridCoord = new int[axisSteps + 1][axisSteps + 1];
		vertGridCoord = new int[axisSteps + 1][axisSteps + 1];
	}
	
	public SurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
																					D3Axis zAxis, String modelKey, int axisSteps) {
		this(theData, applet, xAxis, yAxis, zAxis, modelKey, null, null, axisSteps);
	}
	
	public SurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
																											D3Axis zAxis, String modelKey) {
		this(theData, applet, xAxis, yAxis, zAxis, modelKey, null, null, kDefaultAxisSteps);
	}
	
	public SurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
						D3Axis zAxis, String modelKey, String[] explanKey, String yKey) {
		this(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey, kDefaultAxisSteps);
	}
	
//-----------------------------------------------------------
	
	public void setDrawType(int drawType) {
		this.drawType = drawType;
	}
	
	public void setColourMap(ColourMap colourMap) {
		surfaceDrawer.setColourMap(colourMap);
	}
	
	public void setContourControl(ContourControlView contourControl) {
		this.contourControl = contourControl;
	}
	
	public void setFixedContours(double[] fixedContours) {
		this.fixedContours = fixedContours;
	}

//-------------------------------------------------------------------
	
	private double getContourValue() {
		if (contourControl == null)
			return Double.NaN;
		else
			return contourControl.getContourValue();
	}
	
	public SurfaceGridDrawer getSurfaceDrawer() {
		return surfaceDrawer;
	}
	
	public boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			SurfaceInterface model = (SurfaceInterface)getVariable(modelKey);
			if (model == null)
				initialised = false;
			else
				surfaceDrawer.findHeights(model);
		
			return true;
		}
		return false;
	}
	
	private GridPolygon findMinMaxPoly(boolean maxNotMin) {
		int startX = horizGridCoord[0][0] < horizGridCoord[axisSteps][0] ? 0 : axisSteps;
		int endX = axisSteps - startX;
		int xStep = (startX == 0) ? 1 : -1;
		int startZ = horizGridCoord[0][0] < horizGridCoord[0][axisSteps] ? 0 : axisSteps;
		int endZ = axisSteps - startZ;
		int zStep = (startZ == 0) ? 1 : -1;
		
		GridPolygon p = new GridPolygon();
		for (int i=startX ; i!=endX+xStep ; i+=xStep)
			p.addPoint(horizGridCoord[i][startZ], vertGridCoord[i][startZ]);
		for (int j=startZ+zStep ; j!=endZ+zStep ; j+=zStep)
			p.addPoint(horizGridCoord[endX][j], vertGridCoord[endX][j]);
//		System.out.println("back coords: " + p);
		
		GridPolygon pTemp = new GridPolygon();
		for (int j=startZ ; j!=endZ+zStep ; j+=zStep)
			pTemp.addPoint(horizGridCoord[startX][j], vertGridCoord[startX][j]);
		for (int i=startX+xStep ; i!=endX+xStep ; i+=xStep)
			pTemp.addPoint(horizGridCoord[i][endZ], vertGridCoord[i][endZ]);
//		System.out.println("front coords: " + pTemp);
		
		p.combineMinMax(pTemp, maxNotMin);
//		System.out.println("frontBack coords: " + p);
		
		for (int i=startX+xStep ; i!=endX ; i+=xStep) {
			pTemp.clear();
			
			for (int ii=startX ; ii<i ; ii+= xStep)
				pTemp.addPoint(horizGridCoord[ii][startZ], vertGridCoord[ii][startZ]);
			
			for (int j=startZ ; j!=endZ+zStep ; j+=zStep)
				pTemp.addPoint(horizGridCoord[i][j], vertGridCoord[i][j]);
			
			for (int ii=i+xStep ; ii!=endX+xStep ; ii+=xStep)
				pTemp.addPoint(horizGridCoord[ii][endZ], vertGridCoord[ii][endZ]);
				
//			System.out.println("line " + i + ": " + pTemp);
			p.combineMinMax(pTemp, maxNotMin);
//			System.out.println("mod coords: " + p);
		}
		
		for (int j=startZ+zStep ; j!=endZ ; j+=zStep) {
			pTemp.clear();
			
			for (int jj=startZ ; jj<j ; jj+= zStep)
				pTemp.addPoint(horizGridCoord[startX][jj], vertGridCoord[startX][jj]);
			
			for (int i=startX ; i!=endX+xStep ; i+=xStep)
				pTemp.addPoint(horizGridCoord[i][j], vertGridCoord[i][j]);
			
			for (int jj=j+zStep ; jj!=endZ+zStep ; jj+=zStep)
				pTemp.addPoint(horizGridCoord[endX][jj], vertGridCoord[endX][jj]);
				
			p.combineMinMax(pTemp, maxNotMin);
		}
		return p;
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		SurfaceInterface model = (SurfaceInterface)getVariable(modelKey);
		if (model == null)
			return null;
		
		initialise(g);
		
		boolean xAxisBehind = map.xAxisBehind();
		boolean zAxisBehind = map.zAxisBehind();
		double axisAngle = map.getTheta1();
		boolean startDrawingX = (axisAngle > 135 && axisAngle < 225) || axisAngle < 45 || axisAngle > 315;
		
		double minX = xAxis.getMinOnAxis();
		double maxX = xAxis.getMaxOnAxis();
		double minZ = zAxis.getMinOnAxis();
		double maxZ = zAxis.getMaxOnAxis();
		
		Point p = null;
		
		for (int i=0 ; i<=axisSteps ; i++) {
			double x = minX + i * (maxX - minX) / axisSteps;
			for (int j=0 ; j<=axisSteps ; j++) {
				double z = minZ + j * (maxZ - minZ) / axisSteps;
				p = getScreenPoint(x, surfaceDrawer.fitValue[i][j], z, p);
				horizGridCoord[i][j] = p.x;
				vertGridCoord[i][j] = p.y;
			}
		}
		surfaceDrawer.setGridCoords(horizGridCoord, vertGridCoord);
		
		if (drawType == GRID) {
			surfaceDrawer.frameSurface(g);
			surfaceDrawer.drawContour(g, model, getContourValue());
		}
		else if (drawType == SURFACE) {
			int startXIndex = zAxisBehind ? 0 : axisSteps;
			int stepXIndex = zAxisBehind ? 1 : -1;
			int minXLoopIndex = zAxisBehind ? 0 : 1;
			int maxXLoopIndex = zAxisBehind ? (axisSteps - 1) : axisSteps;
			
			int startZIndex = xAxisBehind ? 0 : axisSteps;
			int stepZIndex = xAxisBehind ? 1 : -1;
			int minZLoopIndex = xAxisBehind ? 0 : 1;
			int maxZLoopIndex = xAxisBehind ? (axisSteps - 1) : axisSteps;
			
			surfaceDrawer.shadeSurface(g, model, getContourValue(),
												startXIndex, stepXIndex, minXLoopIndex, maxXLoopIndex,
												startZIndex, stepZIndex, minZLoopIndex, maxZLoopIndex,
												startDrawingX);
			
			GridPolygon maxPoly = findMinMaxPoly(true);
			GridPolygon minPoly = findMinMaxPoly(false);
			
			maxPoly.complete(minPoly);
			
			return maxPoly.getPolygon();
		}
		else {		//	drawType == CONTOURS
			g.setColor(Color.lightGray);
			surfaceDrawer.outlineSurface(g);
			
			if (fixedContours != null) {
				for (int i=0 ; i<fixedContours.length ; i++) {
					g.setColor(surfaceDrawer.getColourMap().getColour(fixedContours[i]));
					surfaceDrawer.drawContour(g, model, fixedContours[i]);
				}
			}
			double dragContour = getContourValue();
			if (!Double.isNaN(dragContour)) {
					g.setColor(Color.black);
					surfaceDrawer.drawContour(g, model, dragContour);
			}
		}
		return null;
	}
	

	protected void drawData(Graphics g, int shadeHandling) {
	}
	
	
//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(modelKey));
			initialised = false;
		super.doChangeVariable(g, key);
	}

}
	
