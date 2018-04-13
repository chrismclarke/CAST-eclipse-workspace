package percentile;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

import boxPlot.*;


public class GroupedQuartileView extends GroupedBoxView {
//	static public final int DOT_PLOT = 0;
//	static public final int BOX_PLOT = 1;
	static public final int QUARTILE_AND_BOX_PLOT = 2;
	static public final int QUARTILE_AND_DOT_PLOT = 3;
	static public final int QUARTILE_PLOT = 4;
	
	static final protected Color kOuterColor = new Color(0x99CCFF);
	static final protected Color kInnerColor = new Color(0x6699FF);
	static final protected Color kMedianColor = new Color(0x336699);
	
	static final protected Color kLightGray = new Color(0xCCCCCC);
	
	public GroupedQuartileView(DataSet theData, XApplet applet, NumCatAxis theAxis, NumCatAxis groupAxis) {
		super(theData, applet, theAxis, groupAxis);
	}
	
	protected void fillBand(Graphics g, double[] lowerVal, double[] upperVal) {
		int nGroups = lowerVal.length;
		int xPoints[] = new int[2 * nGroups + 5];
		int yPoints[] = new int[2 * nGroups + 5];
		Point p = null;
		int groupShift = groupAxis.catValToPosition(1) - groupAxis.catValToPosition(0);
		
		for (int i=0 ; i<nGroups ; i++) {
			int yPos = axis.numValToRawPosition(upperVal[i]);
			int xPos = groupAxis.catValToPosition(i);
			p = translateToScreen(yPos, xPos, p);
			xPoints[i + 1] = p.x;
			yPoints[i + 1] = p.y;
		}
		xPoints[0] = xPoints[1] - groupShift;
		yPoints[0] = yPoints[nGroups];
		xPoints[nGroups + 1] = xPoints[nGroups] + groupShift;
		yPoints[nGroups + 1] = yPoints[1];
		
		for (int i=0 ; i<nGroups ; i++) {
			int yPos = axis.numValToRawPosition(lowerVal[i]);
			int xPos = groupAxis.catValToPosition(i);
			p = translateToScreen(yPos, xPos, p);
			xPoints[2 * nGroups + 2 - i] = p.x;
			yPoints[2 * nGroups + 2 - i] = p.y;
		}
		xPoints[2 * nGroups + 3] = xPoints[0];
		yPoints[2 * nGroups + 3] = yPoints[nGroups + 3];
		xPoints[nGroups + 2] = xPoints[nGroups + 1];
		yPoints[nGroups + 2] = yPoints[2 * nGroups + 2];
		
		xPoints[2 * nGroups + 4] = xPoints[0];
		yPoints[2 * nGroups + 4] = yPoints[0];
		
		g.fillPolygon(xPoints, yPoints, xPoints.length);
	}
	
	protected void drawMedian(Graphics g, double[] val) {
		int nGroups = val.length;
		int groupShift = groupAxis.catValToPosition(1) - groupAxis.catValToPosition(0);
		
		int yPos = axis.numValToRawPosition(val[nGroups - 1]);
		int xPos = groupAxis.catValToPosition(0) - groupShift;
		Point p0 = translateToScreen(yPos, xPos, null);
		Point p1 = null;
		
		for (int i=0 ; i<nGroups ; i++) {
			yPos = axis.numValToRawPosition(val[i]);
			xPos = groupAxis.catValToPosition(i);
			p1 = translateToScreen(yPos, xPos, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			Point temp = p0; p0 = p1 ; p1 = temp;
		}
		
		yPos = axis.numValToRawPosition(val[0]);
		xPos += groupShift;
		p1 = translateToScreen(yPos, xPos, p1);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
	}
	
	private void drawQuartileBands(Graphics g) {
		int nGroups = groupingVariable.noOfCategories();
		double upperVal[] = new double[nGroups];
		double lowerVal[] = new double[nGroups];
		for (int i=0 ; i<nGroups ; i++) {
			NumValue sortedData[] = numSubset[i].getSortedData();
			lowerVal[i] = sortedData[0].toDouble();
			upperVal[i] = sortedData[sortedData.length - 1].toDouble();
		}
		g.setColor(kOuterColor);
		fillBand(g, lowerVal, upperVal);
		
		for (int i=0 ; i<nGroups ; i++) {
			lowerVal[i] = groupedBoxInfo[i].boxVal[BoxInfo.LOW_QUART];
			upperVal[i] = groupedBoxInfo[i].boxVal[BoxInfo.HIGH_QUART];
		}
		g.setColor(kInnerColor);
		fillBand(g, lowerVal, upperVal);
		
		for (int i=0 ; i<nGroups ; i++)
			lowerVal[i] = groupedBoxInfo[i].boxVal[BoxInfo.MEDIAN];
		g.setColor(kMedianColor);
		drawMedian(g, lowerVal);
	}
	
	protected void drawGroupData(Graphics g, NumVariable variable) {
		switch (plotType) {
			case DOT_PLOT:
				for (int i=0 ; i<groupingVariable.noOfCategories() ; i++)
					paintGroupBackground(g, numSubset[i], groupedBoxInfo[i], i);
				drawDotPlot(g, variable);
				break;
			case BOX_PLOT:
				for (int i=0 ; i<groupingVariable.noOfCategories() ; i++)
					drawBoxPlot(g, numSubset[i].getSortedData(), groupedBoxInfo[i]);
				break;
			case QUARTILE_PLOT:
				drawQuartileBands(g);
				break;
			case QUARTILE_AND_BOX_PLOT:
				drawQuartileBands(g);
				for (int i=0 ; i<groupingVariable.noOfCategories() ; i++)
					drawBoxPlot(g, numSubset[i].getSortedData(), groupedBoxInfo[i]);
				break;
			case QUARTILE_AND_DOT_PLOT:
				drawQuartileBands(g);
				drawDotPlot(g, variable);
				break;
		}
	}
	
	public void paintView(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, getSize().width, getSize().height - getViewBorder().bottom );
		g.setColor(kLightGray);
		g.fillRect(0, getSize().height - getViewBorder().bottom, getSize().width, getViewBorder().bottom);
		
		NumVariable variable = getNumVariable();
		checkInitialisation(variable);
		if (jittering == null)
			initialiseJittering();
		
		drawGroupData(g, variable);
	}
	
	
}