package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class SamplePlanesView extends Rotate3DView {
	
	static final public int NO_SPECIAL_LINE = 0;
	static final public int LINE_AT_X = 1;
	static final public int LINE_AT_Z = 2;
	
	static final private Color kHighlightColor = Color.red;
	static final private Color kModelColor = Color.blue;
	static final private Color kDataColor = Color.black;
	static final private Color kPlaneColour = Color.gray;
	static final private Color kResidColor = Color.red;
	static final private Color kSpecialLineColor = Color.black;
	
	private String planeKey, modelKey, sampleYKey, lsEvaluatorKey;
	private DataSet sourceData;
	private String[] explanKey;
	
	private boolean showModel = false;
	private boolean showData = true;
	private boolean showResiduals = true;
	
	private int specialLineType = NO_SPECIAL_LINE;
	private double specialValue = 0.0;
	
	public SamplePlanesView(SummaryDataSet summaryData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
						D3Axis zAxis, String planeKey, DataSet sourceData, String modelKey, String[] explanKey,
						String sampleYKey, String lsEvaluatorKey) {
		super(summaryData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.planeKey = planeKey;
		this.sourceData = sourceData;
		this.modelKey = modelKey;
		this.sampleYKey = sampleYKey;
		this.explanKey = explanKey;
		this.lsEvaluatorKey = lsEvaluatorKey;
	}
	
	public void setModelShow (boolean showModel) {
		this.showModel = showModel;
		repaint();
	}
	
	public void setDataShow (boolean showData) {
		this.showData = showData;
		repaint();
	}
	
	public void setResidualShow (boolean showResiduals) {
		this.showResiduals = showResiduals;
		repaint();
	}
	
	public void setLineDisplay (int specialLineType, double specialValue) {
		this.specialLineType = specialLineType;
		this.specialValue = specialValue;
		repaint();
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = (MultipleRegnModel)sourceData.getVariable(modelKey);
		double twoSD = 2.0 * model.evaluateSD().toDouble();
		
		MultipleRegnModel lsEvaluator = (MultipleRegnModel)sourceData.getVariable(lsEvaluatorKey);
		
		ValueEnumeration ye = ((NumVariable)sourceData.getVariable(sampleYKey)).values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)sourceData.getVariable(explanKey[i])).values();
			
		Point lowPos = null;
		Point highPos = null;
		Point fitPos = null;
		Point yPos = null;
		double xVals[] = new double[explanKey.length];
			
		while (ye.hasMoreValues()) {
			double yVal = ye.nextDouble();
			for (int i=0 ; i<explanKey.length ; i++)
				xVals[i] = xe[i].nextDouble();
			
			if (showModel) {
				double mean = model.evaluateMean(xVals);
				lowPos = getScreenPoint(xVals[0], mean - twoSD, xVals[1], lowPos);
				highPos = getScreenPoint(xVals[0], mean + twoSD, xVals[1], highPos);
				g.setColor(kModelColor);
				g.drawLine(lowPos.x, lowPos.y, highPos.x, highPos.y);
			}
			
			yPos = getScreenPoint(xVals[0], yVal, xVals[1], yPos);
			if (showResiduals) {
				double fit = lsEvaluator.evaluateMean(xVals);
				fitPos = getScreenPoint(xVals[0], fit, xVals[1], fitPos);
				g.setColor(kResidColor);
				g.drawLine(fitPos.x, fitPos.y, yPos.x, yPos.y);
			}
			
			if (showData) {
				g.setColor(kDataColor);
				drawCross(g, yPos);
			}
		}
		
		ValueEnumeration pe = ((LSCoeffVariable)getVariable(planeKey)).values();
		FlagEnumeration fe = getSelection().getEnumeration();
		while (pe.hasMoreValues()) {
			LSCoeffValue planeVal = (LSCoeffValue)pe.nextValue();
			NumValue coeff[] = planeVal.coeff;
			
			boolean selected = fe.nextFlag();
			g.setColor(selected ? kHighlightColor : kPlaneColour);
			
			Polygon p = ModelGraphics3D.getFull3DPlane(map, this, yAxis, xAxis, zAxis, coeff[0].toDouble(),
																			coeff[1].toDouble(), coeff[2].toDouble());
			g.drawPolygon(p);
			
			if (specialLineType != NO_SPECIAL_LINE) {
				if (!selected)
					g.setColor(kSpecialLineColor);
				drawConditLine(g, coeff[0].toDouble(), coeff[1].toDouble(), coeff[2].toDouble());
			}
		}
	}
	
	private void drawConditLine (Graphics g, double b0, double b1, double b2) {
		if (specialLineType == LINE_AT_X) {
			double lowZ = zAxis.getMinOnAxis();
			double highZ = zAxis.getMaxOnAxis();
			double range = highZ - lowZ;
			lowZ -= range;
			highZ += range;
			double lowFit = b0 + b1 * specialValue + b2 * lowZ;
			double highFit = b0 + b1 * specialValue + b2 * highZ;
			
			double x = xAxis.numValToPosition(specialValue);
			double z0 = zAxis.numValToPosition(lowZ);
			double y0 = yAxis.numValToPosition(lowFit);
			
			double z1 = zAxis.numValToPosition(highZ);
			double y1 = yAxis.numValToPosition(highFit);
			
			Point p0 = translateToScreen(map.mapH3DGraph(y0, x, z0),
															map.mapV3DGraph(y0, x, z0), null);
			Point p1 = translateToScreen(map.mapH3DGraph(y1, x, z1),
															map.mapV3DGraph(y1, x, z1), null);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		else {
			double lowX = xAxis.getMinOnAxis();
			double highX = xAxis.getMaxOnAxis();
			double range = highX - lowX;
			lowX -= range;
			highX += range;
			double lowFit = b0 + b1 * lowX + b2 * specialValue;
			double highFit = b0 + b1 * highX + b2 * specialValue;
			
			double z = zAxis.numValToPosition(specialValue);
			double x0 = xAxis.numValToPosition(lowX);
			double y0 = yAxis.numValToPosition(lowFit);
			
			double x1 = xAxis.numValToPosition(highX);
			double y1 = yAxis.numValToPosition(highFit);
			
			Point p0 = translateToScreen(map.mapH3DGraph(y0, x0, z),
															map.mapV3DGraph(y0, x0, z), null);
			Point p1 = translateToScreen(map.mapH3DGraph(y1, x1, z),
															map.mapV3DGraph(y1, x1, z), null);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
	}
	
}

