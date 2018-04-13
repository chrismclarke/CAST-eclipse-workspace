package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class DragSplitDotPlotView extends DotPlotView {
//	static final public String SPLIT_DOTPLOT = "splitDotPlot";
	
	private static final int kMinHitDist = 5;
	private static final int kMinDragBorder = 50;		//	cannot drag boundary within 50 pixels of edge
	private static final int kHiliteWidth = 2;
	private static final int kLabelGap = 2;
	
//	private static final int kMaxVertJitter = 30;
	
	private double boundary, minBoundary, maxBoundary;
	private LabelValue lowLabel, highLabel;
	
//	private CatVariable groupingVariable = null;
//	private VertAxis groupAxis = null;
	
	private int hitOffset;
	private boolean selected = false;
	private Point p = new Point(0,0);
	private NumValue meanVal; 
	
	public DragSplitDotPlotView(DataSet theData, XApplet applet, NumCatAxis numAxis, double boundary,
								double minBoundary, double maxBoundary, LabelValue lowLabel, LabelValue highLabel,
								int meanDecimals) {
		super(theData, applet, numAxis, 1.0);
		this.boundary = boundary;
		this.minBoundary = minBoundary;
		this.maxBoundary = maxBoundary;
		this.lowLabel = lowLabel;
		this.highLabel = highLabel;
		meanVal = new NumValue(0.0, meanDecimals);
	}
	
	protected int groupIndex(int itemIndex) {
		NumValue y = (NumValue)getNumVariable().valueAt(itemIndex);
		return (y.toDouble() > boundary) ? 1 : 0;
	}
	
	private void drawBackground(Graphics g) {
		int boundaryPos = axis.numValToRawPosition(boundary);
		int bx = translateToScreen(boundaryPos, 0, p).x;
		if (selected) {
			g.setColor(Color.yellow);
			g.fillRect(bx - kHiliteWidth, 0, 2 * kHiliteWidth + 1, getSize().height);
		}
		g.setColor(selected ? Color.black : Color.blue);
		g.drawLine(bx, 0, bx, getSize().height);
		
		double lowSx = 0.0;
		int lowN = 0;
		double highSx = 0.0;
		int highN = 0;
		
		ValueEnumeration e = getNumVariable().values();
		while (e.hasMoreValues()) {
			double x = e.nextDouble();
			if (x < boundary) {
				lowN ++;
				lowSx += x;
			}
			else {
				highN ++;
				highSx += x;
			}
		}
		
		g.setColor(getCrossColor(0));
		drawMean(lowN, lowSx, kLabelGap, bx - kLabelGap, lowLabel, g);
		
		g.setColor(getCrossColor(1));
		drawMean(highN, highSx, bx + kLabelGap, getSize().width - kLabelGap, highLabel, g);
	}
	
	private void drawMean(int n, double sx, int lowPos, int highPos, Value meanLabel,
																																						Graphics g) {
		if (n > 0) {
			double mean = sx / n;
			meanVal.setValue(mean);
			int meanPos = axis.numValToRawPosition(mean);
			int mx = translateToScreen(meanPos, 0, p).x;
			
			int width = Math.max(meanLabel.stringWidth(g), meanVal.stringWidth(g));
			int baseline1 = g.getFontMetrics().getAscent() + kLabelGap;
			int baseline2 = 2 * baseline1 + g.getFontMetrics().getDescent();
			
			int valCentre = Math.max(mx, lowPos + width / 2);
			valCentre = Math.min(valCentre, highPos - width / 2);
			valCentre = Math.max(valCentre, width / 2);
			valCentre = Math.min(valCentre, getSize().width - width / 2);
			
			meanLabel.drawCentred(g, valCentre, baseline1);
			meanVal.drawCentred(g, valCentre, baseline2);
			
			int topOfJitter = translateToScreen(0, currentJitter + 4, p).y;
			g.drawLine(mx, baseline2 + kLabelGap, mx, topOfJitter);
			g.drawLine(mx, topOfJitter, mx - 4, topOfJitter - 4);
			g.drawLine(mx, topOfJitter, mx + 4, topOfJitter - 4);
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int bx = translateToScreen(axis.numValToRawPosition(boundary), 0, p).x;
		if (Math.abs(bx - x) <= kMinHitDist)
			return new HorizDragPosInfo(x, 0, x - bx);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < kMinDragBorder || y < 0 || x >= getSize().width - kMinDragBorder || y >= getSize().height)
			return null;
		else
			return new HorizDragPosInfo(x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		hitOffset = dragPos.hitOffset;
		selected = true;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selected = false;
			repaint();
		}
		else {
			selected = true;
			HorizDragPosInfo newPos = (HorizDragPosInfo)toPos;
			try {
				int bx = translateFromScreen(newPos.x - hitOffset, 0, p).x;
				boundary = Math.min(maxBoundary, Math.max(minBoundary, axis.positionToNumVal(bx)));
				repaint();
			} catch (AxisException ex) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selected = false;
		repaint();
	}
}