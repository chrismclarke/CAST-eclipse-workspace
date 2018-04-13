package glmAnova;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class PolyScatterView extends ScatterView implements SetLastExplanInterface {
//	static public final String POLY_SCATTER_PLOT = "polyScatterPlot";
	
	static final private Color kMainPolyColor = new Color(0x0000FF);
	static final private Color kOtherPolyColor = new Color(0x99CCFF);
	
	protected String mainPolyKey, otherPolyKey;
	
	private boolean showLowerPower;
	
	public PolyScatterView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
											String xKey, String yKey, String mainPolyKey, String otherPolyKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.mainPolyKey = mainPolyKey;
		this.otherPolyKey = otherPolyKey;
	}
	
	public void setShowLowerPower(boolean showLowerPower) {
		this.showLowerPower = showLowerPower;
		otherPolyKey = null;
	}
	
	public void setLastExplanatory(int lastSeparateX) {		//	assumes keys are "ls0", "ls1", etc
		mainPolyKey = "ls" + (lastSeparateX + 1);
		otherPolyKey = showLowerPower ? ("ls" + lastSeparateX) : null;
		repaint();
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
	
	protected Point getScreenPoint(double x, double y, Point thePoint) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = axis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	private void drawBackground(Graphics g) {
		LinearModel mainModel = (LinearModel)getVariable(mainPolyKey);
		LinearModel otherModel = (otherPolyKey == null) ? null : (LinearModel)getVariable(otherPolyKey);
		
		if (mainModel != null && otherModel != null) {
			Point p0 = null, p1 = null;
			g.setColor(Color.red);
			NumVariable xVar = (NumVariable)getVariable(xKey);
			ValueEnumeration xe = xVar.values();
			while (xe.hasMoreValues()) {
				double x = xe.nextDouble();
				double mainFit = mainModel.evaluateMean(x);
				double otherFit = otherModel.evaluateMean(x);
				p0 = getScreenPoint(x, mainFit, p0);
				p1 = getScreenPoint(x, otherFit, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
		
		if (otherModel != null) {
			g.setColor(kOtherPolyColor);
			otherModel.drawMean(g, this, axis, yAxis);
		}
		
		if (mainModel != null) {
			g.setColor(kMainPolyColor);
			mainModel.drawMean(g, this, axis, yAxis);
		}
		
		g.setColor(getForeground());
	}
}
	
