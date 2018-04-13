package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;
import coreVariables.*;


public class TwoGroupSpreadDotView extends DotPlotView {
	static final private Color kArrowColor[] = {new Color(0xCCFFCC), new Color(0xDDDDFF)};
	static final private Color kGroupColor[] = {TwoGroupComponentVariable.kWithin0Color, TwoGroupComponentVariable.kWithin1Color};
	static final private Color kMeanColor = new Color(0xFFBBBB);
	
	static final private int kMaxVertJitter = 30;
	static final private int kArrowOffset = 12;
	static final private int kArrowWidth = 8;
	static final private int kArrowHead = 10;
	static final private int kHalfMeanWidth = 15;
	
	private NumCatAxis groupAxis;
	private CatVariable groupingVariable;
	
	public TwoGroupSpreadDotView(DataSet theData, XApplet applet, NumCatAxis numAxis, NumCatAxis groupAxis,
								String yKey, String xKey) {
		super(theData, applet, numAxis, 0.7);
		this.groupAxis = groupAxis;
		setActiveNumVariable(yKey);
		setActiveCatVariable(xKey);
		groupingVariable = (CatVariable)theData.getVariable(xKey);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = groupingVariable.getItemCategory(index);
			int offset = groupAxis.catValToPosition(groupIndex) - currentJitter / 2;
			if (vertNotHoriz)
				newPoint.x += offset;
			else
				newPoint.y -= offset;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupingVariable.noOfCategories();
		return Math.min(kMaxVertJitter, getDisplayWidth() / noOfGroups / 3);
	}
	
	protected int groupIndex(int itemIndex) {
		int groupIndex = groupingVariable.getItemCategory(itemIndex);
		return groupIndex;
	}
	
	public void drawMark(Graphics g, Point thePos, int markIndex) {
		g.setColor(kGroupColor[markIndex]);
		super.drawMark(g, thePos, 0);
	}
	
	public void paintView(Graphics g) {
		int noOfCategories = groupingVariable.noOfCategories();
		int xSpacing = groupAxis.catValToPosition(1) - groupAxis.catValToPosition(0);
		int arrowOffset = xSpacing / 2 - kArrowOffset;
		Point lowPoint = null;
		Point highPoint = null;
		Point meanPoint = null;
		
		double mean[];
		double sd[];
		if (getData() instanceof GroupsDataSet) {
			GroupsDataSet anovaData = (GroupsDataSet)getData();
			mean = new double[noOfCategories];
			sd = new double[noOfCategories];
			for (int i=0 ; i<noOfCategories ; i++) {
				mean[i] = anovaData.getMean(i);
				sd[i] = anovaData.getSD(i);
			}
		}
		else {
			FixedMeanSDVariable yVar = (FixedMeanSDVariable)getNumVariable();
			mean = yVar.getMeans();
			sd = yVar.getSDs();
		}
		
		for (int i=0 ; i<noOfCategories ; i++)
			try {
				int meanPos = axis.numValToPosition(mean[i]);
				int lowPos = axis.numValToPosition(mean[i] - sd[i]);
				int highPos = axis.numValToPosition(mean[i] + sd[i]);
				
				int xCenter = groupAxis.catValToPosition(i);
				meanPoint = translateToScreen(meanPos, xCenter + arrowOffset, meanPoint);
				lowPoint = translateToScreen(lowPos, xCenter + arrowOffset, lowPoint);
				highPoint = translateToScreen(highPos, xCenter + arrowOffset, highPoint);
				
				int arrowHead = vertNotHoriz ? Math.min(kArrowHead, (lowPoint.y - highPoint.y) / 3)
																		: Math.min(kArrowHead, (highPoint.x - lowPoint.x) / 3);
				int arrowWidth = Math.min(kArrowWidth, arrowHead);
				
				g.setColor(kMeanColor);
				
				if (vertNotHoriz)
					g.drawLine(meanPoint.x - kHalfMeanWidth, meanPoint.y, meanPoint.x + kHalfMeanWidth, meanPoint.y);
				else
					g.drawLine(meanPoint.x, meanPoint.y - kHalfMeanWidth, meanPoint.x, meanPoint.y + kHalfMeanWidth);
				
				g.setColor(kArrowColor[i]);
				
				if (vertNotHoriz) {
					g.fillRect(lowPoint.x - arrowWidth / 2, highPoint.y + arrowWidth,
																			arrowWidth, lowPoint.y - highPoint.y - 2 * arrowWidth);
					
					for (int j=0 ; j<arrowHead ; j++) {
						g.drawLine(lowPoint.x - j, highPoint.y + j, lowPoint.x + j, highPoint.y + j);
						g.drawLine(highPoint.x - j, lowPoint.y - j, highPoint.x + j, lowPoint.y - j);
					}
				}
				else {
					g.fillRect(lowPoint.x + arrowWidth, lowPoint.y - arrowWidth / 2,
																			highPoint.x - lowPoint.x - 2 * arrowWidth, arrowWidth);
					
					for (int j=0 ; j<arrowHead ; j++) {
						g.drawLine(lowPoint.x + j, lowPoint.y - j, lowPoint.x + j, lowPoint.y + j);
						g.drawLine(highPoint.x - j, highPoint.y - j, highPoint.x - j, highPoint.y + j);
					}
				}
			} catch (AxisException e) {
			}
		g.setColor(getForeground());
		
		super.paintView(g);
	}
}