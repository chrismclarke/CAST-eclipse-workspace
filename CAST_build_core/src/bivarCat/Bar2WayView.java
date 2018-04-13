package bivarCat;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;

import cat.CountPropnAxis;


public class Bar2WayView extends TwoWayView {
//	static public final String BAR_2WAY_PLOT = "bar2WayPlot";
	
	static private final int kMinLeftRightBorder = 1;
	static private final int kMaxLeftRightBorder = 7;
	static private final int kMinTopBottomBorder = 1;
	static private final int kMaxTopBottomBorder = 7;
	static private final int kBarWidth = 17;
	
	static private final int kFinalFrame = 50;
	static private final int kFramesPerSec = 20;
	
	static final private Color kGridColor = new Color(0xDDDDDD);
	
	protected CountPropnAxis vertAxis;
	protected Axis horizAxis;
	protected Bar2WayInfo startBarInfo, endBarInfo;
	
	public Bar2WayView(DataSet theData, XApplet applet, CountPropnAxis vertAxis, Axis horizAxis,
																											String xKey, String yKey) {
		super(theData, applet, xKey, yKey);
		this.vertAxis = vertAxis;
		this.horizAxis = horizAxis;
	}
	
	protected int getBarWidth() {
		return kBarWidth;
	}
	
	protected int [] getMainOffsets(int noOfCats) {
		int [] result = new int[noOfCats];
		for (int i=0 ; i<noOfCats ; i++)
			result[i] = (int)Math.round((horizAxis.getAxisLength() - 1) * (i + 0.5) / noOfCats);
		return result;
	}
	
	private int [] getSecondaryOffsets(int noOfCats) {
		int [] result = new int[noOfCats];
		
		int horizPos = - (getBarWidth() + 1) * noOfCats / 2;
		for (int i=0 ; i<noOfCats ; i++) {
			result[i] = horizPos;
			horizPos += getBarWidth() + 1;
		}
		return result;
	}
	
	private int [] getZeroOffsets(int noOfCats) {
		int [] result = new int[noOfCats];
		int offset = -(getBarWidth() + 1) / 2;
		for (int i=0 ; i<noOfCats ; i++)
			result[i] = offset;
		return result;
	}
	
	private Bar2WayInfo getBarInfo(int mainGrouping, int vertScale, boolean stacked) {
		CatVariable x = (CatVariable)getVariable(xKey);
		Variable y = (Variable)getVariable(yKey);
		
		initialise(x, y);
		
		int noOfXCats = x.noOfCategories();
		int noOfYCats = yCounts.length;
				
		int leftRightBorder = (mainGrouping == XMAIN) ? kMinLeftRightBorder : kMaxLeftRightBorder;
		int topBottomBorder = (mainGrouping == XMAIN) ? kMinTopBottomBorder : kMaxTopBottomBorder;
		
		
		double [][] cellHt = new double[noOfXCats][];
		for (int i=0 ; i<noOfXCats ; i++) {
			cellHt[i] = new double[noOfYCats];
			for (int j=0 ; j<noOfYCats ; j++)
				cellHt[i][j] = (vertScale == COUNT) ? vertAxis.getAxisProportion(jointCounts[i][j])
								: (vertScale == PROPN_IN_X || vertScale == PERCENT_IN_X) ? ((double)jointCounts[i][j]) / xCounts[i]
								:	((double)jointCounts[i][j]) / yCounts[j];
		}
		
		
		double [][] cellBottom = new double[noOfXCats][];
		for (int i=0 ; i<noOfXCats ; i++)
			cellBottom[i] = new double[noOfYCats];
		
		if (stacked) {
			if (mainGrouping == XMAIN)
				for (int i=0 ; i<noOfXCats ; i++) {
					double bottom = 0.0;
					for (int j=0 ; j<noOfYCats ; j++) {
						cellBottom[i][j] = bottom;
						bottom += cellHt[i][j];
					}
				}
			else
				for (int j=0 ; j<noOfYCats ; j++) {
					double bottom = 0.0;
					for (int i=0 ; i<noOfXCats ; i++) {
						cellBottom[i][j] = bottom;
						bottom += cellHt[i][j];
					}
				}
		}
		else
			//		This should not be needed, but MW VM initialises all cells to infinity?!!
			for (int i=0 ; i<noOfXCats ; i++)
				for (int j=0 ; j<noOfYCats ; j++)
					cellBottom[i][j] = 0.000000001;

		int [] xHoriz = mainGrouping == XMAIN ? getMainOffsets(noOfXCats)
															: stacked ? getZeroOffsets(noOfXCats)
															: getSecondaryOffsets(noOfXCats);
		int [] yHoriz = mainGrouping == YMAIN ? getMainOffsets(noOfYCats)
															: stacked ? getZeroOffsets(noOfYCats)
															: getSecondaryOffsets(noOfYCats);
		int [][] horizPos = new int[noOfXCats][];
		for (int i=0 ; i<noOfXCats ; i++) {
			horizPos[i] = new int[noOfYCats];
			for (int j=0 ; j<noOfYCats ; j++)
				horizPos[i][j] = xHoriz[i] + yHoriz[j];
		}
		
		return new Bar2WayInfo(leftRightBorder, topBottomBorder, cellHt, cellBottom, horizPos);
	}
	
