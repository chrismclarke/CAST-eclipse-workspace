package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;

import ssq.*;


public class PureComponentsView extends DataWithComponentView {
	static final private Color kPaleRed = new Color(0xFFCCCC);
	static final private Color kModelFillColor = new Color(0xCCCCCC);
	static final private Color kModelMeanColor = Color.white;
	static final private int kMeanLineWidth = 20;
	static final private int kModelWidth = 10;
	
	private String xCatKey, factorKey, groupModelKey;
	
	public PureComponentsView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis, String xKey,
							String xCatKey, String yKey, String linKey, String factorKey,
							String modelKey, int initialComponentType) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, linKey, null, initialComponentType);
		this.xCatKey = xCatKey;
		this.factorKey = factorKey;
		groupModelKey = modelKey;
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
		CoreModelVariable factorFit = (CoreModelVariable)getVariable(factorKey);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		Variable xVar = (Variable)getVariable(xKey);
		ValueEnumeration xe = xVar.values();
		Variable xCatVar = (Variable)getVariable(xCatKey);
		ValueEnumeration xCate = xCatVar.values();
//		Point dataPoint = null;
		Point p1 = new Point(0, 0);
		Point p2 = new Point(0, 0);
		int index = 0;
		g.setColor(QuadComponentVariable.kComponentColor[componentDisplay]);
		
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			Value x = xe.nextValue();
			Value xCat = xCate.nextValue();
			int xPos = getXPos(x, xVar, xCat, xCatVar, index);
			
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
					
					double factorFitVal = factorFit.evaluateMean(xCat);
					int factorFitPos = yAxis.numValToRawPosition(factorFitVal);
					p2  = translateToScreen(xPos, factorFitPos, p2);
					break;
				case QuadComponentVariable.RESIDUAL:
				default:
					p1 = getDataPoint(ye.nextDouble(), yAxis, xPos, p1);
					
					factorFitVal = factorFit.evaluateMean(xCat);
					factorFitPos = yAxis.numValToRawPosition(factorFitVal);
					p2  = translateToScreen(xPos, factorFitPos, p2);
					break;
			}
			
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			
			index ++;
		}
	}
	
	protected void drawAllComponents(Graphics g, int meanOnScreen, CoreModelVariable linFit,
																int selectedIndex) {
		CoreModelVariable factorFit = (CoreModelVariable)getVariable(factorKey);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Variable xVar = (Variable)getVariable(xKey);
		Variable xCatVar = (Variable)getVariable(xCatKey);
		
		Value x = xVar.valueAt(selectedIndex);
		Value xCat = xCatVar.valueAt(selectedIndex);
		int xPos = getXPos(x, xVar, selectedIndex);
		
		double y = yVar.doubleValueAt(selectedIndex);
		Point dataPoint  = getDataPoint(y, yAxis, xPos, null);
		
		Point linFitPoint  = getFittedPoint(linFit, yAxis, x, xPos, null);
		Point factorFitPoint  = getFittedPoint(factorFit, yAxis, xCat, xPos, null);
		
		int halfLineOffset = kLineOffset / 2;
		
		g.setColor(QuadComponentVariable.kTotalColor);
		drawArrow(g, dataPoint.x - 3 * halfLineOffset, dataPoint.y, meanOnScreen);
		
		g.setColor(QuadComponentVariable.kLinearColor);
		drawArrow(g, dataPoint.x - halfLineOffset, linFitPoint.y, meanOnScreen);
		
		g.setColor(QuadComponentVariable.kQuadraticColor);
		drawArrow(g, dataPoint.x + halfLineOffset, factorFitPoint.y, linFitPoint.y);
		
		g.setColor(QuadComponentVariable.kResidualColor);
		drawArrow(g, dataPoint.x + 3 * halfLineOffset, dataPoint.y, factorFitPoint.y);
	}
	
	protected void drawFittedMean(Graphics g, CoreModelVariable linFit) {
		super.drawFittedMean(g, linFit);
		
		GroupsModelVariable factorFit = (GroupsModelVariable)getVariable(factorKey);
		g.setColor(kPaleRed);
		
		CatVariable xCatVar = (CatVariable)getVariable(xCatKey);
		int meanLineWidth = Math.min(kMeanLineWidth, getSize().width / (xCatVar.noOfCategories() * 3));
		for (int i=0 ; i<xCatVar.noOfCategories() ; i++) {
			Value x = xCatVar.getLabel(i);
			double xValue = Double.parseDouble(x.toString());
			int xPos = xAxis.numValToRawPosition(xValue);
			
			Point fitPoint = getFittedPoint(factorFit, yAxis, x, xPos, null);
			g.drawLine(fitPoint.x - meanLineWidth, fitPoint.y, fitPoint.x + meanLineWidth,
																																	fitPoint.y);
		}
	}
	
	protected void drawModel(Graphics g) {
		if (groupModelKey != null) {
			GroupsModelVariable model = (GroupsModelVariable)getVariable(groupModelKey);
			if (model != null) {
				CatVariable xCatVar = (CatVariable)getVariable(xCatKey);
				int modelWidth = Math.min(kModelWidth, getSize().width / (xCatVar.noOfCategories() * 4));
				Point p0 = null;
				Point p1 = null;
				Point pMean = null;
				for (int i=0 ; i<xCatVar.noOfCategories() ; i++) {
					Value x = xCatVar.getLabel(i);
					double xValue = Double.parseDouble(x.toString());
					int xPos = xAxis.numValToRawPosition(xValue);
					
					NumValue mu = model.getMean(i);
					NumValue sigma = model.getSD(i);
					
					p0 = getDataPoint(mu.toDouble() + 2 * sigma.toDouble(), yAxis, xPos, p0);
					p1 = getDataPoint(mu.toDouble() - 2 * sigma.toDouble(), yAxis, xPos, p1);
					pMean = getDataPoint(mu.toDouble(), yAxis, xPos, pMean);
					
					g.setColor(kModelFillColor);
					g.fillRect(p0.x - modelWidth, p0.y, 2 * modelWidth, p1.y - p0.y);
					
					g.setColor(kModelMeanColor);
					g.drawLine(pMean.x - modelWidth, pMean.y, pMean.x + modelWidth, pMean.y);
				}
			}
			
			g.setColor(getForeground());
		}
	}
}
	
