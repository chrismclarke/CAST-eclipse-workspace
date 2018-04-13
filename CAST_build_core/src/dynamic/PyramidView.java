package dynamic;

import java.awt.*;

import dataView.*;


public class PyramidView extends DataView {
//	static public final String PYRAMID_VIEW = "pyramidView";
	
	static final private int LEFT = -1;
	static final private int RIGHT = 1;
	
	static final private Color kHairlineColor = new Color(0xDDDDDD);
	
	static final private Color kLeftFillColor = new Color(0x9999FF);
	static final private Color kLeftBorderColor = new Color(0x0000FF);
	static final private Color kRightFillColor = new Color(0xFF9999);
	static final private Color kRightBorderColor = new Color(0xFF0000);
	static final private Color kGroupedFillColor = new Color(0xCCCCCC);
	static final private Color kGroupedBorderColor = new Color(0x999999);
	
	static final private int kTickLength = 4;
	static final private int kTickVertGap = 2;
	static final private int kTickHorizGap = 4;
	static final private int kTextVertGap = 4;
	
	static final private double kDecayFactor = 1.0 / 3.0;
	
	static final private LabelValue kAgeLabel = new LabelValue("Age");
	
	static final public int kFinalFrame = 40;
	static final private int kFramesPerSec = 10;
	
	private LabelValue kPercentageLabel;
	
	private String leftKey, rightKey;
	protected int classWidth;
	private int freqMax, axisMax, axisStep;
	private LabelValue freqLabel;
	
	private boolean showPercentage = false;
	
	private boolean initialised = false;
	
	private int maxAgeWidth, centreWidth, ascent, descent, topBorder, bottomBorder, leftRightBorder;
	private NumValue tempVal = new NumValue(0.0, 0);
	
	public PyramidView(DataSet theData, XApplet applet, String leftKey, String rightKey,
										int classWidth, int freqMax, int axisMax, int axisStep, LabelValue freqLabel) {
		super(theData, applet, null);
		kPercentageLabel = new LabelValue(applet.translate("Percentage"));
		this.leftKey = leftKey;
		this.rightKey = rightKey;
		this.classWidth = classWidth;
		this.freqMax = freqMax;
		this.axisMax = axisMax;
		this.axisStep = axisStep;
		this.freqLabel = freqLabel;
	}
	
	public void setShowPercentage(boolean showPercentage) {
		this.showPercentage = showPercentage;
		setFrame(showPercentage ? kFinalFrame : 0);
	}
	
	public void animateShowPercentage(boolean showPercentage) {
		this.showPercentage = showPercentage;
		animateFrames(showPercentage ? 1 : kFinalFrame-1, showPercentage ? kFinalFrame-1 : 1-kFinalFrame,
																																							kFramesPerSec, null);
	}
	
