package dynamic;

import java.awt.*;

import dataView.*;
import axis.*;


public class HistogramView extends DataView {
	
	static final protected Color kCorrectFillColor = new Color(0xDDDDDD);
	static final private Color kWrongFillColor = new Color(0xFFBBBB);
	static final private Color kDarkRed = new Color(0x990000);
	
	static final private int kFinalFrame = 20;
	static final private int kFramesPerSec = 10;
	
	static final private int kTopBorder = 5;
	static final private int kRightBorder = 10;
	static final private int kKeySquareSize = 30;
	static final private int kKeyGap = 6;
	
	static final private double kEps = 0.00001;
	
	private HorizAxis xAxis;
	private VertAxis freqAxis;
	private String yKey;
	private double unitClassWidth;
	protected double classBoundary[];
	private boolean groupWithNext[];
	
	private boolean correctHeights = false;
	
	public HistogramView(DataSet theData, XApplet applet,
													String yKey, HorizAxis xAxis, VertAxis freqAxis, double unitClassWidth,
													double[] classBoundary, boolean[] groupWithNext) {
		super(theData, applet, new Insets(0,0,0,0));
		this.yKey = yKey;
		this.xAxis = xAxis;
		this.freqAxis = freqAxis;
		this.unitClassWidth = unitClassWidth;
		this.classBoundary = classBoundary;
		this.groupWithNext = groupWithNext;
		
		setFont(applet.getBigBoldFont());
		
		int nVals = ((NumVariable)getVariable(yKey)).noOfValues();
		if (nVals != (classBoundary.length - 1) || nVals != groupWithNext.length)
			throw new RuntimeException("Wrong number of classes " + nVals + ", " + classBoundary.length + ", " + groupWithNext.length);
	}
	
	public void setCorrectHeights(boolean correctHeights) {
		this.correctHeights = correctHeights;
	}
	
	public void animateGrouping(boolean showGrouped) {
		animateFrames(showGrouped ? 1 : kFinalFrame-1, showGrouped ? kFinalFrame-1 : 1-kFinalFrame,
																																							kFramesPerSec, null);
	}
	