	protected Color [] getOuterColors(int noOfXCats) {
		return ContinTableView.getColors(ContinTableView.OUTER, noOfXCats);
	}
	
	protected Color [] getInnerColors(int noOfYCats) {
		return ContinTableView.getColors(ContinTableView.INNER, noOfYCats);
	}
	
	private void drawAxisGrid(Graphics g) {
		g.setColor(kGridColor);
		Point p = null;
		Enumeration e = vertAxis.getLabels().elements();
		double axisMin = vertAxis.minOnAxis;
		double axisMax = vertAxis.maxOnAxis;
		while (e.hasMoreElements()) {
			AxisLabel nextLabel = (AxisLabel)e.nextElement();
			double labelValue = axisMin + nextLabel.position * (axisMax - axisMin);
			int y = vertAxis.numValToRawPosition(labelValue);
			p = translateToScreen(0, y, p);
			g.drawLine(0, p.y, getSize().width, p.y);
		}
	}
	
	public void paintView(Graphics g) {
		if (endBarInfo == null)
			endBarInfo = getBarInfo(mainGrouping, vertScale, stacked);
		if (startBarInfo == null)
			startBarInfo = endBarInfo;
		
		int currentFrame = getCurrentFrame();
		if (currentFrame == 0 || currentFrame == kFinalFrame)
			drawAxisGrid(g);
		
		Bar2WayInfo currentBarInfo = (currentFrame == 0) ? startBarInfo
							: (currentFrame == kFinalFrame) ? endBarInfo
							: new Bar2WayInfo(startBarInfo, endBarInfo, currentFrame, kFinalFrame);
		int leftRightBorder = currentBarInfo.getLeftRightBorder();
		int topBottomBorder = currentBarInfo.getTopBottomBorder();
		
		int noOfXCats = xCounts.length;
		int noOfYCats = yCounts.length;
		
		Color outerColor[] = getOuterColors(noOfXCats);
		Color innerColor[] = getInnerColors(noOfYCats);
		
		Point topLeft = null;
		Point bottomLeft = null;
		for (int i=0 ; i<noOfXCats ; i++) {
			for (int j=0 ; j<noOfYCats ; j++) {
				double bottomPropn = currentBarInfo.getCellBottom(i, j);
				double htPropn = currentBarInfo.getCellHt(i, j);
				int bottom = vertAxis.numValToRawPosition(bottomPropn);
				int top = vertAxis.numValToRawPosition(bottomPropn + htPropn);
				int left = currentBarInfo.getHorizPos(i, j);
				
				topLeft = translateToScreen(left, top, topLeft);
				bottomLeft = translateToScreen(left, bottom, bottomLeft);
				if (bottomLeft.y == getSize().height - 1)
					bottomLeft.y = getSize().height;	//		because proportion 0.0 maps to position 1 on axis
															//		(lowest position where line will draw)
				
				g.setColor(outerColor[i]);
				g.fillRect(topLeft.x, topLeft.y, getBarWidth(), bottomLeft.y - topLeft.y);
				if (innerColor != null) {
					int height = bottomLeft.y - topLeft.y;
					int localTBBorder = height > 3 * topBottomBorder ? topBottomBorder
													: Math.max(height / 3, 1);
					if (localTBBorder > 0) {
						g.setColor(innerColor[j]);
						g.fillRect(topLeft.x + leftRightBorder, topLeft.y + localTBBorder,
														getBarWidth() - 2 * leftRightBorder,
														bottomLeft.y - topLeft.y - 2 * localTBBorder);
					}
				}
			}
		}
	}
	
	public void setDisplayType(int newMainGrouping, int newVertScale, boolean newStacked) {
		if (mainGrouping != newMainGrouping || vertScale != newVertScale
																					|| stacked != newStacked) {
			if (vertScale != newVertScale)
				vertAxis.setAlternateLabels((newVertScale == COUNT) ? 1
								: (newVertScale == PERCENT_IN_X || newVertScale == PERCENT_IN_Y) ? 2
								: 0);
			
			mainGrouping = newMainGrouping;
			vertScale = newVertScale;
			stacked = newStacked;
			
			startBarInfo = endBarInfo;
			setFrame(0);
			endBarInfo = null;
			animateFrames(1, kFinalFrame - 1, kFramesPerSec, null);
		}
	}
}
	
