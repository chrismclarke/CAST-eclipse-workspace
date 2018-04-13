package boxPlot;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import random.*;
import formula.MText;

import boxPlotProg.*;


public class DragDistnShapeView extends DataView implements BoxPlotConstants {
	static final public int SYMMETRIC = 0;
	static final public int SKEW_RIGHT = 1;
	static final public int SKEW_LEFT = 2;
	static final public int LONG_TAILS = 3;
	
	static final public int HISTOGRAM = 0;
	static final public int DOT_PLOT = 1;
	
	static final private int kBoxVertBorder = 15;
	static final private int kTopHistoBorder = 20;
	static final private int kBoxHistoGap = 20;
	static final private int kBoxHeight = 22;
	
	static final private int kMaxJitter = 20;
	static final private int kJitterBottomMargin = 20;
	static final private int kTopTextBorder = 12;
	static final private int kTextRowGap = 20;
	
	static private final int kQuartileHitSlop = 4;
	
	static final private Color kQuantileColor = new Color(0xD3D3D3);
	static final private Color kDotSeparatorColor = new Color(0xE8E8E8);
	static final private Color kArrowColor = new Color(0x0033CC);
//	static final private Color kTextColor = new Color(0x6699CC);
	static final private Color kTextColor = dimColor(kArrowColor, 0.5);
	
	private HorizAxis axis;
	
	private int displayType = HISTOGRAM;
	private int quarterSampleSize;
	
	protected boolean initialisedBox = false;
	protected boolean initialisedDotPlot = false;
	protected BoxInfo boxInfo;
	
//	private boolean showOutliers = true;
	private Color boxFillColor = null;
	private Color otherBackground = null;
	
	private int selectedQuartile = NO_SELECTED_QUART;
	private int hitOffset;
	
	public DragDistnShapeView(DataSet theData, XApplet applet, HorizAxis axis) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.axis = axis;
		Font bigFont = applet.getBigFont();
//		setFont(bigFont);
		setFont(new Font(bigFont.getName(), Font.BOLD, bigFont.getSize() * 4 / 3));
	}
	
	public void setBoxColor(Color c) {
		setForeground(c);
		boxFillColor = dimColor(c, 0.9);
	}
	
	public void setBackground(Color otherBackground, Color boxBackground) {
		this.otherBackground = otherBackground;
		setBackground(boxBackground);
	}
	
	public void setDisplayType(int displayType, int quarterSampleSize) {
		this.displayType = displayType;
		this.quarterSampleSize = quarterSampleSize;
		if (displayType == DOT_PLOT)
			initialisedDotPlot = false;
		repaint();
	}
	
	protected int getAxisPos(double x) {
		int horizPos = 0;
		try {
			horizPos = axis.numValToPosition(x);
		} catch (AxisException ex) {
			if (ex.axisProblem == AxisException.TOO_HIGH_ERROR)
				horizPos = axis.getAxisLength();
		}
		return horizPos;
	}

