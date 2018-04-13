package curveInteract;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


class QuadraticSolution {
	boolean startBOverA;
	double low, high;
	
	QuadraticSolution(double b0, double b1, double b2, double a0, double a1) {
		double c = (b0 - a0);
		double b = (b1 - a1);
		double a = b2;
		if (a == 0.0) {
			startBOverA = b < 0.0;
			low = - c / b;
			high = Double.POSITIVE_INFINITY;
		}
		else {
			startBOverA = b2 > 0.0;
			double temp = Math.sqrt(b * b - 4 * a * c);
			if (Double.isNaN(temp))
				low = high = Double.POSITIVE_INFINITY;
			else if (a > 0.0) {
				low = (-b - temp) / (2 * a);
				high = (-b + temp) / (2 * a);
			}
			else {
				low = (-b + temp) / (2 * a);
				high = (-b - temp) / (2 * a);
			}
		}
	}
}


public class SeqQuadPlanesView extends Rotate3DPlanesView {
	
	static final public int DATA_TO_M0 = 0;		//	M0 = mean
	static final public int M0_TO_M1 = 1;			//	M1 = linear in X and Z
	static final public int M1_TO_M2 = 2;			//	M2 = quad in X, lin in Z or vice versa
	static final public int DATA_TO_M2 = 3;
	
	static final private int kGridLines = 10;
	
	private int componentType = DATA_TO_M0;
	
	private String model2Key;
	private Color model0Color, model1Color, model2Color, model2Behind1Color;
	
	private Color crossColor[];
	private Color dm0Color[];
	private Color m0m1Color[];
	private Color m1m2Color;
	private Color dm2Color;
	
	private double tempExplan4[] = new double[4];
	
