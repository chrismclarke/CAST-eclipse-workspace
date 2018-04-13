package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class RotateModelBandView extends Rotate3DCrossPlanesView {
	
	static final private Color kPlaneColor = new Color(0xDDDDDD);
	static final private Color kAxisToPlanesColor = new Color(0xFFDDEE);
	static final private Color kBandLineColor = Color.red;
	static final private Color kComponentColor[] = {null, kBandLineColor, null};
	
	private Color axisToPlanesColor[];
	
	public RotateModelBandView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
																											String modelKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, null, kPlaneColor, kPlaneColor, kComponentColor);
		setComponentType(A_TO_B);
		axisToPlanesColor = getAllShades(kAxisToPlanesColor);
	}
	
	protected double getFitA(MultipleRegnModel modelA, MultipleRegnModel modelB, double[] explan) {
		return modelA.evaluateMean(explan) - 2.0 * modelA.evaluateSD().toDouble();
	}
	
	protected double getFitB(MultipleRegnModel modelA, MultipleRegnModel modelB, double[] explan) {
		return modelA.evaluateMean(explan) + 2.0 * modelA.evaluateSD().toDouble();
	}
	
	protected double getNextY(ValueEnumeration ye) {
		return yAxis.getMinOnAxis();
	}
	
	protected void drawComponentLine(Graphics g, int planeLevel, double y, double x, double z,
										double fitA, double fitB, Point crossPos, Point fitAPos, Point fitBPos,
										boolean aPlaneFromTop, boolean bPlaneFromTop,
										boolean crossBehindA, boolean crossBehindB, boolean aFurthestBack) {
		g.setColor(getShade(axisToPlanesColor, planeLevel, crossBehindA, crossBehindB,
																																			aFurthestBack));
		g.drawLine(crossPos.x, crossPos.y, fitAPos.x, fitAPos.y);
		
		super.drawComponentLine(g, planeLevel, y, x, z, fitA, fitB, crossPos, fitAPos, fitBPos,
										aPlaneFromTop, bPlaneFromTop, crossBehindA, crossBehindB, aFurthestBack);
	}
}
	
