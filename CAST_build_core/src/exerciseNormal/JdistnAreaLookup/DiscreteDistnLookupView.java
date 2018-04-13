package exerciseNormal.JdistnAreaLookup;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class DiscreteDistnLookupView extends CoreDistnLookupView {
//	static final public String DISCRETE_DISTN_LOOKUP = "discreteDistnLookup";
	
	static final private int kTopBorder = 50;
	static final private int kDistnTopBorder = 20;
	static final private int kProbVertGap = 4;
	static final private int kMaxBarWidth = 30;
	
//	static final private int LEFT = 0;
//	static final private int CENTER = 1;
//	static final private int RIGHT = 2;
	
	static final private Color kDimBarColor = Color.lightGray;
	static final private Color kBarColor = new Color(0x3366FF);
	static final private Color kSelectedBackgroundColor = Color.yellow;
	
	static final private LabelValue kProbLabel = new LabelValue("Probability is");
	
	private Color textColor;
	private Color barShade[] = new Color[kNoOfShades + 1];
	private Color dimBarShade[] = new Color[kNoOfShades + 1];
	
	private int xCoord[] = new int[9];
	private int yCoord[] = new int[9];
	
	public DiscreteDistnLookupView(DataSet theData, XApplet applet,
					HorizAxis horizAxis, String distnKey, boolean highAndLow) {
		super(theData, applet, horizAxis, distnKey, highAndLow, DISCRETE);
		
		textColor = darkenColor(kBarColor, 0.5);
		AccurateDistnArtist.setShades(barShade, kBarColor);
		AccurateDistnArtist.setShades(dimBarShade, kDimBarColor);
		
		setFont(applet.getStandardBoldFont());
	}
	
	protected void doInitialisation(Graphics g) {
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		int axisLength = horizAxis.getAxisLength();
		
		int minSelPos = 0;
		try {
			minSelPos = horizAxis.numValToPosition(y.getMinSelection());
		} catch (AxisException e) {
			if (e.axisProblem == AxisException.TOO_LOW_ERROR)
				minSelPos = 0;
			else
				minSelPos = axisLength - 1;
		}
		int maxSelPos = 0;
		try {
			maxSelPos = horizAxis.numValToPosition(y.getMaxSelection());
		} catch (AxisException e) {
			if (e.axisProblem == AxisException.TOO_LOW_ERROR)
				maxSelPos = 0;
			else
				maxSelPos = axisLength - 1;
		}
		
		int minEditPos = getSize().width / 4;
		int maxEditPos = getSize().width * 3 / 4;
		
		drawSelectionBackground(g, minSelPos, maxSelPos, minEditPos, maxEditPos);
		
		if (doingDrag && extremeSelected == MIN_SELECTED)
			drawOneLimit(g, minSelPos, minEditPos, null, Color.red, lowPending, true);
		else
			drawOneLimit(g, minSelPos, minEditPos, Color.red, null, lowPending, true);
		if (doingDrag && extremeSelected == MAX_SELECTED)
			drawOneLimit(g, maxSelPos, maxEditPos, null, Color.red, highPending, false);
		else
			drawOneLimit(g, maxSelPos, maxEditPos, Color.red, null, highPending, false);
				
		int nx = (int)Math.round(horizAxis.maxOnAxis);
		int barWidth = Math.max(1, Math.min(kMaxBarWidth, getSize().width / (nx * 3)));
		int minSelX = (int)Math.ceil(y.getMinSelection());
		int maxSelX = (int)Math.floor(y.getMaxSelection());
		
		drawBars(g, kDimBarColor, dimBarShade, 0, minSelX - 1, barWidth);
		drawBars(g, kBarColor, barShade, minSelX, maxSelX, barWidth);
		drawBars(g, kDimBarColor, dimBarShade, maxSelX + 1, nx, barWidth);
		
		if (doingDrag) {
			if (extremeSelected == MIN_SELECTED)
				drawOneLimit(g, minSelPos, minEditPos, Color.black, null, lowPending, true);
			else
				drawOneLimit(g, maxSelPos, maxEditPos, Color.black, null, highPending, false);
		}
		
		double centerProb = y.getCumulativeProb(y.getMaxSelection()) - y.getCumulativeProb(y.getMinSelection());
		drawProb(g, centerProb, minSelPos, maxSelPos, textColor);
	}
	
	private void drawSelectionBackground(Graphics g, int minSelPos, int maxSelPos, int minEditPos, int maxEditPos) {
		if (!lowPending && !highPending) {
			xCoord[0] = xCoord[1] = xCoord[8] = minEditPos;
			xCoord[2] = xCoord[3] = minSelPos;
			xCoord[4] = xCoord[5] = maxSelPos;
			xCoord[6] = xCoord[7] = maxEditPos;
			
			yCoord[0] = yCoord[7] = yCoord[8] = 0;
			yCoord[1] = yCoord[6] = kTopBorder / 2;
			yCoord[2] = yCoord[5] = kTopBorder;
			yCoord[3] = yCoord[4] = getSize().height;
			
			g.setColor(kSelectedBackgroundColor);
			g.fillPolygon(xCoord, yCoord, 9);
		}
	}
	
	private void drawOneLimit(Graphics g, int selPos, int editPos, Color lineColor,
																				Color lineHiliteColor, boolean isPending, boolean lowNotHigh) {
		if (isPending)
			if (lowNotHigh)
				kFinishTyping.drawRight(g, 5, 20);
			else
				kFinishTyping.drawLeft(g, getSize().width - 5, 20);
		else {
			if (lineColor != null) {
				g.setColor(lineColor);
				drawOneLine(g, selPos, editPos);
			}
			
			if (lineHiliteColor != null) {
				g.setColor(lineHiliteColor);
				drawOneLine(g, selPos - 1, editPos - 1);
				drawOneLine(g, selPos + 1, editPos + 1);
			}
		}
	}
	
	private void drawOneLine(Graphics g, int selPos, int editPos) {
		g.drawLine(editPos, 0, editPos, kTopBorder / 2);
		g.drawLine(editPos, kTopBorder / 2, selPos, kTopBorder);
		g.drawLine(selPos, kTopBorder, selPos, getSize().height);
	}
	
//	private void drawSelectionLines(Graphics g, int minSelPos, int maxSelPos, int minEditPos, int maxEditPos,
//																										Color lineColor, Color lineHiliteColor) {
//		drawOneLimit(g, minSelPos, minEditPos, lineColor, lineHiliteColor, lowPending, true);
//		drawOneLimit(g, maxSelPos, maxEditPos, lineColor, lineHiliteColor, highPending, false);
//	}
	
	private void drawBars(Graphics g, Color barColor, Color[] barShade, int minX, int maxX, int barWidth) {
		int halfBarWidth = barWidth / 2;
		DiscreteDistnVariable distn = (DiscreteDistnVariable)getVariable(distnKey);
		
		double maxProb = distn.getMaxScaledProb();
		double probFactor = (getSize().height - kTopBorder - kDistnTopBorder) / maxProb;
		
		int baseline = getSize().height;
		for (int i=minX ; i<=maxX ; i++) {
			double doubleHt = distn.getScaledProb(i) * probFactor;
			int pixHeight = (int)Math.floor(doubleHt);
			int topPixShade = (int)Math.round((doubleHt - pixHeight) * kNoOfShades);
			
			g.setColor(barColor);
			int xPos = horizAxis.numValToRawPosition(i);
			g.fillRect(xPos - halfBarWidth, baseline - pixHeight, barWidth, pixHeight);
			g.setColor(barShade[topPixShade]);
			g.fillRect(xPos - halfBarWidth, baseline - pixHeight - 1, barWidth, 1);
		}
	}
	
	private void drawProb(Graphics g, double prob, int minX, int maxX, Color c) {
		g.setColor(c);
		tempVal.setValue(prob);
			
//		int valCenter = (minX + maxX) / 2;
		int valCenter = getSize().width / 2;
		int ascent = g.getFontMetrics().getAscent();
		int valBaseline = kProbVertGap + ascent;
		kProbLabel.drawCentred(g, valCenter, valBaseline);
		
		valBaseline += (ascent + 3);
		tempVal.drawCentred(g, valCenter, valBaseline);
	}
}
	
