package bivarDistn;

import java.awt.*;

import dataView.*;
import models.*;

import graphics3D.*;


public class ConditSurfaceView extends SurfaceView {
	static final public int NO_SLICE = 0;
	static final public int X_SLICE = 1;
	static final public int Z_SLICE = 2;
	
	static final private Color kGrayColor = new Color(0xBBBBBB);
	
	private int sliceType = NO_SLICE;
	private double sliceValue;
	
	private String textLabel = null;
	
	public ConditSurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
						D3Axis zAxis, String modelKey, String[] explanKey, String yKey, int axisSteps) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey, axisSteps);
	}
	
	public ConditSurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
																					D3Axis zAxis, String modelKey, int axisSteps) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, axisSteps);
	}
	
	public ConditSurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
																											D3Axis zAxis, String modelKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey);
	}
	
	public ConditSurfaceView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
						D3Axis zAxis, String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
	}
	
//-----------------------------------------------------------
	
	public void setSliceType(int sliceType) {
		this.sliceType = sliceType;
	}
	
	public void setSliceValue(double sliceValue) {
		this.sliceValue = sliceValue;
		repaint();
	}
	
	public void setTextLabel(String textLabel) {
		this.textLabel = textLabel;
		repaint();
	}

//-------------------------------------------------------------------
	
	private void drawXSlice(double x, Graphics g) {
		SurfaceInterface model = (SurfaceInterface)getVariable(modelKey);
		
		double minZ = zAxis.getMinOnAxis();
		double maxZ = zAxis.getMaxOnAxis();
		
		Point p0 = null, p1 = null;
		double xz[] = new double[2];
		xz[0] = x;
		
		for (int j=0 ; j<=axisSteps ; j++) {
			p0 = p1;
			double z = minZ + j * (maxZ - minZ) / axisSteps;
			xz[1] = z;
			double density = model.getHeight(xz);
			p1 = getScreenPoint(x, density, z, null);
			if (p0 != null)
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
	}
	
	private void drawZSlice(double z, Graphics g) {
		SurfaceInterface model = (SurfaceInterface)getVariable(modelKey);
		
		double minX = xAxis.getMinOnAxis();
		double maxX = xAxis.getMaxOnAxis();
		
		Point p0 = null, p1 = null;
		double xz[] = new double[2];
		xz[1] = z;
		
		for (int j=0 ; j<=axisSteps ; j++) {
			p0 = p1;
			double x = minX + j * (maxX - minX) / axisSteps;
			xz[0] = x;
			double density = model.getHeight(xz);
			p1 = getScreenPoint(x, density, z, null);
			if (p0 != null)
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
	}
	
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		g.setColor(Color.black);
		switch (sliceType) {
			case X_SLICE:
				drawXSlice(sliceValue, g);
				break;
			case Z_SLICE:
				drawZSlice(sliceValue, g);
				break;
			case NO_SLICE:
			default:
				break;
		}
		
		if (textLabel != null) {
			g.setFont(getApplet().getBigBoldFont());
			g.setColor(kGrayColor);
			FontMetrics fm = g.getFontMetrics();
			int labelWidth = fm.stringWidth(textLabel);
			g.drawString(textLabel, getSize().width - labelWidth - 6, fm.getAscent() + 6);
		}
	}

}
	
