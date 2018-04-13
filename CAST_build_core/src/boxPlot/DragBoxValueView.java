package boxPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import random.*;


public class DragBoxValueView extends BoxAndDotView {
//	static public final String DRAG_BOX_VALUE_PLOT = "dragBoxValuePlot";
	
	static final private Color kPaleGray = new Color(0xCCCCCC);
	static final private Color kDotBoxSeparatorColor = new Color(0xCC9999);
	
	static final private int kArrowHead = 4;
	static final private int kArrowToText = 4;
	static final private int kTextTopBorder = 4;
	
	private boolean allowDrag = true;
	
	private int hitIndex = -1;
	private int hitOffset;
	private boolean selected = false;
	
	private Point crossPos[];
	private static final int kMinHitDist = 9;
	
	private boolean showIQR = false;
	private Font iqrFont;
	private LabelValue iqrLabel;
	
	public DragBoxValueView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
		iqrFont = applet.getBigBoldFont();
		iqrLabel = new LabelValue("1.5 " + applet.translate("IQR"));
	}
	
	public void setShowIQR(boolean showIQR) {
		this.showIQR = showIQR;
	}
	
	public void setAllowDrag(boolean allowDrag) {
		this.allowDrag = allowDrag;
	}
	
	protected void initialiseJittering() {
		super.initialiseJittering();
		NumVariable variable = getNumVariable();
		int noOfVals = variable.noOfValues();
		if (crossPos == null || crossPos.length != noOfVals) {
			if (jittering == null || jittering.length != noOfVals) {
				RandomBits generator = new RandomBits(14, noOfVals);
																						//	between 0 and 2^14 = 16384
				jittering = generator.generate();
			}
			crossPos = new Point[noOfVals];
			for (int i=0 ; i<noOfVals ; i++)
				crossPos[i] = getScreenPoint(i, (NumValue)(variable.valueAt(i)), null);
		}
	}
	
	protected void initialise(NumVariable variable) {
		super.initialise(variable);
		boxInfo.setBoxHeight(20);
	}
	
	private void drawIQRBand(Graphics g, int lowX, int highX, Point p1, Point p2) {
		g.setColor(kPaleGray);
		p1 = translateToScreen(lowX, getSize().height, p1);
		p2 = translateToScreen(highX, 0, p2);
		g.fillRect(p1.x, 0, p2.x - p1.x, getSize().height);
		
		g.setColor(Color.blue);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		int textBaseline = kTextTopBorder + ascent;
		iqrLabel.drawCentred(g, (p1.x + p2.x) / 2, textBaseline);
		
		int lineVert = kTextTopBorder + ascent + descent + kArrowToText + kArrowHead;
		g.drawLine(p1.x, lineVert, p2.x - 1, lineVert);
		g.drawLine(p1.x, lineVert, p1.x + kArrowHead, lineVert - kArrowHead);
		g.drawLine(p1.x, lineVert, p1.x + kArrowHead, lineVert + kArrowHead);
		g.drawLine(p2.x - 1, lineVert, p2.x - 1 - kArrowHead, lineVert - kArrowHead);
		g.drawLine(p2.x - 1, lineVert, p2.x - 1 - kArrowHead, lineVert + kArrowHead);
	}
	
	protected void shadeBackground(Graphics g) {
		Color oldColor = g.getColor();
		if (showIQR) {
			double lq = boxInfo.boxVal[LOW_QUART];
			double uq = boxInfo.boxVal[HIGH_QUART];
			double lowLimit = lq - 1.5 * (uq - lq);
			double highLimit = uq + 1.5 * (uq - lq);
			int lowLimitPos = axis.numValToRawPosition(lowLimit);
			int highLimitPos = axis.numValToRawPosition(highLimit);
			int lqPos = axis.numValToRawPosition(lq);
			int uqPos = axis.numValToRawPosition(uq);
			
			Point p1 = new Point(0,0);
			Point p2 = new Point(0,0);
			Font oldFont = g.getFont();
			g.setFont(iqrFont);
			drawIQRBand(g, lowLimitPos, lqPos, p1, p2);
			drawIQRBand(g, uqPos, highLimitPos, p1, p2);
			g.setFont(oldFont);
		}
		
		g.setColor(kDotBoxSeparatorColor);
		int separatorHt = currentJitter + 2 * getViewBorder().bottom + 2;
		g.drawLine(0, getSize().height - separatorHt, getSize().width, getSize().height - separatorHt);
		
		if (selected) {
			g.setColor(Color.yellow);
			g.fillRect(crossPos[hitIndex].x - 1, getSize().height - 4 * currentJitter, 3, 4 * currentJitter);
		}
		g.setColor(oldColor);
	}
	
	public void resetValues() {
								//	used by DragValueMeanMedianView when dragged values are reset to initial positions
		NumVariable variable = getNumVariable();
		for (int i=0 ; i<crossPos.length ; i++)
			crossPos[i] = getScreenPoint(i, (NumValue)(variable.valueAt(i)), null);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return allowDrag;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<crossPos.length ; i++)
			if (crossPos[i] != null) {
				int xDist = crossPos[i].x - x;
				int yDist = crossPos[i].y - y;
				int dist = xDist*xDist + yDist*yDist;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = i;
					minDist = dist;
				}
			}
		if (gotPoint && minDist < kMinHitDist)
			return new HorizDragPosInfo(x, minIndex, x - crossPos[minIndex].x);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y >= 4 * currentJitter || hitPos.x - hitOffset < 0
														|| hitPos.x - hitOffset >= axis.getAxisLength())
			return null;
		else
			return new HorizDragPosInfo(x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		getData().clearSelection();
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		hitOffset = dragPos.hitOffset;
		hitIndex = dragPos.index;
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
			if (fromPos == null)
				selected = true;
			NumVariable variable = getNumVariable();
			NumValue hitNumValue = (NumValue)(variable.valueAt(hitIndex));
//			double oldValue = hitNumValue.toDouble();
			double newValue = 0.0;
			HorizDragPosInfo newPos = (HorizDragPosInfo)toPos;
			try {
				newValue = axis.positionToNumVal(newPos.x - getViewBorder().left);
			} catch (AxisException ex) {
				return;
			}
			hitNumValue.setValue(newValue);
			crossPos[hitIndex] = getScreenPoint(hitIndex, (NumValue)(variable.valueAt(hitIndex)), null);
			variable.reSortData();
			
			initialiseBox(variable.getSortedData(), boxInfo);
			
			getData().valueChanged(hitIndex);
			getApplet().notifyDataChange(this);
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		hitIndex = -1;
		selected = false;
		repaint();
	}
	
}
	