	public SeqQuadPlanesView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String model0Key, String model1Key, String model2Key, Color model0Color,
						Color model1Color, Color model2Color, Color[] componentColor) {
		super(theData, applet, xAxis, yAxis, zAxis, model0Key, model1Key, model0Color, model0Color);
		this.model2Key = model2Key;
		crossColor = getAllShades(Color.black, 0.55);
		if (componentColor[0] != null)
			dm0Color = getAllShades(componentColor[0]);
		if (componentColor[1] != null)
			m0m1Color = getAllShades(componentColor[1]);
		m1m2Color = componentColor[2];
		dm2Color = componentColor[3];
		this.model0Color = model0Color;
		this.model1Color = model1Color;
		this.model2Color = model2Color;
		model2Behind1Color = mixColors(model1Color, darkenColor(model2Color, 0.2), 0.8);
//		model2Behind1Color = Color.blue;
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
			setPlaneColors(model1Color, model1Color);
		else
			;			//	No planes drawn for DATA_TO_M2
	}
	
	public void setModelKeys(String model0Key, String model1Key, String model2Key) {
		setPlaneKeys(model0Key, model1Key);
		this.model2Key = model2Key;
	}
	
	protected MultipleRegnModel getPlaneA() {
		return (componentType == DATA_TO_M0) ? (MultipleRegnModel)getData().getVariable(model0Key)
						: (componentType == M0_TO_M1) || (componentType == M1_TO_M2) ? (MultipleRegnModel)getData().getVariable(model1Key)
						: null;
	}
	
	protected MultipleRegnModel getPlaneB() {
		return (componentType == DATA_TO_M0) || (componentType == M0_TO_M1) ? (MultipleRegnModel)getData().getVariable(model0Key)
						: (componentType == M1_TO_M2) ? (MultipleRegnModel)getData().getVariable(model1Key)
						: null;
	}
	
	private double getFit(MultipleRegnModel model, double[] explan) {
		tempExplan4[0] = explan[0];
		tempExplan4[1] = explan[1];
		tempExplan4[2] = explan[0] * explan[0];
		tempExplan4[3] = explan[1] * explan[1];
		return model.evaluateMean(tempExplan4);
	}
	
	protected double getFitA(MultipleRegnModel modelA, MultipleRegnModel modelB, double[] explan) {
		return getFit(modelA, explan);
	}
	
	protected double getFitB(MultipleRegnModel modelA, MultipleRegnModel modelB, double[] explan) {
		return getFit(modelB, explan);
	}
	
	protected Point getModelPoint(double[] xVals, MultipleRegnModel model) {
		double fit = getFit(model, xVals);
		return getScreenPoint(xVals[0], fit, xVals[1], null);
	}
	
	protected Point getModelPoint(double[] xVals, MultipleRegnModel model, Point p) {
		double fit = getFit(model, xVals);
		return getScreenPoint(xVals[0], fit, xVals[1], p);
	}
	
	private void drawLineAtX(Graphics g, MultipleRegnModel model, MultipleRegnModel planeModel,
								QuadraticSolution quadSoln, double lowZ, double highZ, double[] explan,
								Point p0, Point p1, Color abovePlaneColor, Color belowPlaneColor) {
		explan[1] = lowZ;
		p0 = getModelPoint(explan, model, p0);
		if (quadSoln == null || quadSoln.high < lowZ || quadSoln.low > highZ) {
			g.setColor(quadSoln == null || quadSoln.startBOverA ? abovePlaneColor : belowPlaneColor);
			explan[1] = highZ;
			p1 = getModelPoint(explan,  model, p1);
			
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		else {
			if (quadSoln.low > lowZ) {
				g.setColor(quadSoln.startBOverA ? abovePlaneColor : belowPlaneColor);
				explan[1] = quadSoln.low;
				p1 = getModelPoint(explan,  model, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				Point tempP = p0; p0 = p1 ; p1 = tempP;
			}
			
			g.setColor(quadSoln.startBOverA ? belowPlaneColor : abovePlaneColor);
			explan[1] = Math.min(highZ, quadSoln.high);
			p1 = getModelPoint(explan,  model, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			Point tempP = p0; p0 = p1 ; p1 = tempP;
			
			if (quadSoln.high < highZ) {
				g.setColor(quadSoln.startBOverA ? abovePlaneColor : belowPlaneColor);
				explan[1] = highZ;
				p1 = getModelPoint(explan,  model, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
	}
	
	private void drawLineAtZ(Graphics g, MultipleRegnModel model, MultipleRegnModel planeModel,
								QuadraticSolution quadSoln, double lowX, double highX, double[] explan,
								Point p0, Point p1, Color abovePlaneColor, Color belowPlaneColor) {
		explan[0] = lowX;
		p0 = getModelPoint(explan, model, p0);
		if (quadSoln == null || quadSoln.high < lowX || quadSoln.low > highX) {
			g.setColor(quadSoln == null || quadSoln.startBOverA ? abovePlaneColor : belowPlaneColor);
			explan[0] = highX;
			p1 = getModelPoint(explan,  model, p1);
			
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		else {
			if (quadSoln.low > lowX) {
				g.setColor(quadSoln.startBOverA ? abovePlaneColor : belowPlaneColor);
				explan[0] = quadSoln.low;
				p1 = getModelPoint(explan,  model, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
				Point tempP = p0; p0 = p1 ; p1 = tempP;
			}
			
			g.setColor(quadSoln.startBOverA ? belowPlaneColor : abovePlaneColor);
			explan[0] = Math.min(highX, quadSoln.high);
			p1 = getModelPoint(explan,  model, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			Point tempP = p0; p0 = p1 ; p1 = tempP;
			
			if (quadSoln.high < highX) {
				g.setColor(quadSoln.startBOverA ? abovePlaneColor : belowPlaneColor);
				explan[0] = highX;
				p1 = getModelPoint(explan,  model, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
	}
	
	private void drawModelGrid(Graphics g, MultipleRegnModel model,
											MultipleRegnModel planeModel, boolean planeFromTop, int planeLevel) {
		
		double xMin = xAxis.getMinOnAxis();
		double zMin = zAxis.getMinOnAxis();
		double xMax = xAxis.getMaxOnAxis();
		double zMax = zAxis.getMaxOnAxis();
		
		Point p0 = new Point(0, 0);
		Point p1 = new Point(0, 0);
		
		double x[] = new double[2];
		
		Color abovePlaneColor = (planeLevel == BEHIND_0_PLANES) ? model2Color
														: planeFromTop ? model2Color : model2Behind1Color;
		Color belowPlaneColor = (planeLevel == BEHIND_0_PLANES) ? model2Color
														: planeFromTop ? model2Behind1Color : model2Color;
		
		for (int i=0 ; i<=kGridLines ; i++) {
			x[0] = xMin + (xMax - xMin) * i / kGridLines;
			x[1] = 0.0;
			QuadraticSolution quadSoln = null;
			if (planeModel != null) {
				double b0 = getFit(model, x);
				double a0 = getFit(planeModel, x);
				double b1 = model.getParameter(2).toDouble();
				double a1 = planeModel.getParameter(2).toDouble();
				double b2 = model.getParameter(4).toDouble();
				quadSoln = new QuadraticSolution(b0, b1, b2, a0, a1);
			}
			
			if (model.getParameter(4).toDouble() == 0.0)
				drawLineAtX(g, model, planeModel, quadSoln, zMin, zMax, x, p0, p1,
																													abovePlaneColor, belowPlaneColor);
			else
				for (int j=0 ; j<kGridLines ; j++)
					drawLineAtX(g, model, planeModel, quadSoln, zMin + (zMax - zMin) * j / kGridLines,
																	zMin + (zMax - zMin) * (j + 1) / kGridLines, x, p0, p1,
																	abovePlaneColor, belowPlaneColor);
		}
		
		for (int j=0 ; j<=kGridLines ; j++) {
			x[0] = 0.0;
			x[1] = zMin + (zMax - zMin) * j / kGridLines;
			QuadraticSolution quadSoln = null;
			if (planeModel != null) {
				double b0 = getFit(model, x);
				double a0 = getFit(planeModel, x);
				double b1 = model.getParameter(1).toDouble();
				double a1 = planeModel.getParameter(1).toDouble();
				double b2 = model.getParameter(3).toDouble();
				quadSoln = new QuadraticSolution(b0, b1, b2, a0, a1);
			}
			
			if (model.getParameter(3).toDouble() == 0.0)
				drawLineAtZ(g, model, planeModel, quadSoln, xMin, xMax, x, p0, p1,
																													abovePlaneColor, belowPlaneColor);
			else
				for (int i=0 ; i<kGridLines ; i++)
					drawLineAtZ(g, model, planeModel, quadSoln, xMin + (xMax - xMin) * i / kGridLines,
																	xMin + (xMax - xMin) * (i + 1) / kGridLines, x, p0, p1,
																	abovePlaneColor, belowPlaneColor);
		}
	}
	
	protected void drawComponentLine(Graphics g, int planeLevel, double y, double x, double z,
										double fitA, double fitB, Point crossPos, Point fitAPos, Point fitBPos,
										boolean aPlaneFromTop, boolean bPlaneFromTop,
										boolean crossBehindA, boolean crossBehindB, boolean aFurthestBack) {
		if (componentType == M0_TO_M1) {
			boolean behindA = (fitB < fitA) == aPlaneFromTop;
			boolean behindB = (fitA < fitB) == bPlaneFromTop;
			
			g.setColor(getShade(m0m1Color, planeLevel, behindA, behindB, aFurthestBack));
			g.drawLine(fitAPos.x, fitAPos.y, fitBPos.x, fitBPos.y);
		}
		else if (componentType == DATA_TO_M0) {
			g.setColor(getShade(dm0Color, planeLevel, crossBehindB, crossBehindA, aFurthestBack));
			g.drawLine(crossPos.x, crossPos.y, fitBPos.x, fitBPos.y);
		}
		else if (componentType == M1_TO_M2) {
			g.setColor(m1m2Color);
			g.drawLine(fitAPos.x, fitAPos.y, fitBPos.x, fitBPos.y);
		}
		else {
			g.setColor(dm2Color);
			g.drawLine(crossPos.x, crossPos.y, fitAPos.x, fitAPos.y);
		}
	}
	
	protected void drawPlanesData(Graphics g, int planeLevel) {
		MultipleRegnModel modelM0 = (componentType == DATA_TO_M0) ? null
					: (componentType == M0_TO_M1) ? (MultipleRegnModel)getData().getVariable(model0Key)
					: (componentType == M1_TO_M2) ? (MultipleRegnModel)getData().getVariable(model1Key)
					: (MultipleRegnModel)getData().getVariable(model2Key);
		MultipleRegnModel modelM1 = (componentType == DATA_TO_M0) ? (MultipleRegnModel)getData().getVariable(model0Key)
					: (componentType == M0_TO_M1) ? (MultipleRegnModel)getData().getVariable(model1Key)
					: (componentType == M1_TO_M2) ? (MultipleRegnModel)getData().getVariable(model2Key)
					: null;
		
		boolean m0PlaneFromTop = viewingPlaneFromTop(modelM0);
		boolean m1PlaneFromTop = viewingPlaneFromTop(modelM1);
		
		if (componentType == M1_TO_M2)
			drawModelGrid(g, modelM1, modelM0, m0PlaneFromTop, planeLevel);
		else if (componentType == DATA_TO_M2)
			drawModelGrid(g, modelM0, null, true, planeLevel);
			
		
		double explan[] = new double[2];
		
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
		Point fitM0Pos = null;
		Point fitM1Pos = null;
		while (xe.hasMoreValues() && ze.hasMoreValues()) {
			double x = xe.nextDouble();
			double z = ze.nextDouble();
			double y = ye.nextDouble();
			
			explan[0] = x;
			explan[1] = z;
			double fitM0 = (modelM0 == null) ? Double.NaN : getFitA(modelM0, modelM1, explan);
			double fitM1 = (modelM1 == null) ? Double.NaN : getFitB(modelM0, modelM1, explan);
			
			boolean crossBehindM0 = (y < fitM0) == m0PlaneFromTop;
			boolean crossBehindM1 = (y < fitM1) == m1PlaneFromTop;
			boolean m0FurthestBack = (fitM0 < fitM1) == m0PlaneFromTop;
			
			crossPos = getScreenPoint(x, y, z, crossPos);
			if (crossPos != null) {
				fitM0Pos = getScreenPoint(x, fitM0, z, fitM0Pos);
				fitM1Pos = getScreenPoint(x, fitM1, z, fitM1Pos);
				
				drawComponentLine(g, planeLevel, y, x, z, fitM0, fitM1, crossPos, fitM0Pos, fitM1Pos,
										m0PlaneFromTop, m1PlaneFromTop, crossBehindM0, crossBehindM1, m0FurthestBack);
				
				g.setColor(getShade(crossColor, planeLevel, crossBehindM0, crossBehindM1, m0FurthestBack));
				drawCross(g, crossPos);
			}
		}
	}
	
}
	
