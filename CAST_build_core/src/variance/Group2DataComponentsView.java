package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;

import ssq.*;


public class Group2DataComponentsView extends DataWithComponentView {
	
	public Group2DataComponentsView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
							String xKey, String yKey, String lsKey, String modelKey, int initialComponentType) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, lsKey, modelKey, initialComponentType);
	}
	
	protected void drawOneComponent(Graphics g, int meanOnScreen, Point dataPoint, Variable xVar,
															Value x, int xPos, CoreModelVariable lsFit, Point fitPoint) {
		int group = ((CatVariable)xVar).labelIndex(x);
		if (componentDisplay == TwoGroupComponentVariable.WITHIN_0 && group == 1
											|| componentDisplay == TwoGroupComponentVariable.WITHIN_1 && group == 0)
			return;
		
		g.setColor(TwoGroupComponentVariable.kComponentColor[componentDisplay]);
		if (componentDisplay != TwoGroupComponentVariable.TOTAL) {
			double fit = lsFit.evaluateMean(x);
			int fitPos = yAxis.numValToRawPosition(fit);
			fitPoint  = translateToScreen(xPos, fitPos, fitPoint);
		}
		switch (componentDisplay) {
			case TwoGroupComponentVariable.TOTAL:
				g.drawLine(dataPoint.x, dataPoint.y, dataPoint.x, meanOnScreen);
				break;
			case TwoGroupComponentVariable.BETWEEN_MEANS:
				g.drawLine(dataPoint.x, fitPoint.y, dataPoint.x, meanOnScreen);
				break;
			case TwoGroupComponentVariable.WITHIN_0:
			case TwoGroupComponentVariable.WITHIN_1:
				g.drawLine(dataPoint.x, fitPoint.y, dataPoint.x, dataPoint.y);
				break;
			default:
				break;
		}
	}
	
	protected void drawAllComponents(Graphics g, int meanOnScreen, CoreModelVariable lsFit,
																int selectedIndex) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Variable xVar = (Variable)getVariable(xKey);
		
		Value x = xVar.valueAt(selectedIndex);
		int xPos = getXPos(x, xVar, selectedIndex);
		int group = ((CatVariable)xVar).labelIndex(x);
		
		double y = yVar.doubleValueAt(selectedIndex);
		int yPos = yAxis.numValToRawPosition(y);
		Point dataPoint  = translateToScreen(xPos, yPos, null);
		
		double fit = lsFit.evaluateMean(x);
		int fitPos = yAxis.numValToRawPosition(fit);
		Point fitPoint  = translateToScreen(xPos, fitPos, null);
		
		g.setColor(TwoGroupComponentVariable.kTotalColor);
		drawArrow(g, dataPoint.x - kLineOffset, dataPoint.y, meanOnScreen);
		
		g.setColor(TwoGroupComponentVariable.kBetweenColor);
		drawArrow(g, dataPoint.x + kLineOffset, fitPoint.y, meanOnScreen);
		
		if (group == 0)
			g.setColor(TwoGroupComponentVariable.kWithin0Color);
		else
			g.setColor(TwoGroupComponentVariable.kWithin1Color);
		drawArrow(g, dataPoint.x, dataPoint.y, fitPoint.y);
	}
}
	
