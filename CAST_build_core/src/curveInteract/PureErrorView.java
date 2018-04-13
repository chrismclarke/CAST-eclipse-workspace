package curveInteract;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class PureErrorView extends Rotate3DView {
//	static public final String PURE_ERROR_VIEW = "pureErrorView";
	
	static final private Color kRegnColor = new Color(0xFF0000);
	static final private Color kFactorColor = new Color(0x0000FF);
	
//	static final private int kHalfFitLine = 10;
	
	private String linModelKey, factorLSKey;
	
	private double[] xValues;
	private double[] zValues;
	
	private double tempVal[] = new double[5];
	
	public PureErrorView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String linModelKey, String xKey, String yKey, String zKey,
						String factorLSKey, double[] xValues, double[] zValues) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
		this.linModelKey = linModelKey;
		this.factorLSKey = factorLSKey;
		
		this.xValues = xValues;
		this.zValues = zValues;
	}
	
//--------------------------------------------------------------
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel regnModel = (MultipleRegnModel)getVariable(linModelKey);
		GroupsModelVariable factorModel = (GroupsModelVariable)getVariable(factorLSKey);
		
		drawModelGrid(g, regnModel, factorModel, shadeHandling);
		super.drawData(g, shadeHandling);
	}
	
	private Point getLinearFitPoint(double x, double z, MultipleRegnModel regnModel,
																						Point p) {
		tempVal[0] = x;
		tempVal[1] = z;
		tempVal[2] = x * x;
		tempVal[3] = z * z;
		tempVal[4] = x * z;
		double prediction =  regnModel.evaluateMean(tempVal);
		return getScreenPoint(x, prediction, z, p);
	}
	
	private Point getFactorFitPoint(double x, double z, int index, GroupsModelVariable model,
																																										Point p) {
		double prediction =  model.getMean(index).toDouble();
		return getScreenPoint(x, prediction, z, p);
	}
	
	private void drawModelGrid(Graphics g, MultipleRegnModel regnModel,
																GroupsModelVariable factorModel, int shadeHandling) {
		Point p0 = null;
		Point p1 = null;
		
		g.setColor(kRegnColor);
		for (int i=0 ; i<xValues.length ; i++) {
			double x = xValues[i];
			double z = zValues[0];
			p0 = getLinearFitPoint(x, z, regnModel, p0);
			for (int j=1 ; j<zValues.length ; j++) {
				z = zValues[j];
				
				p1 = getLinearFitPoint(x, z, regnModel, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				Point pTemp = p0 ; p0 = p1 ; p1 = pTemp;
			}
		}
		
		for (int j=0 ; j<zValues.length ; j++) {
			double z = zValues[j];
			double x = xValues[0];
			p0 = getLinearFitPoint(x, z, regnModel, p0);
			for (int i=1 ; i<xValues.length ; i++) {
				x = xValues[i];
				
				p1 = getLinearFitPoint(x, z, regnModel, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				Point pTemp = p0 ; p0 = p1 ; p1 = pTemp;
			}
		}
		
		g.setColor(kFactorColor);
		int index = 0;
		for (int i=0 ; i<xValues.length ; i++) {
			double x = xValues[i];
			double z = zValues[0];
			p0 = getFactorFitPoint(x, z, index, factorModel, p0);
			index ++;
			for (int j=1 ; j<zValues.length ; j++) {
				z = zValues[j];
				
				p1 = getFactorFitPoint(x, z, index, factorModel, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				index ++;
				Point pTemp = p0 ; p0 = p1 ; p1 = pTemp;
			}
		}
		
		for (int j=0 ; j<zValues.length ; j++) {
			double z = zValues[j];
			double x = xValues[0];
			index = j;
			p0 = getFactorFitPoint(x, z, index, factorModel, p0);
			for (int i=1 ; i<xValues.length ; i++) {
				x = xValues[i];
				
				index += zValues.length;
				p1 = getFactorFitPoint(x, z, index, factorModel, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				
				Point pTemp = p0 ; p0 = p1 ; p1 = pTemp;
			}
		}
	}
}
	
