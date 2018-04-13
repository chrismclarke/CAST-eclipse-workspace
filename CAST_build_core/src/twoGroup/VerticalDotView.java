package twoGroup;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class VerticalDotView extends DotPlotView {
	static final public int NO_MEAN = 0;
	static final public int MEAN_LINE = 1;
	static final public int MEAN_CHANGE = 2;
	
	static final private int kMaxHorizJitter = 30;
	static final private int kHalfOverlap = 15;
	static final private int kHalfMeanExtra = 10;
	static final private int kArrowSize = 5;
	
	static final private Color kPaleGrayColor = new Color(0xCCCCCC);
	
	static final private Color kVeryPaleBlue = new Color(0xCCEEFF);
	static final private Color kPaleBlue = new Color(0x99CCFF);
	static final private Color kMidBlue = new Color(0x3399FF);
	
	static final private double kZ50 = 0.6745;
	
	private String modelKey;
	private HorizAxis groupAxis;
	private CatVariable groupingVariable;
	
	private int meanDisplay = NO_MEAN;
	
	private boolean show50PercentBand = false;
	
	public VerticalDotView(DataSet theData, XApplet applet, VertAxis numAxis, HorizAxis groupAxis,
								String yKey, String xKey, String modelKey, double jitterPropn) {
		super(theData, applet, numAxis, jitterPropn);
		this.groupAxis = groupAxis;
		setActiveNumVariable(yKey);
		setActiveCatVariable(xKey);
		this.modelKey = modelKey;
		groupingVariable = (CatVariable)theData.getVariable(xKey);
	}
	
	public void setMeanDisplay(int meanDisplay) {
		this.meanDisplay = meanDisplay;
	}
	
	public void setShow50PercentBand(boolean show50PercentBand) {
		this.show50PercentBand = show50PercentBand;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = groupingVariable.getItemCategory(index);
			int offset = groupAxis.catValToPosition(groupIndex) - currentJitter / 2;
			newPoint.x += offset;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupingVariable.noOfCategories();
		return Math.min(kMaxHorizJitter,
							(getSize().width - getViewBorder().left - getViewBorder().right) / noOfGroups / 2);
	}
	
	private void drawBand(Graphics g, double mean, double sd, double z, int xCenter,
														int offset, Color fillColor, Point topLeftPoint,
														Point bottomRightPoint) {
		int yTopPos = axis.numValToRawPosition(mean + z * sd);
		int yBottomPos = axis.numValToRawPosition(mean - z * sd);
		topLeftPoint = translateToScreen(yTopPos, xCenter - offset,
																							topLeftPoint);
		bottomRightPoint = translateToScreen(yBottomPos, xCenter + offset,
																						bottomRightPoint);
		g.setColor(fillColor);
		g.fillRect(topLeftPoint.x, topLeftPoint.y, 2 * offset + 1,
															bottomRightPoint.y - topLeftPoint.y + 1);
	}
	
	private double getMean(int i, GroupsDataSet anovaData) {
		if (anovaData != null)
			return anovaData.getMean(i);
		else {
			NumVariable yVar = getNumVariable();
			CatVariable groupVar = getCatVariable();
			int n = 0;
			double sy = 0.0;
			
			ValueEnumeration ye = yVar.values();
			ValueEnumeration ge = groupVar.values();
			while (ye.hasMoreValues() && ge.hasMoreValues()) {
				double y = ye.nextDouble();
				int g = groupVar.labelIndex(ge.nextValue());
				if (g == i) {
					n ++;
					sy += y;
				}
			}
			return sy / n;
		}
	}
	
	public void paintView(Graphics g) {
		if (modelKey != null) {
			GroupsModelVariable model = (GroupsModelVariable)getData().getVariable(modelKey);
			int noOfCategories = groupingVariable.noOfCategories();
			Point topLeftPoint = new Point(0,0);
			Point bottomRightPoint = new Point(0,0);
			int offset = (currentJitter * 12) / 20;	//	20% more than currentJitter / 2
			
			for (int i=0 ; i<noOfCategories ; i++)
				try {
					double mean = model.getMean(i).toDouble();
					double sd = model.getSD(i).toDouble();
					int xCenter = groupAxis.catValToPosition(i);
					
					if (show50PercentBand) {
						drawBand(g, mean, sd, 2.0, xCenter, offset, kVeryPaleBlue, topLeftPoint,
																							bottomRightPoint);
						drawBand(g, mean, sd, kZ50, xCenter, offset, kPaleBlue, topLeftPoint,
																							bottomRightPoint);
					}
					else
						drawBand(g, mean, sd, 2.0, xCenter, offset, kPaleGrayColor, topLeftPoint,
																							bottomRightPoint);
					
					int meanPos = axis.numValToPosition(mean);
					topLeftPoint = translateToScreen(meanPos, xCenter - offset,
																										topLeftPoint);
					g.setColor(show50PercentBand ? kMidBlue : Color.gray);
					g.drawLine(topLeftPoint.x, topLeftPoint.y, topLeftPoint.x + 2 * offset,
																										topLeftPoint.y);
				} catch (AxisException e) {
				}
			g.setColor(getForeground());
		}
		if (meanDisplay != NO_MEAN) {
			DataSet tempData = getData();
			GroupsDataSet anovaData = (tempData instanceof GroupsDataSet) ?
																(GroupsDataSet)getData() : null;
			int noOfCategories = groupingVariable.noOfCategories();
			int xSpacing = groupAxis.catValToPosition(1) - groupAxis.catValToPosition(0);
			int offset;
			if (meanDisplay == MEAN_LINE)
				offset = Math.min(currentJitter / 2 + kHalfMeanExtra, xSpacing / 2);
			else
				offset = Math.min(xSpacing / 2 + kHalfOverlap, xSpacing - currentJitter / 2);
			Point thePoint = null;
			int previousY = 0;
			for (int i=0 ; i<noOfCategories ; i++)
				try {
					double mean = getMean(i, anovaData);
					int yPos = axis.numValToPosition(mean);
					int xCenter = groupAxis.catValToPosition(i);
					thePoint = translateToScreen(yPos, xCenter - offset, thePoint);
					g.setColor(show50PercentBand ? Color.black : Color.blue);
					g.drawLine(thePoint.x, thePoint.y, thePoint.x + 2 * offset, thePoint.y);
					if (meanDisplay == MEAN_CHANGE && i > 0) {
						int xPos = xCenter - xSpacing / 2;
						thePoint = translateToScreen(yPos, xPos, thePoint);
						g.setColor(Color.red);
						if (thePoint.y >= previousY + 2) {
							int yEnd = thePoint.y - 1;
							int yStart = previousY + 1;
							g.drawLine(thePoint.x, yStart, thePoint.x, yEnd);
							g.drawLine(thePoint.x + 1, yStart, thePoint.x + 1, yEnd);
							g.drawLine(thePoint.x - kArrowSize, yEnd - kArrowSize, thePoint.x, yEnd);
							g.drawLine(thePoint.x + 1 + kArrowSize, yEnd - kArrowSize, thePoint.x + 1, yEnd);
							
							g.drawLine(thePoint.x + 1 - kArrowSize, yEnd - kArrowSize, thePoint.x, yEnd - 1);
							g.drawLine(thePoint.x + kArrowSize, yEnd - kArrowSize, thePoint.x + 1, yEnd - 1);
						}
						else if (thePoint.y <= previousY - 2) {
							int yEnd = thePoint.y + 1;
							int yStart = previousY - 1;
							g.drawLine(thePoint.x + 1, yStart, thePoint.x + 1, yEnd);
							g.drawLine(thePoint.x - kArrowSize, yEnd + kArrowSize, thePoint.x, yEnd);
							g.drawLine(thePoint.x + 1 + kArrowSize, yEnd + kArrowSize, thePoint.x + 1, yEnd);
							
							g.drawLine(thePoint.x + 1 - kArrowSize, yEnd + kArrowSize, thePoint.x, yEnd + 1);
							g.drawLine(thePoint.x + kArrowSize, yEnd + kArrowSize, thePoint.x + 1, yEnd + 1);
						}
					}
					previousY = thePoint.y;
				} catch (AxisException e) {
				}
			g.setColor(getForeground());
		}
		super.paintView(g);
	}
}