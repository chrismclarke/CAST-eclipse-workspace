package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

//import dotPlot.*;


public class BlockTreatDotView extends DotPlotView {
	private static final int kMaxJitter = 30;
	
//	static public final String TREAT_BLOCK_DOTPLOT = "treatBlockDotPlot";
	
	private NumCatAxis treatAxis;
	private CatVariable treatVariable, blockVariable;
	
	private boolean showData = false;
	private boolean showBlocks = false;
	
	public BlockTreatDotView(DataSet theData, XApplet applet, NumCatAxis numAxis, NumCatAxis treatAxis,
								String yKey, String treatKey, String blockKey) {
		super(theData, applet, numAxis, 0.5);
		this.treatAxis = treatAxis;
		setActiveNumVariable(yKey);
		treatVariable = (CatVariable)getVariable(treatKey);
		blockVariable = (CatVariable)getVariable(blockKey);
	}
	
	public void setShowData(boolean showData) {
		if (this.showData == showData)
			return;
		this.showData = showData;
		repaint();
	}
	
	public void setShowBlocks(boolean showBlocks) {
		if (this.showBlocks == showBlocks)
			return;
		this.showBlocks = showBlocks;
		repaint();
	}
	
	protected int groupIndex(int itemIndex) {
		return showBlocks ? blockVariable.getItemCategory(itemIndex) : 0;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = treatVariable.getItemCategory(index);
			int offset = treatAxis.catValToPosition(groupIndex) - currentJitter / 2;
			newPoint.x += offset;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = treatVariable.noOfCategories();
		return Math.min(kMaxJitter,
							(getSize().width - getViewBorder().left - getViewBorder().right) / noOfGroups / 2);
	}
	
	private void drawBackground(Graphics g) {
		int noOfCategories = treatVariable.noOfCategories();
		int xSpacing = treatAxis.catValToPosition(1) - treatAxis.catValToPosition(0);
		int offset = Math.min(xSpacing / 2, xSpacing - currentJitter / 2);
		
		int n[] = new int[noOfCategories];
		double sx[] = new double[noOfCategories];
		ValueEnumeration e = getNumVariable().values();
		int index = 0;
		while (e.hasMoreValues()) {
			int group = treatVariable.getItemCategory(index);
			n[group] ++;
			sx[group] += e.nextDouble();
			index ++;
		}
		
		g.setColor(Color.red);
		Point thePoint = null;
		for (int treat=0 ; treat<noOfCategories ; treat++) {
			double mean = sx[treat] / n[treat];
			int yPos = axis.numValToRawPosition(mean);
			int xCenter = treatAxis.catValToPosition(treat);
			thePoint = translateToScreen(yPos, xCenter - offset, thePoint);
			g.drawLine(thePoint.x, thePoint.y, thePoint.x + 2 * offset, thePoint.y);
		}
	}
	
	public void paintView(Graphics g) {
		if (showData) {
			drawBackground(g);
			super.paintView(g);
		}
	}
}