	protected void doInitialisation(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		maxAgeWidth = Math.max(kAgeLabel.stringWidth(g), fm.stringWidth("100"));
		centreWidth = maxAgeWidth + 2 * (kTickLength + kTickHorizGap);
		
		topBorder = ascent + descent + kTextVertGap + ascent / 2 + 1;
		bottomBorder = 2 * ascent + descent + kTextVertGap + kTickLength + kTickVertGap + 2;
		tempVal.setValue(Math.max(100, (axisMax / axisStep) * axisStep));		//	100 is for percentage view
		leftRightBorder = tempVal.stringWidth(g) / 2 + 1;
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			initialised = true;
			doInitialisation(g);
		}
	}
	
	private void drawAgeAxis(Graphics g, int axisHoriz, int bottom, int ageAxisLength, int direction) {
		g.setColor(getForeground());
		g.drawLine(axisHoriz, topBorder, axisHoriz, bottom);
		
		for (int i=0 ; i<=100 ; i+=10) {
			int tickVert = bottom - (i * ageAxisLength) / 100;
			g.drawLine(axisHoriz, tickVert, axisHoriz - kTickLength * direction, tickVert);
		}
	}
	
	private void drawFreqAxis(Graphics g, String varName, int axisHoriz, int freqAxisWidth, int bottom,
																														int ageAxisLength, int direction) {
		g.setColor(Color.white);
		if (direction > 0)
			g.fillRect(axisHoriz + 1, topBorder, freqAxisWidth, ageAxisLength);
		else
			g.fillRect(axisHoriz - freqAxisWidth, topBorder, freqAxisWidth, ageAxisLength);
		
		g.setColor(kHairlineColor);
		Font oldFont = g.getFont();
		Font nameFont = new Font(oldFont.getName(), Font.BOLD, 24);
		g.setFont(nameFont);
		FontMetrics fm = g.getFontMetrics();
		int baseline = topBorder + 2 * fm.getAscent();
		int centre = axisHoriz + freqAxisWidth / 2 * direction;
		int left = centre - fm.stringWidth(varName) / 2;
		g.drawString(varName, left, baseline);
		g.setFont(oldFont);
		
		g.setColor(getForeground());
		g.drawLine(axisHoriz, bottom, axisHoriz + freqAxisWidth * direction, bottom);
		
		int freqValueBaseline = bottom + kTickLength + kTickVertGap + ascent;
		int step = showPercentage ? 20 : axisStep;
		int max = showPercentage ? 100 : axisMax;
		int axisVal = step;
		while (axisVal <= max) {
			int horiz = axisHoriz + (axisVal * freqAxisWidth) / max * direction;
			
			g.setColor(kHairlineColor);
			g.drawLine(horiz, topBorder, horiz, bottom - 1);
			
			g.setColor(getForeground());
			g.drawLine(horiz, bottom, horiz, bottom + kTickLength);
			
			tempVal.setValue(axisVal);
			tempVal.drawCentred(g, horiz, freqValueBaseline);
			
			axisVal += step;
		}
		
		int labelBaseline = freqValueBaseline + kTextVertGap + ascent;
		int labelCenter = axisHoriz + direction * freqAxisWidth / 2;
		if (showPercentage)
			kPercentageLabel.drawCentred(g, labelCenter, labelBaseline);
		else
			freqLabel.drawCentred(g, labelCenter, labelBaseline);
	}
	
	protected int[] getFrequencies(NumVariable freqVar) {
		int nClasses = freqVar.noOfValues();
		int freq[] = new int[nClasses];
		int leftOver = 0;
		for (int i=0 ; i<nClasses ; i++)
			if (leftOver > 0) {
				freq[i] = (leftOver * 2) / 3;
				leftOver -= freq[i];
			}
			else {
				freq[i] = (int)Math.round(freqVar.doubleValueAt(i));
				if (i < (nClasses - 1) && Double.isNaN(freqVar.doubleValueAt(i + 1))) {
					int classesLeft = nClasses - i;
					double thisFactor = (1.0 - kDecayFactor) / (1.0 - Math.pow(1.0 / 3.0, classesLeft));
					leftOver = (int)Math.round(freq[i] * (1.0 - thisFactor));
					freq[i] -= leftOver;
				}
			}
		return freq;
	}
	
	protected int[] getOuterFrequencies(NumVariable freqVar, NumVariable otherVar) {
		return null;
	}
	
	protected int[] getInnerFrequencies(NumVariable freqVar, NumVariable otherVar, int[] outerFreq) {
		return getFrequencies(freqVar);
	}
	
	protected boolean[] areEstimates(NumVariable freqVar) {
		int nClasses = freqVar.noOfValues();
		boolean ests[] = new boolean[nClasses];
		for (int i=0 ; i<nClasses ; i++)
			ests[i] = Double.isNaN(freqVar.doubleValueAt(i))
																|| (i < nClasses - 1) && Double.isNaN(freqVar.doubleValueAt(i + 1));
		return ests;
	}
	
	protected boolean[] innerAreEstimates(NumVariable freqVar, NumVariable otherVar) {
		return areEstimates(freqVar);
	}
	
	protected boolean[] outerAreEstimates(NumVariable freqVar, NumVariable otherVar) {
		return null;
	}
	
	protected void drawHistogramBars(Graphics g, NumVariable freqVar, NumVariable otherVar, int axisHoriz,
												int freqAxisWidth, int bottom, int ageAxisLength, int direction, Color fillColor,
												Color fillBorderColor) {
		int classTop = 0;
		int classRectTop = bottom;
		
		int nClasses = freqVar.noOfValues();
		int outerFreq[] = getOuterFrequencies(freqVar, otherVar);
		int innerFreq[] = getInnerFrequencies(freqVar, otherVar, outerFreq);
		boolean innerAreEstimates[] = innerAreEstimates(freqVar, otherVar);
		boolean outerAreEstimates[] = outerAreEstimates(freqVar, otherVar);
		
		for (int i=0 ; i<nClasses ; i++) {
//			int classBottom = classTop;
			int classRectBottom = classRectTop;
			if (classRectBottom < topBorder)
				return;
			
			classTop += classWidth;
			classRectTop = bottom - (classTop * ageAxisLength) / 100;
			if (classRectTop < topBorder)
				classRectTop = topBorder;
			
			long longInner = innerFreq[i];
			long longOuter = (outerFreq == null) ? longInner : outerFreq[i];
			
			int currentFrame = getCurrentFrame();
			if (currentFrame > 0) {
				long finalInner = (longInner * freqMax) / longOuter;
				long finalOuter = freqMax;
				longInner = (longInner * (kFinalFrame - currentFrame) + finalInner * currentFrame) / kFinalFrame;
				longOuter = (longOuter * (kFinalFrame - currentFrame) + finalOuter * currentFrame) / kFinalFrame;
			}
			
			int innerRectWidth = (int)((longInner * freqAxisWidth) / freqMax);			//	to avoid overflow
			int outerRectWidth = (int)((longOuter * freqAxisWidth) / freqMax);			//	to avoid overflow
			
			if (innerAreEstimates != null && innerAreEstimates[i]
															|| currentFrame > 0 && outerFreq != null && longInner == longOuter)
				g.setColor(kGroupedFillColor);
			else
				g.setColor(fillColor);
				
			if (direction > 0)
				g.fillRect(axisHoriz + 1, classRectTop, innerRectWidth, classRectBottom - classRectTop);
			else
				g.fillRect(axisHoriz - innerRectWidth, classRectTop, innerRectWidth, classRectBottom - classRectTop);
			
			int borderRectWidth = Math.max(outerRectWidth - innerRectWidth, 0);
			if (borderRectWidth > 0) {
				if (outerAreEstimates != null && outerAreEstimates[i])
					g.setColor(kGroupedBorderColor);
				else
					g.setColor(fillBorderColor);
				if (direction > 0)
					g.fillRect(axisHoriz + innerRectWidth + 1, classRectTop, borderRectWidth, classRectBottom - classRectTop);
				else
					g.fillRect(axisHoriz - outerRectWidth, classRectTop, borderRectWidth, classRectBottom - classRectTop);
			}
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int centre = getSize().width / 2;
		kAgeLabel.drawCentred(g, centre, ascent);
		
		int bottom = getSize().height - bottomBorder;
		int ageAxisLength = bottom - topBorder;
		for (int i=0 ; i<=100 ; i+=10) {
			int tickVert = bottom - (i * ageAxisLength) / 100;
			tempVal.setValue(i);
			tempVal.drawCentred(g, centre, tickVert +  ascent / 2);
		}
		int freqAxisWidth = (getSize().width - centreWidth) / 2 - leftRightBorder;
		
		int leftAxisHoriz = centre - centreWidth / 2;
		drawAgeAxis(g, leftAxisHoriz, bottom, ageAxisLength, LEFT);
		int rightAxisHoriz = centre + centreWidth / 2;
		drawAgeAxis(g, rightAxisHoriz, bottom, ageAxisLength, RIGHT);
		
		NumVariable leftVar = (NumVariable)getVariable(leftKey);
		drawFreqAxis(g, leftVar.name, leftAxisHoriz, freqAxisWidth, bottom, ageAxisLength, LEFT);
		NumVariable rightVar = (NumVariable)getVariable(rightKey);
		drawFreqAxis(g, rightVar.name, rightAxisHoriz, freqAxisWidth, bottom, ageAxisLength, RIGHT);
		
		drawHistogramBars(g, leftVar, rightVar, leftAxisHoriz, freqAxisWidth, bottom, ageAxisLength, LEFT,
															kLeftFillColor, kLeftBorderColor);
		drawHistogramBars(g, rightVar, leftVar, rightAxisHoriz, freqAxisWidth, bottom, ageAxisLength, RIGHT,
															kRightFillColor, kRightBorderColor);
	}
	
//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
	
