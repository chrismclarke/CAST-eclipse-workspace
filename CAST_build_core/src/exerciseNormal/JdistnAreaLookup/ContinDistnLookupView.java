package exerciseNormal.JdistnAreaLookup;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class ContinDistnLookupView extends CoreDistnLookupView {
//	static final public String CONTIN_DISTN_LOOKUP = "continDistnLookup";
	
	static final private int kTopBorder = 30;
	static final private int kDistnTopBorder = 10;
	static final private int kProbGap = 5;
	static final private int kProbVertGap = 4;
	
	static final private int LEFT = 0;
	static final private int CENTER = 1;
	static final private int RIGHT = 2;
	
	private Color dimColor = Color.lightGray;
	private Color hiliteColor = new Color(0x3366FF);
	private Color lowColor = new Color(0xFF6633);
	private Color centerColor = new Color(0xCC3399);
	private Color highColor = new Color(0x009900);
	
	private Color hiliteTextColor, lowTextColor, centerTextColor, highTextColor;
	
	private boolean singleDensity = false;
	
	private Color dimShade[] = new Color[kNoOfShades + 1];
	private Color hiliteShade[] = new Color[kNoOfShades + 1];
	private Color centerShade[] = new Color[kNoOfShades + 1];
	private Color lowShade[] = new Color[kNoOfShades + 1];
	private Color highShade[] = new Color[kNoOfShades + 1];
	
	private int pixHt[];
	private int topPixShade[];
	
	private int maxHeight; //		getSize().height at time of initialisation
	
	public ContinDistnLookupView(DataSet theData, XApplet applet,
					HorizAxis horizAxis, String distnKey, boolean highAndLow) {
		super(theData, applet, horizAxis, distnKey, highAndLow, CONTINUOUS);
		
		setColors(dimColor, hiliteColor, lowColor, centerColor, highColor);
		setFont(applet.getStandardBoldFont());
	}
	
	public void setColors(Color dimColor, Color hiliteColor, Color lowColor,
																									Color centerColor, Color highColor) {
		this.dimColor = dimColor;
		this.hiliteColor = hiliteColor;
		this.lowColor = lowColor;
		this.centerColor = centerColor;
		this.highColor = highColor;
		
		AccurateDistnArtist.setShades(dimShade, dimColor);
		AccurateDistnArtist.setShades(hiliteShade, hiliteColor);
		AccurateDistnArtist.setShades(lowShade, lowColor);
		AccurateDistnArtist.setShades(centerShade, centerColor);
		AccurateDistnArtist.setShades(highShade, highColor);
		
		hiliteTextColor = darkenColor(hiliteColor, 0.5);
		lowTextColor = darkenColor(lowColor, 0.5);
		centerTextColor = darkenColor(centerColor, 0.5);
		highTextColor = darkenColor(highColor, 0.5);
	}
	
	public void setSingleDensity() {
		singleDensity = true;
	}
	
	protected void doInitialisation(Graphics g) {
		ContinDistnVariable y = (ContinDistnVariable)getVariable(distnKey);
			
		int noOfClasses = horizAxis.getAxisLength();
		double min = horizAxis.minOnAxis;
		double max = horizAxis.maxOnAxis;
		
		double prob[] = new double[noOfClasses + 2];
		double x = min;
		double step = (max - min) / noOfClasses;
		double lastCum = 0.0;
		for (int i=0 ; i<=noOfClasses ; i++) {
			double nextCum = y.getCumulativeProb(x);
			prob[i] = nextCum - lastCum;
			lastCum = nextCum;
			x += step;
		}
		prob[noOfClasses + 1] = 1.0 - lastCum;
		
		double maxProb = 0.0;
		for (int i=1 ; i<prob.length - 1 ; i++)
			maxProb = Math.max(maxProb, prob[i]);
		
		if (pixHt == null || pixHt.length != (prob.length - 2)) {
			pixHt = new int[prob.length - 2];
			topPixShade = new int[prob.length - 2];
		}
		maxHeight = getSize().height;
		if (editPanel != null)
			maxHeight -= kTopBorder;
		if (highAndLow && !singleDensity)
			maxHeight /= 3;
		maxHeight -= kDistnTopBorder;
		for (int i=0 ; i<pixHt.length ; i++) {
			double doubleHt = prob[i + 1] / maxProb * maxHeight;
			pixHt[i] = (int)Math.floor(doubleHt);
			topPixShade[i] = (int)Math.round((doubleHt - pixHt[i]) * kNoOfShades);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		if (maxHeight != getSize().height)
			doInitialisation(g);		//	This needs to be called because sometimes panel gets resized after initialisation
		
		ContinDistnVariable y = (ContinDistnVariable)getVariable(distnKey);
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
		
		double lowProb = y.getCumulativeProb(y.getMinSelection());
		double highProb = 1 - y.getCumulativeProb(y.getMaxSelection());
		double centerProb = 1 - lowProb - highProb;
		
		int baseline = getSize().height;
		int topBorder = (editPanel == null) ? 0 : kTopBorder;
		
		if (highAndLow) {
			drawBars(g, lowColor, lowShade, 0, minSelPos, baseline);
			drawBars(g, centerColor, centerShade, minSelPos, maxSelPos, baseline);
			drawBars(g, highColor, highShade, maxSelPos, getSize().width - 1, baseline);
			
			if (!singleDensity) {
				baseline -= (getSize().height - topBorder) / 3;
				drawBars(g, dimColor, dimShade, 0, minSelPos, baseline);
				drawBars(g, hiliteColor, hiliteShade, minSelPos, getSize().width - 1, baseline);
				
				baseline -= (getSize().height - topBorder) / 3;
				drawBars(g, hiliteColor, hiliteShade, 0, maxSelPos, baseline);
				drawBars(g, dimColor, dimShade, maxSelPos, getSize().width - 1, baseline);
			}
		}
		else {
//			drawBars(g, lowColor, hiliteShade, 0, maxSelPos, baseline);
//			drawBars(g, highColor, dimShade, maxSelPos, getSize().width - 1, baseline);
			drawBars(g, lowColor, lowShade, 0, maxSelPos, baseline);
			drawBars(g, highColor, highShade, maxSelPos, getSize().width - 1, baseline);
		}
		
		g.setColor(Color.red);
		int minEditPos = getSize().width / 4;
		int maxEditPos = highAndLow ? getSize().width * 3 / 4 : getSize().width / 2;
		if (highAndLow) {
			if (lowPending)
				kFinishTyping.drawRight(g, 5, 20);
			else {
				g.drawLine(minSelPos, topBorder, minSelPos, getSize().height);
				if (editPanel != null)
					g.drawLine(minEditPos, 0, minSelPos, topBorder);
			}
		}
		if (highPending)
			kFinishTyping.drawLeft(g, getSize().width - 5, 20);
		else {
			g.drawLine(maxSelPos, topBorder, maxSelPos, getSize().height);
			if (editPanel != null)
				g.drawLine(maxEditPos, 0, maxSelPos, topBorder);
		}
		
		if (doingDrag) {
			if (extremeSelected == MIN_SELECTED) {
				g.fillRect(minSelPos - 1, topBorder, 3, getSize().height - topBorder);
				g.setColor(Color.black);
				g.drawLine(minSelPos, topBorder, minSelPos, getSize().height);
				g.drawLine(minEditPos, 0, minSelPos, topBorder);
			}
			else {
				g.fillRect(maxSelPos - 1, topBorder, 3, getSize().height - topBorder);
				g.setColor(Color.black);
				g.drawLine(maxSelPos, topBorder, maxSelPos, getSize().height);
				g.drawLine(maxEditPos, 0, maxSelPos, topBorder);
			}
		}
		
		baseline = getSize().height;
		if (highAndLow) {
			drawProb(g, lowProb, 0, minSelPos, LEFT, baseline, lowTextColor);
			drawProb(g, centerProb, minSelPos, maxSelPos, CENTER, baseline, centerTextColor);
			drawProb(g, highProb, maxSelPos, getSize().width - 1, RIGHT, baseline, highTextColor);
			
			if (!singleDensity) {
				baseline -= (getSize().height - topBorder) / 3;
				drawProb(g, 1 - lowProb, minSelPos, getSize().width - 1, RIGHT, baseline, hiliteTextColor);
				
				baseline -= (getSize().height - topBorder) / 3;
				drawProb(g, 1 - highProb, 0, maxSelPos, LEFT, baseline, hiliteTextColor);
			}
		}
		else {
			drawProb(g, 1 - highProb, 0, maxSelPos, LEFT, baseline, lowTextColor);
			drawProb(g, highProb, maxSelPos, getSize().width - 1, RIGHT, baseline, highTextColor);
		}
	}
	
	private void drawBars(Graphics g, Color mainColor, Color[] shade,
																													int minX, int maxX, int baseline) {
		g.setColor(mainColor);
//		int displayHt = getSize().height;
		for (int i=minX ; i<=maxX ; i++)
			if (pixHt[i] > 0)
				g.fillRect(i, baseline - pixHt[i], 1, pixHt[i]);
		
		for (int i=minX ; i<=maxX ; i++) {
			g.setColor(shade[topPixShade[i]]);
			int topPix = baseline - pixHt[i] - 1;
			g.fillRect(i, topPix, 1, 1);
		}
	}
	
	private void drawProb(Graphics g, double prob, int minX, int maxX, int segment,
																																int baseline, Color c) {
		g.setColor(c);
		tempVal.setValue(prob);
			
		if (segment == CENTER) {
			int valCenter = (minX + maxX) / 2;
			int valBaseline = baseline - kProbVertGap;
			tempVal.drawCentred(g, valCenter, valBaseline);
		}
		else {
			int valWidth = tempVal.stringWidth(g);
			
			int maxHt = 0;
			for (int i=minX ; i<=maxX ; i++)
				maxHt = Math.max(maxHt, pixHt[i]);
			int baselineHt = Math.max(maxHt / 3, g.getFontMetrics().getAscent() + kProbVertGap);
			int valBaseline = baseline - baselineHt;
			
			if (segment == LEFT) {
				int valRight;
				for (valRight=0 ; valRight<maxX ; valRight++)
					if (baselineHt < pixHt[valRight])
						break;
				valRight = Math.max(valRight - kProbGap, valWidth);
				tempVal.drawLeft(g, valRight, valBaseline);
			}
			else {			//	segment == RIGHT
				int valLeft;
				for (valLeft=getSize().width - 1 ; valLeft>minX ; valLeft--)
					if (baselineHt < pixHt[valLeft])
						break;
				valLeft = Math.min(valLeft + kProbGap, getSize().width - valWidth);
				tempVal.drawRight(g, valLeft, valBaseline);
			}
		}
	}
	
	public double getPixError(double x) {
		DistnVariable distn = (DistnVariable)getVariable(distnKey);
		double xPlus = twoPixOffsetValue(x, 1);
		double xMinus = twoPixOffsetValue(x, -1);
		return distn.getCumulativeProb(xPlus) - distn.getCumulativeProb(xMinus);
	}
	
	static final private int kAxisPixels = 400;		//	assumed axis length of 400 must be used when applet checks answer
																								//	This can happen before applet layout, so axisLength can still be 0
	
	private double twoPixOffsetValue(double x, int offsetPix) {
		return x + offsetPix * (horizAxis.maxOnAxis - horizAxis.minOnAxis) / kAxisPixels;
//		double xOffset;
//		try {
//			xOffset = horizAxis.positionToNumVal(horizAxis.numValToRawPosition(x) + offsetPix);
//		} catch (AxisException e) {
//			xOffset = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? horizAxis.minOnAxis : horizAxis.maxOnAxis;
//		}
//		return xOffset;
	}
	
	public double twoPixelValue() {
		return 2 * (horizAxis.maxOnAxis - horizAxis.minOnAxis) / kAxisPixels;
//		int centrePos = getSize().width / 2;
//		try {
//			return horizAxis.positionToNumVal(centrePos) - horizAxis.positionToNumVal(centrePos - 2);
//		} catch (AxisException e) {
//			return 0.0;
//		}
	}
}
	
