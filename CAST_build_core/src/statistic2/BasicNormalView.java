package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import distn.*;


public class BasicNormalView extends BasicDataView {
	static final private int kMaxHitSlop = 4;
	static final private int kMinSeparation = 30;
	static final private int kArrowVert = 10;
	static final private int kArrowLength = 6;
	
	static final private double kSdFactor = 1.5;
	
	private BackgroundNormalArtist backgroundDrawer;
	
	private NormalDistnVariable normDistn;
	
	public BasicNormalView(DataSet data, XApplet applet, NumCatAxis valAxis,
													String yKey, int meanDecimals, int sdDecimals, String normKey) {
		super(data, applet, valAxis, yKey, meanDecimals, sdDecimals);
		backgroundDrawer = new BackgroundNormalArtist(normKey, data);
		backgroundDrawer.setMaxDensityFactor(1.5);
		normDistn = (NormalDistnVariable)data.getVariable(normKey);
		normDistn.setDecimals(meanDecimals, sdDecimals);
	}
	
	public void changeClasses(double class0Start, double classWidth) {
	}
	
	protected double[] countClasses(double localClassStart[]) {
		return null;
	}
	
	public void setMeanSD(double mean, double sd) {
		normDistn.setMean(mean);
		normDistn.setSD(sd);
	}
	
	protected double[] defaultClasses() {
		return initialiseClasses();
	}
	
	protected double[] initialiseClasses() {
		normDistn.setMean((axis.maxOnAxis + axis.minOnAxis) / 2);
		normDistn.setSD((axis.maxOnAxis - axis.minOnAxis) / 8);
		return null;
	}
	
	protected double[] defaultCounts(double localClassStart[]) {
		return null;
	}
	
	public void setMessages(String[] messageArray, NumValue exactAnswer) {
		messageArray[2] = "Good! The exact standard deviation of the normal distribution is " + exactAnswer.toString();
		messageArray[3] = "Not close enough. From the 70-95-100 rule, about 95% of the normal density area will be within 2s of the mean (a range of 4s values).";
		messageArray[4] = "The best estimate of standard deviation for this histogram is " + exactAnswer.toString();
	}

//-------------------------------------------------------------------
	
	public double findMeanFromHisto() {
		return normDistn.getMean().toDouble();
	}
	
	public double findSDFromHisto() {
		return normDistn.getSD().toDouble();
	}

//-------------------------------------------------------------------
	
	public void paintView(Graphics g) {
		initialise();
		
		drawTopBorder(g);
		
//		int maxHt = getDisplayWidth() - topBorder(g);
		
		backgroundDrawer.paintDistn(g, this, axis);
		
		double mean = normDistn.getMean().toDouble();
		int meanPos = axis.numValToRawPosition(mean);
		double sd = normDistn.getSD().toDouble();
		int meanPlus2SdPos = axis.numValToRawPosition(mean + kSdFactor * sd);
		
		Point p = translateToScreen(meanPos, 0, null);
		if (dragIndex != 1)
			drawDragArrow(g, p.x, dragIndex == 0);
		
		p = translateToScreen(meanPlus2SdPos, 0, p);
		if (dragIndex != 0)
			drawDragArrow(g, p.x, dragIndex == 1);
	}
	
	private void drawDragArrow(Graphics g, int arrowPos, boolean selected) {
		int arrowVert = getSize().height - kArrowVert;
		
		g.setColor(Color.red);
		if (selected) {
			g.drawLine(arrowPos - kArrowLength, arrowVert, arrowPos + kArrowLength, arrowVert);
			g.drawLine(arrowPos - kArrowLength + 1, arrowVert - 1, arrowPos + kArrowLength - 1, arrowVert - 1);
			g.drawLine(arrowPos - kArrowLength + 1, arrowVert + 1, arrowPos + kArrowLength - 1, arrowVert + 1);
			for (int i=2 ; i<4 ; i++) {
				g.drawLine(arrowPos - kArrowLength + i, arrowVert - i, arrowPos - kArrowLength + i, arrowVert + i);
				g.drawLine(arrowPos + kArrowLength - i, arrowVert - i, arrowPos + kArrowLength - i, arrowVert + i);
			}
		}
		else {
			g.drawLine(arrowPos - kArrowLength, arrowVert, arrowPos + kArrowLength, arrowVert);
			g.drawLine(arrowPos - kArrowLength, arrowVert, arrowPos - kArrowLength + 3, arrowVert - 3);
			g.drawLine(arrowPos - kArrowLength, arrowVert, arrowPos - kArrowLength + 3, arrowVert + 3);
			g.drawLine(arrowPos + kArrowLength, arrowVert, arrowPos + kArrowLength - 3, arrowVert - 3);
			g.drawLine(arrowPos + kArrowLength, arrowVert, arrowPos + kArrowLength - 3, arrowVert + 3);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		initialise();
		
		Point p = translateFromScreen(x, y, null);
		
		double mean = normDistn.getMean().toDouble();
		int meanPos = axis.numValToRawPosition(mean);
		double sd = normDistn.getSD().toDouble();
		int meanPlus2SdPos = axis.numValToRawPosition(mean + kSdFactor * sd);
		
		int nearestIndex = -1;
		int minDist = Integer.MAX_VALUE;
		
		if (Math.abs(p.x - meanPos) < Math.abs(minDist)) {
			nearestIndex = 0;
			minDist = p.x - meanPos;
		}
		
		if (Math.abs(p.x - meanPlus2SdPos) < Math.abs(minDist)) {
			nearestIndex = 1;
			minDist = p.x - meanPlus2SdPos;
		}
		
		if (Math.abs(minDist) <= kMaxHitSlop) 
			return new HorizDragPosInfo(x, nearestIndex, minDist);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point p = translateFromScreen(x, y, null);
		double mean = normDistn.getMean().toDouble();
		int meanPos = axis.numValToRawPosition(mean);
		if (dragIndex == 0) {
			double sd = normDistn.getSD().toDouble();
			int meanPlus2SdPos = axis.numValToRawPosition(mean + kSdFactor * sd);
			if (p.x >= getSize().width - (meanPlus2SdPos - meanPos) - 2)
				return null;
		}
		else if (dragIndex == 1) {
			if (p.x - kMinSeparation <= meanPos)
				return null;
		}
		
		return new HorizDragPosInfo(x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		dragIndex = dragPos.index;
		hitOffset = dragPos.hitOffset;
		resetMessage();
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			Point p = translateFromScreen(dragPos.x, 0, null);
			
			
			try {
				double x = axis.positionToNumVal(p.x);
				
				if (dragIndex == 0)
					normDistn.setMean(x);
				else {
					double mean = normDistn.getMean().toDouble();
					normDistn.setSD((x - mean) / kSdFactor);
				}
				repaint();
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		dragIndex = -1;
		repaint();
	}
	
	
}
	
