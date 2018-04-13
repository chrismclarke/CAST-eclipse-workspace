package ssq;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class ResidsWithinGroupsView extends DataWithComponentView {
	static final private int kHalfMeanGap = 5;
	
//	static final private Color kPaleBlue = new Color(0x99CCFF);
//	static final private Color kPaleGray = new Color(0xDDDDDD);
//	static final private Color kMidGray = new Color(0xCCCCCC);
	
	public ResidsWithinGroupsView(DataSet theData, XApplet applet,
							HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey,
							String lsKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, lsKey, modelKey, BasicComponentVariable.RESIDUAL);
	}
	
	private Color lighterVersion(Color c) {
		int newRed = 255 - (255 - c.getRed()) / 4;
		int newBlue = 255 - (255 - c.getBlue()) / 4;
		int newGreen = 255 - (255 - c.getGreen()) / 4;
		return new Color(newRed, newGreen, newBlue);
	}
	
	protected boolean canDrawOverallMean() {
		return false;
	}
	
	protected void drawFittedMean(Graphics g, CoreModelVariable lsFit) {
						//		Based on GroupsModelVariable.drawMean();
		GroupsModelVariable groupsFit = (GroupsModelVariable)lsFit;
		CatVariable xVar = (CatVariable)getVariable(xKey);
		int noOfCategories = xVar.noOfCategories();
		int xSpacing = xAxis.catValToPosition(1) - xAxis.catValToPosition(0);
		int offset = xSpacing / 2 - kHalfMeanGap;
		Point thePoint = null;
		for (int i=0 ; i<noOfCategories ; i++)
			try {
				int yPos = yAxis.numValToPosition(groupsFit.getMean(i).toDouble());
				int xCenter = xAxis.catValToPosition(i);
				thePoint = translateToScreen(xCenter - offset, yPos, thePoint);
				g.setColor(lighterVersion(GroupResidComponent.kGroupColor[i]));
				g.drawLine(thePoint.x, thePoint.y, thePoint.x + 2 * offset, thePoint.y);
			} catch (AxisException e) {
			}
	}
	
	protected void drawOneComponent(Graphics g, int meanOnScreen, Point dataPoint, Variable xVar,
															Value x, int xPos, CoreModelVariable lsFit, Point fitPoint) {
		double fit = lsFit.evaluateMean(x);
		int fitPos = yAxis.numValToRawPosition(fit);
		fitPoint  = translateToScreen(xPos, fitPos, fitPoint);
		
		int xCat = ((CatVariable)xVar).labelIndex(x);
		g.setColor(GroupResidComponent.kGroupColor[xCat]);
		g.drawLine(dataPoint.x, fitPoint.y, dataPoint.x, dataPoint.y);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean canDrag() {
		return false;
	}
}
	