//-----------------------------------------------------------------------------------
	
	public void setupBox(int specialDistn) {
		double axisRange = axis.maxOnAxis - axis.minOnAxis;
		int minPos = getAxisPos(axis.minOnAxis + axisRange * 0.05);
		int maxPos = getAxisPos(axis.minOnAxis + axisRange * 0.95);
		int centre = (minPos + maxPos) / 2;
		int halfLen = (maxPos - minPos) / 2;
		minPos = centre - halfLen;
		maxPos = centre + halfLen;
		boxInfo.boxPos[LOW_EXT] = minPos;
		boxInfo.boxPos[HIGH_EXT] = maxPos;
		switch (specialDistn) {
			case SYMMETRIC:
			case LONG_TAILS:
				boxInfo.boxPos[MEDIAN] = centre;
				int innerLen = (specialDistn == SYMMETRIC) ? (halfLen * 2 / 5)
																			: (innerLen = halfLen / 8);
				boxInfo.boxPos[LOW_QUART] = centre - innerLen;
				boxInfo.boxPos[HIGH_QUART] = centre + innerLen;
				break;
			case SKEW_RIGHT:
			case SKEW_LEFT:
				int len1 = halfLen / 10;
				int len2 = halfLen / 6;
				int len3 = halfLen / 4;
				if (specialDistn == SKEW_RIGHT) {
					boxInfo.boxPos[LOW_QUART] = minPos + len1;
					boxInfo.boxPos[MEDIAN] = boxInfo.boxPos[LOW_QUART] + len2;
					boxInfo.boxPos[HIGH_QUART] = boxInfo.boxPos[MEDIAN] + len3;
				}
				else {
					boxInfo.boxPos[HIGH_QUART] = maxPos - len1;
					boxInfo.boxPos[MEDIAN] = boxInfo.boxPos[HIGH_QUART] - len2;
					boxInfo.boxPos[LOW_QUART] = boxInfo.boxPos[MEDIAN] - len3;
				}
				break;
			default:
		}
	}
	
	protected int getBoxBottom() {
		return kBoxVertBorder;
	}
	
	protected void initialiseBox() {
		initialisedBox = true;
		boxInfo = new BoxInfo();
		boxInfo.setBoxHeight(kBoxHeight);
		if (boxFillColor != null)
			boxInfo.setFillColor(boxFillColor);
		boxInfo.boxBottom = getBoxBottom();
		boxInfo.vertMidLine = boxInfo.boxBottom + boxInfo.getBoxHeight() / 2;
		setupBox(SYMMETRIC);
	}
	
	protected void drawBoxPlot(Graphics g, BoxInfo boxInfo) {
		g.setColor(getForeground());
		
		boxInfo.drawBoxPlot(g, this, null, axis);
	}

//-----------------------------------------------------------------------------------
	
	private double crossPropn[][] = new double[4][];
	private double jitter[][] = new double[4][];
	private double minPropn[] = new double[4];
	private double maxPropn[] = new double[4];
