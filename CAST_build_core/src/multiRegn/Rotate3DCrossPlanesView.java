package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class Rotate3DCrossPlanesView extends Rotate3DPlanesView {
	
	static final public int NO_LINES = -1;
	static final public int DATA_TO_A = 0;
	static final public int A_TO_B = 1;
	static final public int DATA_TO_B = 2;
	
//	static final private Color kDarkGreen = new Color(0x006600);
//	static final private Color kPaleGray = new Color(0xDDDDDD);
	
	private int componentType = DATA_TO_A;
	
	private Color crossColor[];
	private Color xaColor[];
	private Color xbColor[];
	private Color abColor[];
//	private Color hairColor[];
	
	public Rotate3DCrossPlanesView(DataSet theData, XApplet applet, D3Axis xAxis,
									D3Axis yAxis, D3Axis zAxis, String planeAKey, String planeBKey, Color planeAColor,
									Color planeBColor, Color[] componentColor) {
		super(theData, applet, xAxis, yAxis, zAxis, planeAKey, planeBKey, planeAColor, planeBColor);
		crossColor = getAllShades(Color.black, 0.55);
		if (componentColor != null) {
			if (componentColor[0] != null)
				xaColor = getAllShades(componentColor[0]);
			if (componentColor[1] != null)
				abColor = getAllShades(componentColor[1]);
			if (componentColor[2] != null)
				xbColor = getAllShades(componentColor[2]);
		}
//		hairColor = getAllShades(kPaleGray, 0.5);
	}
	
	public void setComponentType(int componentType) {
		this.componentType = componentType;
	}
	
	protected void drawComponentLine(Graphics g, int planeLevel, double y, double x, double z,
										double fitA, double fitB, Point crossPos, Point fitAPos, Point fitBPos,
										boolean aPlaneFromTop, boolean bPlaneFromTop,
										boolean crossBehindA, boolean crossBehindB, boolean aFurthestBack) {
		if (componentType == NO_LINES)
			return;
		else if (componentType == A_TO_B) {
			boolean behindA = (fitB < fitA) == aPlaneFromTop;
			boolean behindB = (fitA < fitB) == bPlaneFromTop;
			
			g.setColor(getShade(abColor, planeLevel, behindA, behindB, aFurthestBack));
			g.drawLine(fitAPos.x, fitAPos.y, fitBPos.x, fitBPos.y);
			
//			if ((y < fitA) == (y < fitB)) {
//				g.setColor(getShade(hairColor, planeLevel, crossBehindA, crossBehindB, aFurthestBack));
//				Point midPos = ((y < fitB) == (fitB < fitA)) ? fitBPos : fitAPos;
//				g.drawLine(crossPos.x, crossPos.y, midPos.x, midPos.y);
//			}
		}
		else {
			Color c[] = (componentType == DATA_TO_A) ? xaColor : xbColor;
			Point endPos = (componentType == DATA_TO_A) ? fitAPos : fitBPos;
			boolean crossesPlane = (componentType == DATA_TO_A) ? (y < fitB) == (fitB < fitA)
																													: (y < fitA) == (fitA < fitB);
			
			if (crossesPlane) {
				Point midPos = (componentType == DATA_TO_A) ? fitBPos : fitAPos;
				
				g.setColor(getShade(c, planeLevel, crossBehindA, crossBehindB, aFurthestBack));
				g.drawLine(crossPos.x, crossPos.y, midPos.x, midPos.y);
				
				boolean midBehindA = (fitB < fitA) == aPlaneFromTop;
				boolean midBehindB = (fitA < fitB) == bPlaneFromTop;
				
				g.setColor(getShade(c, planeLevel, midBehindA, midBehindB, aFurthestBack));
				g.drawLine(midPos.x, midPos.y, endPos.x, endPos.y);
			}
			else {
				g.setColor(getShade(c, planeLevel, crossBehindA, crossBehindB, aFurthestBack));
				g.drawLine(crossPos.x, crossPos.y, endPos.x, endPos.y);
			}
		}
	}
	
	protected double getNextY(ValueEnumeration ye) {
		return ye.nextDouble();
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
		ValueEnumeration ye = (yVariable == null) ? null : yVariable.values();
																	//	yVariable == null when used by RotateModelBandView
		Point crossPos = null;
		
		g.setColor(Color.red);
		FlagEnumeration fe = getSelection().getEnumeration();
		while (xe.hasMoreValues() && ze.hasMoreValues()) {
			double x = xe.nextDouble();
			double z = ze.nextDouble();
			double y = getNextY(ye);
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				crossPos = getScreenPoint(x, y, z, crossPos);
				if (crossPos != null)
					drawCrossBackground(g, crossPos);
			}
		}
		
		
		xe = xVariable.values();
		ze = zVariable.values();
		ye = (yVariable == null) ? null : yVariable.values();
		Point fitAPos = null;
		Point fitBPos = null;
		while (xe.hasMoreValues() && ze.hasMoreValues()) {
			double x = xe.nextDouble();
			double z = ze.nextDouble();
			double y = getNextY(ye);
			
			explan[0] = x;
			explan[1] = z;
			double fitA = getFitA(modelA, modelB, explan);
			double fitB = getFitB(modelA, modelB, explan);
			
			boolean crossBehindA = (y < fitA) == aPlaneFromTop;
			boolean crossBehindB = (y < fitB) == bPlaneFromTop;
			boolean aFurthestBack = (fitA < fitB) == aPlaneFromTop;
			
			crossPos = getScreenPoint(x, y, z, crossPos);
			if (crossPos != null) {
				fitAPos = getScreenPoint(x, fitA, z, fitAPos);
				fitBPos = getScreenPoint(x, fitB, z, fitBPos);
				
				drawComponentLine(g, planeLevel, y, x, z, fitA, fitB, crossPos, fitAPos, fitBPos,
										aPlaneFromTop, bPlaneFromTop, crossBehindA, crossBehindB, aFurthestBack);
				
//				if ((y < fitB) == (fitB < fitA)) {
//					midPos = getScreenPoint(x, fitB, z, midPos);
//					g.setColor(getShade(xaColor, planeLevel, crossBehindA, crossBehindB, aFurthestBack));
//					g.drawLine(crossPos.x, crossPos.y, midPos.x, midPos.y);
//					
//					boolean midBehindA = (fitB < fitA) == aPlaneFromTop;
//					boolean midBehindB = (fitA < fitB) == bPlaneFromTop;
//					g.setColor(getShade(xaColor, planeLevel, midBehindA, midBehindB, aFurthestBack));
//					g.drawLine(midPos.x, midPos.y, fitPos.x, fitPos.y);
//				}
//				else {
//					g.setColor(getShade(xaColor, planeLevel, crossBehindA, crossBehindB, aFurthestBack));
//					g.drawLine(crossPos.x, crossPos.y, fitPos.x, fitPos.y);
//				}
				
				g.setColor(getShade(crossColor, planeLevel, crossBehindA, crossBehindB, aFurthestBack));
				drawCross(g, crossPos);
			}
		}
	}
	
}
	
