package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class SeqComponentPlanesView extends Rotate3DPlanesView {
	
	static final public int DATA_TO_M0 = 0;
	static final public int M0_TO_M1 = 1;
	static final public int M1_TO_M2 = 2;
	static final public int DATA_TO_M2 = 3;
	
	private int componentType = DATA_TO_M0;
	
	private String model2Key;
	private Color model0Color, model1Color, model2Color;
	
	private Color crossColor[];
	private Color dm0Color[];
	private Color m0m1Color[];
	private Color m1m2Color[];
	private Color dm2Color[];
	
	public SeqComponentPlanesView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String model0Key, String model1Key, String model2Key, Color model0Color,
						Color model1Color, Color model2Color, Color[] componentColor) {
		super(theData, applet, xAxis, yAxis, zAxis, model0Key, model1Key, model0Color, model0Color);
		this.model2Key = model2Key;
		crossColor = getAllShades(Color.black, 0.55);
		if (componentColor[0] != null)
			dm0Color = getAllShades(componentColor[0]);
		if (componentColor[1] != null)
			m0m1Color = getAllShades(componentColor[1]);
		if (componentColor[2] != null)
			m1m2Color = getAllShades(componentColor[2]);
		if (componentColor[3] != null)
			dm2Color = getAllShades(componentColor[3]);
		this.model0Color = model0Color;
		this.model1Color = model1Color;
		this.model2Color = model2Color;
	}
	
	public void setModelColors(Color model0Color, Color model1Color, Color model2Color) {
		this.model0Color = model0Color;
		this.model1Color = model1Color;
		this.model2Color = model2Color;
	}
	
	public void setComponentType(int componentType) {
		this.componentType = componentType;
		if (componentType == DATA_TO_M0)
			setPlaneColors(model0Color, model0Color);
		else if (componentType == M0_TO_M1)
			setPlaneColors(model1Color, model0Color);
		else if (componentType == M1_TO_M2)
			setPlaneColors(model2Color, model1Color);
		else
			setPlaneColors(model2Color, model2Color);
	}
	
	public void setModelKeys(String model0Key, String model1Key, String model2Key) {
		setPlaneKeys(model0Key, model1Key);
		this.model2Key = model2Key;
		repaint();
	}
	
	protected MultipleRegnModel getPlaneA() {
		return (componentType == DATA_TO_M0) ? super.getPlaneA()
						: (componentType == M0_TO_M1) ? super.getPlaneB()
						: (MultipleRegnModel)getVariable(model2Key);
	}
	
	protected MultipleRegnModel getPlaneB() {
		return (componentType == DATA_TO_M0) || (componentType == DATA_TO_M2) ? null
						: (componentType == M0_TO_M1) ? super.getPlaneA()
						: super.getPlaneB();
	}
	
	protected void drawComponentLine(Graphics g, int planeLevel, double y, double x, double z,
										double fitA, double fitB, Point crossPos, Point fitAPos, Point fitBPos,
										boolean aPlaneFromTop, boolean bPlaneFromTop,
										boolean crossBehindA, boolean crossBehindB, boolean aFurthestBack) {
		if (componentType == M0_TO_M1 || componentType == M1_TO_M2) {
			boolean behindA = (fitB < fitA) == aPlaneFromTop;
			boolean behindB = (fitA < fitB) == bPlaneFromTop;
			
			Color c[] = (componentType == M0_TO_M1) ? m0m1Color : m1m2Color;
			g.setColor(getShade(c, planeLevel, behindA, behindB, aFurthestBack));
			g.drawLine(fitAPos.x, fitAPos.y, fitBPos.x, fitBPos.y);
		}
		else {
			Color c[] = (componentType == DATA_TO_M0) ? dm0Color : dm2Color;
			g.setColor(getShade(c, planeLevel, crossBehindA, crossBehindB, aFurthestBack));
			g.drawLine(crossPos.x, crossPos.y, fitAPos.x, fitAPos.y);
		}
	}
	
	protected void drawPlanesData(Graphics g, int planeLevel) {
		MultipleRegnModel modelA = getPlaneA();
		MultipleRegnModel modelB = getPlaneB();
		double explan[] = new double[2];
		
		boolean aPlaneFromTop = viewingPlaneFromTop(modelA);
		boolean bPlaneFromTop = viewingPlaneFromTop(modelB);
		
		NumVariable xVariable = (NumVariable)getVariable("x");
		NumVariable zVariable = (NumVariable)getVariable("z");
		NumVariable yVariable = (NumVariable)getVariable("y");
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ze = zVariable.values();
		ValueEnumeration ye = yVariable.values();
		
		Point crossPos = null;
		
		g.setColor(Color.red);
		FlagEnumeration fe = getSelection().getEnumeration();
		while (xe.hasMoreValues() && ze.hasMoreValues()) {
			double x = xe.nextDouble();
			double z = ze.nextDouble();
			double y = ye.nextDouble();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				crossPos = getScreenPoint(x, y, z, crossPos);
				if (crossPos != null)
					drawCrossBackground(g, crossPos);
			}
		}
		
		
		xe = xVariable.values();
		ze = zVariable.values();
		ye = yVariable.values();
		Point fitAPos = null;
		Point fitBPos = null;
		while (xe.hasMoreValues() && ze.hasMoreValues()) {
			double x = xe.nextDouble();
			double z = ze.nextDouble();
			double y = ye.nextDouble();
			
			explan[0] = x;
			explan[1] = z;
			double fitA = getFitA(modelA, modelB, explan);
			double fitB = (modelB == null) ? Double.NaN : getFitB(modelA, modelB, explan);
			
			boolean crossBehindA = (y < fitA) == aPlaneFromTop;
			boolean crossBehindB = (y < fitB) == bPlaneFromTop;
			boolean aFurthestBack = (fitA < fitB) == aPlaneFromTop;
			
			crossPos = getScreenPoint(x, y, z, crossPos);
			if (crossPos != null) {
				fitAPos = getScreenPoint(x, fitA, z, fitAPos);
				fitBPos = getScreenPoint(x, fitB, z, fitBPos);
				
				drawComponentLine(g, planeLevel, y, x, z, fitA, fitB, crossPos, fitAPos, fitBPos,
										aPlaneFromTop, bPlaneFromTop, crossBehindA, crossBehindB, aFurthestBack);
				
				g.setColor(getShade(crossColor, planeLevel, crossBehindA, crossBehindB, aFurthestBack));
				drawCross(g, crossPos);
			}
		}
	}
	
}
	