//	private double lowerQuartileGap, medianGap, upperQuartileGap;
	
	public void initialiseDotPlot() {
		initialisedDotPlot = true;
		RandomRectangular random = new RandomRectangular(quarterSampleSize, 0.0, 1.0);
		random.setNeatening(0.25);
		for (int i=0 ; i<4 ; i++) {
			crossPropn[i] = random.generate();
			jitter[i] = random.generate();
			
			minPropn[i] = 1.0;
			maxPropn[i] = 0.0;
			for (int j=0 ; j<crossPropn[i].length ; j++) {
				double p = crossPropn[i][j];
				minPropn[i] = Math.min(minPropn[i], p);
				maxPropn[i] = Math.max(maxPropn[i], p);
			}
		}
//		lowerQuartileGap = 1 - Math.pow(random.generateOne(), 1.0 / quarterSampleSize);
//		medianGap = 1 - Math.pow(random.generateOne(), 1.0 / quarterSampleSize);
//		upperQuartileGap = 1 - Math.pow(random.generateOne(), 1.0 / quarterSampleSize);
	}
	
	private void shadeDotBackground(Graphics g) {
		Point p = null;
		int dotBottom = getHistoBottom();
		if (otherBackground != null) {
			g.setColor(otherBackground);
			p = translateToScreen(0, dotBottom, p);
			g.fillRect(0, 0, getSize().width, p.y);
		}
			
		g.setColor(kDotSeparatorColor);
		p = translateToScreen(0, dotBottom, p);
		g.drawLine(0, p.y, getSize().width, p.y);
	}
	
	private int translateToPix(double propn, int index) {
		int lowQuantilePos = boxInfo.boxPos[index];
		int highQuantilePos = boxInfo.boxPos[index + 1];
		return (int)Math.round(propn * (highQuantilePos - lowQuantilePos));
	}
	
	private void drawDotPlot(Graphics g) {
		if (!initialisedDotPlot)
			initialiseDotPlot();
			
		g.setColor(kDotSeparatorColor);
		Point p = null;
		int dotBottom = getHistoBottom();
		p = translateToScreen(0, dotBottom, p);
		g.drawLine(0, p.y, getSize().width, p.y);
		
		g.setColor(Color.black);
		dotBottom += kJitterBottomMargin;
		
		for (int i=0 ; i<4 ; i++) {
			double minP = minPropn[i];
			double maxP = maxPropn[i];
			
			int lowPos = boxInfo.boxPos[i];
			if (i > 0) {
				int lowSpace = translateToPix(minPropn[i], i);
				int previousSpace = translateToPix(1 - maxPropn[i - 1], i - 1);
				lowPos += Math.min(lowSpace, previousSpace);
			}
			
			int highPos = boxInfo.boxPos[i + 1];
			if (i < 3) {
				int highSpace = translateToPix(1 - maxPropn[i], i);
				int nextSpace = translateToPix(minPropn[i + 1], i + 1);
				highPos -= Math.min(highSpace, nextSpace);
			}
			
			for (int j=0 ; j<crossPropn[i].length ; j++) {
				double propn = (crossPropn[i][j] - minP) / (maxP - minP);
				
				int horizPos = lowPos + (int)Math.round(propn * (highPos - lowPos));
				int vertPos = dotBottom + (int)Math.round(jitter[i][j] * kMaxJitter);
				p = translateToScreen(horizPos, vertPos, p);
				drawCross(g, p);
			}
		}
		
		FontMetrics fm = g.getFontMetrics();
		String quarterValuesString = MText.expandText("#quarter# ") + getApplet().translate("values");
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int stringWidth = fm.stringWidth(quarterValuesString);
		int borderOffset = getViewBorder().left;
		for (int i=0 ; i<4 ; i++) {
			int baseline = kTopTextBorder + ascent;
			if (i == 1 || i == 2)
				baseline += (ascent + descent + kTextRowGap) * i;
			
			int lowPos = borderOffset + boxInfo.boxPos[i];
			int highPos = borderOffset + boxInfo.boxPos[i + 1];
			int textLeft = (lowPos + highPos - stringWidth) / 2;
			textLeft = Math.max(2, Math.min(getSize().width - stringWidth - 2, textLeft));
			
			g.setColor(kTextColor);
			g.drawString(quarterValuesString, textLeft, baseline);
			
			g.setColor(kArrowColor);
			int arrowVert = baseline + descent + 3;
			if (highPos - lowPos > 0)
				g.drawLine(lowPos + 1, arrowVert, highPos - 1, arrowVert);
			if (highPos - lowPos > 2) {
				g.drawLine(lowPos + 2, arrowVert - 1, highPos - 2, arrowVert - 1);
				g.drawLine(lowPos + 2, arrowVert + 1, highPos - 2, arrowVert + 1);
			}
			if (highPos - lowPos > 8) {
				g.drawLine(lowPos + 1, arrowVert, lowPos + 4, arrowVert + 3);
				g.drawLine(lowPos + 1, arrowVert, lowPos + 4, arrowVert - 3);
				g.drawLine(highPos - 1, arrowVert, highPos - 4, arrowVert + 3);
				g.drawLine(highPos - 1, arrowVert, highPos - 4, arrowVert - 3);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	private int getHistoBottom() {
		return boxInfo.getBoxHeight() + 2 * kBoxVertBorder + kBoxHistoGap;
	}
	
	private void shadeHistoBackground(Graphics g) {
		if (otherBackground != null) {
			g.setColor(otherBackground);
			Point p = translateToScreen(0, getHistoBottom(), null);
			g.fillRect(0, 0, getSize().width, p.y);
		}
	}
	
	protected void drawHistogram(Graphics g) {
		int histoBottom = getHistoBottom();
		
		int rectArea = (boxInfo.boxPos[HIGH_EXT] - boxInfo.boxPos[LOW_EXT])
												* (getSize().height - histoBottom - kTopHistoBorder) / 20;
																//		ht = 1/5 if all boxes are same width
		Point p0 = null;
		Point p1 = null;
		for (int i=LOW_EXT ; i<HIGH_EXT ; i++) {
			int width = boxInfo.boxPos[i+1] - boxInfo.boxPos[i];
			p0 = translateToScreen(boxInfo.boxPos[i], histoBottom, p0);
			if (width == 0) {
				g.setColor(Color.black);
				g.drawLine(p0.x, 0, p0.x, p0.y);
			}
			else {
				int ht = rectArea / width;
				p1 = translateToScreen(boxInfo.boxPos[i+1], histoBottom + ht, p1);
				
				g.setColor(Color.lightGray);
				g.fillRect(p0.x, p1.y, (p1.x - p0.x), (p0.y - p1.y));
				g.setColor(Color.black);
				g.drawRect(p0.x, p1.y, (p1.x - p0.x), (p0.y - p1.y));
			}
		}
		
		p0 = translateToScreen(0, histoBottom, p0);
		g.drawLine(0, p0.y, getSize().width, p0.y);
	}

//-----------------------------------------------------------------------------------
	
	private void shadeBackground(Graphics g) {
		Color oldColor = g.getColor();
		if (displayType == DOT_PLOT)
			shadeDotBackground(g);
		else
			shadeHistoBackground(g);

		g.setColor(Color.yellow);
		Point p = null;
		if (selectedQuartile != NO_SELECTED_QUART) {
			p = translateToScreen(boxInfo.boxPos[selectedQuartile], 0, p);
			g.fillRect(p.x - 2, 0, 5, getSize().height);
		}
		
		g.setColor(kQuantileColor);
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++) {
			p = translateToScreen(boxInfo.boxPos[i], 0, p);
			g.drawLine(p.x, 0, p.x, getSize().height);
		}
		
		g.setColor(oldColor);
	}
	
	public void paintView(Graphics g) {
		if (!initialisedBox)
			initialiseBox();
		
		shadeBackground(g);
		
		drawBoxPlot(g, boxInfo);
		
		if (displayType == DOT_PLOT)
			drawDotPlot(g);
		else
			drawHistogram(g);
	}
	

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y >= getSize().height)
			return null;
		int hitIndex = NO_SELECTED_QUART;
		int hitOffset = kQuartileHitSlop;
		int absOffset = kQuartileHitSlop;
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++) {
			int thisHitOffset = hitPos.x - boxInfo.boxPos[i];
			int thisAbsOffset = Math.abs(thisHitOffset);
			if (thisAbsOffset < absOffset) {
				hitIndex = i;
				hitOffset = thisHitOffset;
				absOffset = thisAbsOffset;
			}
		}
		if (hitIndex == NO_SELECTED_QUART)
			return null;
		else
			return new HorizDragPosInfo(hitPos.x, hitIndex, hitOffset);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y >= getSize().height || hitPos.x - hitOffset < 0
														|| hitPos.x - hitOffset >= axis.getAxisLength())
			return null;
		else
			return new HorizDragPosInfo(hitPos.x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null) {
			if (selectedQuartile != NO_SELECTED_QUART) {
				selectedQuartile = NO_SELECTED_QUART;
				repaint();
			}
			return false;
		}
		else {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
			hitOffset = dragPos.hitOffset;
			selectedQuartile = dragPos.index;
			repaint();
			notifyStartDrag();
			return true;
		}
	}
	
	private void setSelectedQuartile(int newPos) {
		if (newPos < 0 || newPos >= axis.getAxisLength())
			return;
		int newRank = 0;
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++)
			if (i != selectedQuartile && newPos > boxInfo.boxPos[i])
				newRank++;
		if (newRank > selectedQuartile) {
			for (int i=selectedQuartile ; i<newRank ; i++)
				boxInfo.boxPos[i] = boxInfo.boxPos[i+1];
			selectedQuartile = newRank;
		}
		else if (newRank < selectedQuartile) {
			for (int i=selectedQuartile ; i>newRank ; i--)
				boxInfo.boxPos[i] = boxInfo.boxPos[i-1];
			selectedQuartile = newRank;
		}
		
		boxInfo.boxPos[selectedQuartile] = newPos;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			setSelectedQuartile(dragPos.x - hitOffset);
			repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
	
	protected void notifyStartDrag() {
		DragBoxHistoApplet applet = (DragBoxHistoApplet)getApplet();
		applet.notifyStartDrag();
	}
	

//-----------------------------------------------------------------------------------
	
	public void mousePressed(MouseEvent e) {
		requestFocus();
		super.mousePressed(e);
	}
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_LEFT && selectedQuartile != NO_SELECTED_QUART) {
			setSelectedQuartile(boxInfo.boxPos[selectedQuartile] - 1);
			repaint();
		}
		else if (key == KeyEvent.VK_RIGHT && selectedQuartile != NO_SELECTED_QUART) {
			setSelectedQuartile(boxInfo.boxPos[selectedQuartile] + 1);
			repaint();
		}
	}
	
	public void focusLost(FocusEvent e) {
		if (selectedQuartile != NO_SELECTED_QUART) {
			selectedQuartile = NO_SELECTED_QUART;
			repaint();
		}
	}
}
	
