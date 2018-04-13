package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class TreatDotView extends DotPlotView {
//	static final public String TREAT_DOTPLOT = "treatPlot";
	
	static final private int kMaxHorizJitter = 30;
	static final private int kHalfOverlap = 15;
//	static final private int kHalfMeanExtra = 10;
	static final private int kArrowSize = 5;
	
	private HorizAxis groupAxis;
	private CatVariable groupingVariable, symbolVariable;
	
	public TreatDotView(DataSet theData, XApplet applet, VertAxis numAxis, HorizAxis groupAxis,
								String yKey, String xKey, double jitterPropn) {
		super(theData, applet, numAxis, jitterPropn);
		this.groupAxis = groupAxis;
		setActiveNumVariable(yKey);
		symbolVariable = groupingVariable = (CatVariable)theData.getVariable(xKey);
	}
	
	public void setSymbolVariable(String symbolKey) {
		symbolVariable = (CatVariable)getData().getVariable(symbolKey);
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
	
	protected int groupIndex(int itemIndex) {
		return symbolVariable.getItemCategory(itemIndex);
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupingVariable.noOfCategories();
		return Math.min(kMaxHorizJitter,
							(getSize().width - getViewBorder().left - getViewBorder().right) / noOfGroups / 2);
	}
	
	public void paintView(Graphics g) {
		int noOfCategories = groupingVariable.noOfCategories();
		int xSpacing = groupAxis.catValToPosition(1) - groupAxis.catValToPosition(0);
		int offset = Math.min(xSpacing / 2 + kHalfOverlap, xSpacing - currentJitter / 2);
		Point thePoint = null;
		int previousY = 0;
		
		int n[] = new int[noOfCategories];
		double sx[] = new double[noOfCategories];
		ValueEnumeration e = getNumVariable().values();
		int index = 0;
		while (e.hasMoreValues()) {
			int group = groupingVariable.getItemCategory(index);
			n[group] ++;
			sx[group] += e.nextDouble();
			index ++;
		}
	
		for (int group=0 ; group<noOfCategories ; group++)
			try {
				double mean = sx[group] / n[group];
				if (Double.isNaN(mean))
					break;
				int yPos = axis.numValToPosition(mean);
				int xCenter = groupAxis.catValToPosition(group);
				thePoint = translateToScreen(yPos, xCenter - offset, thePoint);
				g.setColor(Color.blue);
				g.drawLine(thePoint.x, thePoint.y, thePoint.x + 2 * offset, thePoint.y);
				if (group > 0) {
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
			} catch (AxisException ex) {
			}
		g.setColor(getForeground());
		
		super.paintView(g);
	}
}