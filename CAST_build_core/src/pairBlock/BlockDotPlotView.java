package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;



public class BlockDotPlotView extends DotPlotView {
	private static final int kMaxVertJitter = 30;
	
	static final public int PROFILE_IN_BLOCKS = 0;
	static final public int BLOCK_MEANS = 1;
	
//	static final private Color kLightGray = new Color(0xDDDDDD);
	
	private boolean showBlocks = false;
	
	protected NumCatAxis groupAxis;
	protected CatVariable groupVariable, blockVariable;
	
	private int drawType = PROFILE_IN_BLOCKS;
	
	private boolean isHorizontal;
	
	public BlockDotPlotView(DataSet theData, XApplet applet, String groupKey, String blockKey, NumCatAxis numAxis,
								NumCatAxis groupAxis, double jitter) {
		super(theData, applet, numAxis, jitter);
		groupVariable = (CatVariable)theData.getVariable(groupKey);
		blockVariable = (CatVariable)theData.getVariable(blockKey);
		this.groupAxis = groupAxis;
		isHorizontal = groupAxis instanceof VertAxis;
	}
	
	public void setShowBlocks(boolean showBlocks) {
		this.showBlocks = showBlocks;
	}
	
	public void setDrawType(int drawType) {
		this.drawType = drawType;
	}
	
/*
	private boolean inSelectedBlock(int itemIndex) {
		int selectedIndex = getSelection().findSingleSetFlag();
		Value selectedBlock = (selectedIndex < 0) ? null : blockVariable.valueAt(selectedIndex);
		Value itemBlock = blockVariable.valueAt(itemIndex);
		return itemBlock == selectedBlock;
	}
*/
	
	protected int groupIndex(int itemIndex) {
		if (showBlocks)
			return blockVariable.getItemCategory(itemIndex);
		else
			return 0;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = groupVariable.getItemCategory(index);
			int offset = groupAxis.catValToPosition(groupIndex) - currentJitter / 2;
			if (isHorizontal)
				newPoint.y -= offset;
			else
				newPoint.x += offset;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupVariable.noOfCategories();
		return Math.min(kMaxVertJitter,
							(getSize().height - getViewBorder().top - getViewBorder().bottom) / noOfGroups / 4);
	}
	
	protected void drawBackground(Graphics g, Value selectedBlock) {
		if (showBlocks || selectedBlock != null) {
			drawProfiles(g, selectedBlock);
			if (drawType == BLOCK_MEANS)
				drawBlockMeans(g, selectedBlock);
		}
	}
	
	private void drawProfiles(Graphics g, Value selectedBlock) {
		int nBlocks = blockVariable.noOfCategories();
		int nGroups = groupVariable.noOfCategories();
		double[][] sy = new double[nBlocks][];
		int[][] n = new int[nBlocks][];
		for (int i=0 ; i<nBlocks ; i++) {
			sy[i] = new double[nGroups];
			n[i] = new int[nGroups];
		}
		ValueEnumeration ye = getNumVariable().values();
		ValueEnumeration be = blockVariable.values();
		ValueEnumeration ge = groupVariable.values();
		while (ye.hasMoreValues()) {
			double nextVal = ye.nextDouble();
			int blockIndex = blockVariable.labelIndex(be.nextValue());
			int groupIndex = groupVariable.labelIndex(ge.nextValue());
			sy[blockIndex][groupIndex] += nextVal;
			n[blockIndex][groupIndex] ++;
		}
		
		Point p1 = null;
		Point p2 = null;
		for (int bi=0 ; bi<nBlocks ; bi++) {
			if (selectedBlock == null || blockVariable.getLabel(bi) == selectedBlock) {
				g.setColor(mixColors(Color.white, getCrossColor(bi), 0.7));
				
				p1 = null;
				for (int gi=0 ; gi<nGroups ; gi++) {
					if (n[bi][gi] > 0) {
						int groupPos = groupAxis.catValToPosition(gi);
						double mean = sy[bi][gi] / n[bi][gi];
						int yPos = axis.numValToRawPosition(mean);
						p2 = translateToScreen(yPos, groupPos, p2);
					}
					else
						p2 = null;
					if (p1 != null && p2 != null)
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					
					Point pTemp = p1;
					p1 = p2;
					p2 = pTemp;
				}
			}
		}
	}
	
	private void drawBlockMeans (Graphics g, Value selectedBlock) {
		int nBlocks = blockVariable.noOfCategories();
		double[] sy = new double[nBlocks];
		int[] n = new int[nBlocks];
		ValueEnumeration ye = getNumVariable().values();
		ValueEnumeration be = blockVariable.values();
		while (ye.hasMoreValues()) {
			double nextVal = ye.nextDouble();
			int blockIndex = blockVariable.labelIndex(be.nextValue());
			sy[blockIndex] += nextVal;
			n[blockIndex] ++;
		}
		
		Point p = null;
		for (int bi=0 ; bi<nBlocks ; bi++) {
			if (selectedBlock == null || blockVariable.getLabel(bi) == selectedBlock) {
				g.setColor(mixColors(Color.white, getCrossColor(bi), 0.7));
				
				double mean = sy[bi] / n[bi];
				int yPos = axis.numValToRawPosition(mean);
				p = translateToScreen(yPos, 0, p);
				g.drawLine(p.x, 0, p.x, getSize().height);
			}
		}
	}
	
	public void paintView(Graphics g) {
		int selectedIndex = getSelection().findSingleSetFlag();
		Value selectedBlock = (selectedIndex < 0) ? null : blockVariable.valueAt(selectedIndex);
		
		drawBackground(g, selectedBlock);
		
		NumVariable variable = getNumVariable();
		Point thePoint = null;
		
		checkJittering();
		
		g.setColor(getForeground());
		ValueEnumeration ye = variable.values();
		ValueEnumeration be = blockVariable.values();
		int index = 0;
		while (ye.hasMoreValues()) {
			NumValue nextVal = (NumValue)ye.nextValue();
			Value blockVal = be.nextValue();
			if (selectedBlock == null || selectedBlock == blockVal) {
				thePoint = getScreenPoint(index, nextVal, thePoint);
				if (thePoint != null)
					drawMark(g, thePoint, groupIndex(index));
			}
			index++;
		}
	}

}