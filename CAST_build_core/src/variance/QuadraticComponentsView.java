package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;

import ssq.*;


public class QuadraticComponentsView extends DataWithComponentView {
//	static public final String QUAD_COMPONENT_PLOT = "quadraticComponent";
	
	static final private Color kPaleRed = new Color(0xFFCCCC);
	
	private String quadKey;
	
	public QuadraticComponentsView(DataSet theData, XApplet applet, HorizAxis xAxis,
							VertAxis yAxis, String xKey, String yKey, String linKey, String quadKey,
							String modelKey, int initialComponentType) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, linKey, modelKey, initialComponentType);
		this.quadKey = quadKey;
	}
	
	private Point getDataPoint(double y, VertAxis yAxis, int xPos, Point dataPoint) {
		int yPos = yAxis.numValToRawPosition(y);
		return translateToScreen(xPos, yPos, dataPoint);
	}
	
	private Point getFittedPoint(CoreModelVariable lsFit, VertAxis yAxis, Value x,
																															int xPos, Point fitPoint) {
		double fit = lsFit.evaluateMean(x);
		int fitPos = yAxis.numValToRawPosition(fit);
		return translateToScreen(xPos, fitPos, fitPoint);
	}
	
	protected void drawComponent(Graphics g, int meanOnScreen, CoreModelVariable linFit) {
		CoreModelVariable quadFit = (CoreModelVariable)getVariable(quadKey);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		Variable xVar = (Variable)getVariable(xKey);
		ValueEnumeration xe = xVar.values();
//		Point dataPoint = null;
		Point p1 = new Point(0, 0);
		Point p2 = new Point(0, 0);
		int index = 0;
		g.setColor(QuadComponentVariable.kComponentColor[componentDisplay]);
		
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			Value x = xe.nextValue();
			int xPos = getXPos(x, xVar, index);
			
			switch (componentDisplay) {
				case QuadComponentVariable.TOTAL:
					p1 = getDataPoint(ye.nextDouble(), yAxis, xPos, p1);
					p2.x = p1.x;
					p2.y = meanOnScreen;
					break;
				case QuadComponentVariable.LINEAR:
					double linFitVal = linFit.evaluateMean(x);
					int linFitPos = yAxis.numValToRawPosition(linFitVal);
					p1  = translateToScreen(xPos, linFitPos, p1);
					p2.x = p1.x;
					p2.y = meanOnScreen;
					break;
				case QuadComponentVariable.QUADRATIC:
					linFitVal = linFit.evaluateMean(x);
					linFitPos = yAxis.numValToRawPosition(linFitVal);
					p1  = translateToScreen(xPos, linFitPos, p1);
					
					double quadFitVal = quadFit.evaluateMean(x);
					int quadFitPos = yAxis.numValToRawPosition(quadFitVal);
					p2  = translateToScreen(xPos, quadFitPos, p2);
					break;
				case QuadComponentVariable.RESIDUAL:
				default:
					p1 = getDataPoint(ye.nextDouble(), yAxis, xPos, p1);
					
					quadFitVal = quadFit.evaluateMean(x);
					quadFitPos = yAxis.numValToRawPosition(quadFitVal);
					p2  = translateToScreen(xPos, quadFitPos, p2);
					break;
			}
			
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			
			index ++;
		}
	}
	
	protected void drawAllComponents(Graphics g, int meanOnScreen, CoreModelVariable linFit,
																int selectedIndex) {
		CoreModelVariable quadFit = (CoreModelVariable)getVariable(quadKey);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Variable xVar = (Variable)getVariable(xKey);
		
		Value x = xVar.valueAt(selectedIndex);
		int xPos = getXPos(x, xVar, selectedIndex);
		
		double y = yVar.doubleValueAt(selectedIndex);
		Point dataPoint  = getDataPoint(y, yAxis, xPos, null);
		
		Point linFitPoint  = getFittedPoint(linFit, yAxis, x, xPos, null);
		Point quadFitPoint  = getFittedPoint(quadFit, yAxis, x, xPos, null);
		
		int halfLineOffset = kLineOffset / 2;
		
		g.setColor(QuadComponentVariable.kTotalColor);
		drawArrow(g, dataPoint.x - 3 * halfLineOffset, dataPoint.y, meanOnScreen);
		
		g.setColor(QuadComponentVariable.kLinearColor);
		drawArrow(g, dataPoint.x - halfLineOffset, linFitPoint.y, meanOnScreen);
		
		g.setColor(QuadComponentVariable.kQuadraticColor);
		drawArrow(g, dataPoint.x + halfLineOffset, quadFitPoint.y, linFitPoint.y);
		
		g.setColor(QuadComponentVariable.kResidualColor);
		drawArrow(g, dataPoint.x + 3 * halfLineOffset, dataPoint.y, quadFitPoint.y);
	}
	
	protected void drawFittedMean(Graphics g, CoreModelVariable linFit) {
		super.drawFittedMean(g, linFit);
		
		CoreModelVariable quadFit = (CoreModelVariable)getVariable(quadKey);
		g.setColor(kPaleRed);
		quadFit.drawMean(g, this, xAxis, yAxis);
	}
}
	