	protected Point getScreenPoint(double x, double y, Point thePoint) {
		int vertPos = freqAxis.numValToRawPosition(y);
		int horizPos = xAxis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	protected void fillOneBar(Graphics g, double y, double lowBoundary, double highBoundary,
																							Color fillColor, Point p0, Point p1) {
		p0 = getScreenPoint(lowBoundary, y, p0);
		p1 = getScreenPoint(highBoundary, 0.0, p1);
		g.setColor(fillColor);
		g.fillRect(p0.x, p0.y, (p1.x - p0.x) + 1, (p1.y - p0.y) + 1);
	}
	
	protected void outlineOneBar(Graphics g, double y, double lowBoundary, double highBoundary,
																												Color lineColor,  Point p0, Point p1) {
		p0 = getScreenPoint(lowBoundary, y, p0);
		p1 = getScreenPoint(highBoundary, 0.0, p1);
		g.setColor(lineColor);
		g.drawLine(p0.x, p0.y, p0.x, p1.y);
		g.drawLine(p0.x, p0.y, p1.x, p0.y);
		g.drawLine(p1.x, p0.y, p1.x, p1.y);
	}
	
	protected void drawOneBar(Graphics g, NumVariable yVar, int index, boolean needsScaling,
																															double scaling, Point p0, Point p1) {
		double y = yVar.doubleValueAt(index);
		if (correctHeights && needsScaling)
			y *= scaling;
		Color fillColor = needsScaling && !correctHeights ? kWrongFillColor : kCorrectFillColor;
		fillOneBar(g, y, classBoundary[index], classBoundary[index + 1], fillColor, p0, p1);
		outlineOneBar(g, y, classBoundary[index], classBoundary[index + 1], getForeground(), p0, p1);
	}
	
	private void drawAllUnitBars(Graphics g, NumVariable yVar, Point p0, Point p1) {
		int nCoreBars = yVar.noOfValues();
		
		boolean accumulated = false;
		for (int i=0 ; i<nCoreBars ; i++) {
			if (!accumulated && !groupWithNext[i]) {
				double scaling = unitClassWidth / (classBoundary[i + 1] - classBoundary[i]);
				boolean needsScaling = (scaling < 1.0 - kEps || scaling > 1.0 + kEps);
				drawOneBar(g, yVar, i, needsScaling, scaling, p0, p1);
			}
			accumulated = groupWithNext[i];
		}
	}
	
	private void drawOneGroupedBar(Graphics g, NumVariable yVar, int lowIndex, int highIndex,
																																						Point p0, Point p1) {
		double groupY = 0.0;
		for (int i=lowIndex ; i<=highIndex ; i++)
			groupY += yVar.doubleValueAt(i);
		
		double propn = getCurrentFrame() / (double)kFinalFrame;
		Color fillColor = correctHeights ? kCorrectFillColor
																			: mixColors(kWrongFillColor, kCorrectFillColor, propn);
		Color drawColor = mixColors(fillColor, getForeground(), 2.0 * Math.min(propn, 1 - propn));
		
		double lowBoundary = classBoundary[lowIndex];
		double highBoundary = classBoundary[highIndex + 1];
		if (correctHeights)
			groupY = groupY * unitClassWidth / (highBoundary - lowBoundary);
		for (int i=lowIndex ; i<=highIndex ; i++) {
			double drawY = groupY * propn + yVar.doubleValueAt(i) * (1 - propn);
			fillOneBar(g, drawY, classBoundary[i], classBoundary[i + 1], fillColor, p0, p1);
		}
		
		if (propn < 0.5) {
			for (int i=lowIndex ; i<=highIndex ; i++) {
				double drawY = groupY * propn + yVar.doubleValueAt(i) * (1 - propn);
				outlineOneBar(g, drawY, classBoundary[i], classBoundary[i + 1], drawColor, p0, p1);
			}
		}
		else
			outlineOneBar(g, groupY, lowBoundary, highBoundary, drawColor, p0, p1);
	}
	
	private void drawGroupedBars(Graphics g, NumVariable yVar, Point p0, Point p1) {
		int nCoreBars = yVar.noOfValues();
		
		boolean accumulated = false;
		int lowIndex = 0;
		for (int i=0 ; i<nCoreBars ; i++) {
			if (accumulated) {
				if (!groupWithNext[i]) {
					drawOneGroupedBar(g, yVar, lowIndex, i, p0, p1);
					accumulated = false;
				}
			}
			else {
				lowIndex = i;
				accumulated = groupWithNext[i];
			}
		}
	}
	
	protected void drawCoreKey(Graphics g, LabelValue text, int keySize, Color fillColor, 
																											Color outlineColor, int vertOffset) {
		g.setColor(outlineColor);
		
		int textWidth = text.stringWidth(g);
		int ascent = g.getFontMetrics().getAscent();
		int baseline = kTopBorder + Math.max((keySize + ascent) / 2, ascent) + vertOffset;
		int textLeft = getSize().width - textWidth - kRightBorder;
		text.drawRight(g, textLeft, baseline);
		
		int rectLeft = textLeft - kKeyGap - keySize;
		int rectTop = baseline - (ascent + keySize) / 2;
		g.setColor(Color.black);
		g.drawRect(rectLeft, rectTop, keySize, keySize);
		g.setColor(fillColor);
		g.fillRect(rectLeft + 1, rectTop + 1, keySize - 1, keySize - 1);
	}
	
	protected void drawKey(Graphics g) {
		if (!correctHeights) {
			LabelValue kMisleadingText = new LabelValue(getApplet().translate("Misleading"));
			drawCoreKey(g, kMisleadingText, kKeySquareSize, kWrongFillColor, kDarkRed, 0);
		}
	}
	
	public void paintView(Graphics g) {
		drawKey(g);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		Point p0 = new Point(0,0);
		Point p1 = new Point(0,0);
		drawAllUnitBars(g, yVar, p0, p1);
		drawGroupedBars(g, yVar, p0, p1);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
	
