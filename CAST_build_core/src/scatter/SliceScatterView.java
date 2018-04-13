package scatter;

import java.awt.*;

import dataView.*;
import axis.*;


public class SliceScatterView extends DataView {
	static final private Color kMidGray = new Color(0x999999);
	static final protected Color kPaleGray = new Color(0xCCCCCC);
	static final protected Color kArrowBackground = new Color(0xFFF2EF);
	static final protected Color kPaleArrow = new Color(0xFFB6B0);
	static final protected Color kSelectedArrow = Color.red;
	
	static final private int kArrowBorder = 2;
	static final private int kArrowHeadSize = 7;
	static final private int kArrowBodySize = 3;
	static final private int kArrowRight = kArrowBorder + 2 * kArrowHeadSize;
	static final private int kBodyRight = kArrowBorder + kArrowHeadSize + kArrowBodySize;
	static final private int kBodyLeft = kArrowBorder + kArrowHeadSize - kArrowBodySize;
	
	static final private int rangeArrowX[] = {kArrowBorder + kArrowHeadSize,
														kArrowRight,
														kBodyRight,
														kBodyRight,
														kArrowRight,
														kArrowBorder + kArrowHeadSize,
														kArrowBorder,
														kBodyLeft,
														kBodyLeft,
														kArrowBorder,
														kArrowBorder + kArrowHeadSize};
	private int rangeArrowY[] = new int[11];
	
	protected HorizAxis xAxis;
	protected VertAxis yAxis;
	
	private int leftBorder, rightBorder;
	
	protected double minSelect, selectRange;
	private String xKey, yKey;
	
	protected boolean selected = false;
	
	public SliceScatterView(DataSet theData, XApplet applet,
									HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey,
									double selectRange, int leftBorder, int rightBorder) {
		super(theData, applet, new Insets(5, leftBorder, 5, rightBorder));
		this.selectRange = selectRange;
		this.xKey = xKey;
		this.yKey = yKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.leftBorder = leftBorder;
		this.rightBorder = rightBorder;
	}
	
	public SliceScatterView(DataSet theData, XApplet applet,
									HorizAxis xAxis, VertAxis yAxis, String xKey,
									String yKey, double selectRange) {
		this(theData, applet, xAxis, yAxis, xKey, yKey, selectRange, kArrowRight + kArrowBorder + 5, 5);
	}
	
	protected Point getScreenPoint(double yVal, double xVal, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal);
			int horizPos = xAxis.numValToPosition(xVal);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected void drawScatterBorders(Graphics g, NumVariable yVariable, NumVariable xVariable) {
		g.setColor(kArrowBackground);
		g.fillRect(0, 0, leftBorder, getSize().height);
		
		ValueEnumeration ye = yVariable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		double sy = 0.0;
//		double syy = 0.0;
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		int n = 0;
		while (ye.hasMoreValues()) {
			double nextY = ye.nextDouble();
			boolean nextSel = fe.nextFlag();
			if (!selected || nextSel) {
				sy += nextY;
//				syy += nextY * nextY;
				if (nextY < min)
					min = nextY;
				if (nextY > max)
					max = nextY;
				n ++;
			}
		}
		
		if (n > 0) {
			double mean = sy / n;
			int meanY = yAxis.numValToRawPosition(mean);
//			double twoSD = 0.0;
//			if (n > 1)
//				twoSD = 2 * Math.sqrt((syy - sy * mean) / (n - 1));
//			int minY = Math.min(yAxis.numValToRawPosition(mean - twoSD), meanY - kArrowHeadSize - 2);
//			int maxY = Math.max(yAxis.numValToRawPosition(mean + twoSD), meanY + kArrowHeadSize + 2);
			int minY = Math.min(yAxis.numValToRawPosition(min), meanY - kArrowHeadSize - 2);
			int maxY = Math.max(yAxis.numValToRawPosition(max), meanY + kArrowHeadSize + 2);
			Point minPos = translateToScreen(0, minY, null);
			Point maxPos = translateToScreen(0, maxY, null);
			
			g.setColor(selected ? kSelectedArrow : kPaleArrow);
			
			rangeArrowY[0] = rangeArrowY[10] = maxPos.y;
			rangeArrowY[1] = rangeArrowY[2] = rangeArrowY[8] = rangeArrowY[9] = maxPos.y + kArrowHeadSize;
			rangeArrowY[3] = rangeArrowY[4] = rangeArrowY[6] = rangeArrowY[7] = minPos.y - kArrowHeadSize;
			rangeArrowY[5] = minPos.y;

			g.fillPolygon(rangeArrowX, rangeArrowY, 11);
			g.drawPolygon(rangeArrowX, rangeArrowY, 11);
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		
		if (selected) {
			g.setColor(kPaleGray);
			try {
				int x1Pos = xAxis.numValToPosition(minSelect);
				int x2Pos = xAxis.numValToPosition(minSelect + selectRange);
				Point topLow = translateToScreen(x1Pos, 0, null);
				Point topHigh = translateToScreen(x2Pos, 0, null);
				g.fillRect(leftBorder - 1, 0, topLow.x - leftBorder + 1, getSize().height);
				g.fillRect(topHigh.x, 0, getSize().width - topHigh.x - rightBorder, getSize().height);
			} catch (AxisException ex) {
			}
		}
		
		g.setColor(getForeground());
		
		ValueEnumeration ye = yVariable.values();
		ValueEnumeration xe = xVariable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		Point thePoint = null;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double nextY = ye.nextDouble();
			double nextX = xe.nextDouble();
			boolean nextSel = fe.nextFlag();
			thePoint = getScreenPoint(nextY, nextX, thePoint);
			if (thePoint != null) {
				if (selected)
					g.setColor(nextSel ? Color.red : kMidGray);
				drawCross(g, thePoint);
			}
		}
		
		drawScatterBorders(g, yVariable, xVariable);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean canDrag() {
		return true;
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		try {
			int minXPos = xAxis.numValToPosition(xAxis.minOnAxis + selectRange * 0.5);
			int maxXPos = xAxis.numValToPosition(xAxis.maxOnAxis - selectRange * 0.5);
			
			Point hitPos = translateFromScreen(x, y, null);
			hitPos.x = Math.max(minXPos, Math.min(maxXPos, hitPos.x));
			return new HorizDragPosInfo(Math.max(minXPos, Math.min(maxXPos, hitPos.x)));
		} catch (Exception e) {
		}
		return null;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		doDrag(null, startPos);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selected = false;
			repaint();
		}
		else {
			selected = true;
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			boolean selectionChanged = false;
			try {
				minSelect = xAxis.positionToNumVal(dragPos.x) - selectRange * 0.5;
				selectionChanged = getData().setSelection(xKey, minSelect, minSelect + selectRange);
			} catch (AxisException e) {
			}
			if (!selectionChanged)
				repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		getData().clearSelection();
		selected = false;
		repaint();
	}
}
	
