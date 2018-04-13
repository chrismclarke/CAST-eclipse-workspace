package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;


public class DragOutlierStackedView extends StackedMeanSDView {
	static final private int kTopBorder = 30;
	
	static final private int kHitSlop = 6;
	
	private int dragIndex = -1;
	private int hitOffset;
	
//	private NumValue meanValue, sdValue;

	private double minLowDragOutlier, maxLowDragOutlier, minHighDragOutlier, maxHighDragOutlier;
	
	public DragOutlierStackedView(DataSet theData, XApplet applet, NumCatAxis theAxis,
										int meanDecimals, int sdDecimals, double minLowDragOutlier, double maxLowDragOutlier,
										double minHighDragOutlier, double maxHighDragOutlier) {
		super(theData, applet, theAxis, meanDecimals, sdDecimals);
		setViewBorder(new Insets(kTopBorder, 0, 5, 0));
		this.minLowDragOutlier = minLowDragOutlier;
		this.maxLowDragOutlier = maxLowDragOutlier;
		this.minHighDragOutlier = minHighDragOutlier;
		this.maxHighDragOutlier = maxHighDragOutlier;
	}
	
	protected boolean initialise() {
		NumVariable yVar = getNumVariable();
		int n = yVar.noOfValues();
		NumValue lowOutlierVal = (NumValue)yVar.valueAt(n - 2);
		double lowOutlier = lowOutlierVal.toDouble();
		lowOutlierVal.setValue(Double.NaN);
		
		NumValue highOutlierVal = (NumValue)yVar.valueAt(n - 1);
		double highOutlier = highOutlierVal.toDouble();
		highOutlierVal.setValue(Double.NaN);
		
		boolean init = super.initialise();
		
		lowOutlierVal.setValue(lowOutlier);
		highOutlierVal.setValue(highOutlier);
		
		return init;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		NumVariable yVar = getNumVariable();
		int n = yVar.noOfValues();
		if (index >= n - 2)
			return null;
		return super.getScreenPoint(index, theVal, thePoint);
	}
	
	private void drawValue(NumValue y, int yPos, int baseline, Graphics g) {
		int width = g.getFontMetrics().stringWidth(y.toString());
		if (yPos + (width + 3) / 2 >= getSize().width)
			y.drawLeft(g, getSize().width - 2, baseline);
		else if (yPos - (width + 3) / 2 < 0)
			y.drawRight(g, 2, baseline);
		else
			y.drawCentred(g, yPos, baseline);
		
		int leftArrowTip = yPos - width / 2 - 10;
		int rightArrowTip = yPos + width / 2 + 10;
		g.fillRect(leftArrowTip + 5, baseline - 6, 4, 3);
		g.fillRect(rightArrowTip - 8, baseline - 6, 6, 3);
		
		for (int j=0 ; j<5 ; j++) {
			g.drawLine(leftArrowTip + j, baseline - 5 + j, leftArrowTip + j, baseline - 5 - j);
			g.drawLine(rightArrowTip - j, baseline - 5 + j, rightArrowTip - j, baseline - 5 - j);
		}
	}
	
	protected void paintBackground(Graphics g) {
		draw2SdBands(g);
		
		NumVariable yVar = getNumVariable();
		int n = yVar.noOfValues();
		
		int vertCrossPos = getSize().height - getCrossSize() * 2 - 3;
		
		for (int index=n-2 ; index<n ; index++) {
			NumValue y = (NumValue)yVar.valueAt(index);
			if (!Double.isNaN(y.toDouble()))
				try {
					int yPos = axis.numValToPosition(y.toDouble());
					if (index == dragIndex) {
						g.setColor(Color.yellow);
						g.fillRect(yPos - 3, vertCrossPos - 3, 7, 7);
					}
					
					g.setColor(index == dragIndex ? Color.black : Color.red);
					drawCross(g, translateToScreen(yPos, 0, null));
					
					g.setColor(index == dragIndex ? kDarkRed : Color.red);
					
					drawValue(y, yPos, vertCrossPos - getCrossSize() - 2, g);
				} catch (AxisException e) {
					int arrowCentre = getSize().height - 10;
					int width = g.getFontMetrics().stringWidth(y.toString());
					g.setColor(Color.red);
					if (e.axisProblem == AxisException.TOO_HIGH_ERROR) {
						g.fillRect(getSize().width - 7 - 2*3 - width, arrowCentre - 1, width + 2*3, 3);
						for (int j=0 ; j<6 ; j++)
							g.drawLine(getSize().width - 2 - j, arrowCentre - j, getSize().width - 2 - j, arrowCentre + j);
						
						y.drawLeft(g, getSize().width - 7 - 2, arrowCentre - 3);
					}
					else if (e.axisProblem == AxisException.TOO_LOW_ERROR) {
						g.fillRect(2, arrowCentre - 1, width + 2*3, 3);
						for (int j=0 ; j<6 ; j++)
							g.drawLine(1 + j, arrowCentre - j, 1 + j, arrowCentre + j);
						
						y.drawRight(g, 7 + 2, arrowCentre - 3);
					}
				}
		}
		
		g.setColor(getForeground());
	}
	
//	protected void fiddleColor(Graphics g, int index) {
//		NumVariable yVar = getNumVariable();
//		int n = yVar.noOfValues();
//		if (index >= n - 2)
//			g.setColor(index == dragIndex ? Color.black : Color.red);
//	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		NumVariable yVar = getNumVariable();
		int n = yVar.noOfValues();
		
		Point hitPos = translateFromScreen(x, y, null);
		
		double yLow = yVar.doubleValueAt(n - 2);
		if (!Double.isNaN(yLow))
			try {
				int yPos = axis.numValToPosition(yLow);
				if (Math.abs(yPos - hitPos.x) < kHitSlop && Math.abs(hitPos.y) < kHitSlop)
					return new HorizDragPosInfo(x, n - 2, hitPos.x - yPos);
			} catch (AxisException e) {
			}
		
		double yHigh = yVar.doubleValueAt(n - 1);
		if (!Double.isNaN(yHigh))
			try {
				int yPos = axis.numValToPosition(yHigh);
				if (Math.abs(yPos - hitPos.x) < kHitSlop && Math.abs(hitPos.y) < kHitSlop)
					return new HorizDragPosInfo(x, n - 1, hitPos.x - yPos);
			} catch (AxisException e) {
			}
		
		return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		return new HorizDragPosInfo(hitPos.x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		hitOffset = dragPos.hitOffset;
		
		dragIndex = dragPos.index;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			int newYPos = dragPos.x + hitOffset;
			
			double newY;
			try {
				newY = axis.positionToNumVal(newYPos);
			} catch (AxisException e) {
				if (e.axisProblem == AxisException.TOO_HIGH_ERROR)
					newY = axis.maxOnAxis;
				else
					newY = axis.minOnAxis;
			}
			
			NumVariable yVar = getNumVariable();
			int n = yVar.noOfValues();
			
			if (dragIndex == n - 2)
				newY = Math.min(maxLowDragOutlier, Math.max(minLowDragOutlier, newY));
			else
				newY = Math.min(maxHighDragOutlier, Math.max(minHighDragOutlier, newY));
			
			((NumValue)yVar.valueAt(dragIndex)).setValue(newY);
			
			repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		dragIndex = -1;
		repaint();
	}
}