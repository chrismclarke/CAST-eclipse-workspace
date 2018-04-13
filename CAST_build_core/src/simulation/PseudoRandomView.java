package simulation;

import java.awt.*;

import dataView.*;


class IntChars {
	private int value = 0;
	private char[] digits = null;
	
	void update(int newValue) {
		if (digits == null || newValue != value) {
			int temp = newValue;
			int noOfDigits = 1;
			while (temp > 9) {
				temp /= 10;
				noOfDigits ++;
			}
			if (digits == null || digits.length != noOfDigits)
				digits = new char[noOfDigits];
			
			for (int i=0 ; i<noOfDigits ; i++) {
				digits[noOfDigits - i - 1] = (char)('0' + newValue % 10);
				newValue /= 10;
			}
		}
	}
	
	int countDigits() {
		return digits.length;
	}
	
	void draw(Graphics g, int charStart, int baseline, int charSpacing) {
		for (int i=0 ; i<digits.length ; i++) {
			g.drawChars(digits, i, 1, charStart, baseline);
			charStart += charSpacing;
		}
	}
}


public class PseudoRandomView extends DataView {
//	static final public String PSEUDO_RAND_CALC = "pseudoRandCalc";
	
	static final private int kTopBottomBorder = 5;
	static final private int kLeftRightBorder = 10;
	static final private int kCharSpacing = 3;
	static final private int kLineGap = 16;
	static final private int kOperatorWidth = 18;
	static final private int kSignHalfWidth = 3;
	static final private int kMinDisplayLines = 6;
	static final private int kArrowWidth = 10;
	
	private String generatorKey;
	private int maxSeedDigits, maxResultDigits, maxTimesDigits, maxPlusDigits;
	
	private boolean initialised = false;
	private int ascent;
	
	private int oneCharWidth;
	private int minWidth, minHeight;
	
	private IntChars plusChars = new IntChars();
	private IntChars timesChars = new IntChars();
//	private int plus = 0;
//	private int times = 0;
	
	private char[] tempChar = new char[1];
	private int[] arrowX = new int[8];
	private int[] arrowY = new int[8];
	
	public PseudoRandomView(DataSet theData, XApplet applet, int maxSeedDigits, int maxResultDigits,
										int maxTimesDigits, int maxPlusDigits, String generatorKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.generatorKey = generatorKey;
		this.maxSeedDigits = maxSeedDigits;
		this.maxResultDigits = maxResultDigits;
		this.maxTimesDigits = maxTimesDigits;
		this.maxPlusDigits = maxPlusDigits;
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			
			oneCharWidth = fm.stringWidth("0");
			
			minHeight = 2 * kTopBottomBorder + 2 + kMinDisplayLines * ascent
																		+ (kMinDisplayLines - 1) * kLineGap;
			minWidth = getWidth(maxSeedDigits, maxTimesDigits, maxPlusDigits, maxResultDigits);
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	private int getWidth(int seedDigits, int timesDigits, int plusDigits, int resultDigits) {
		return 2 * kLeftRightBorder + 2 + (seedDigits + timesDigits + plusDigits + resultDigits)
							* (oneCharWidth + kCharSpacing) + 3 * kOperatorWidth + kArrowWidth;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(minWidth, minHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	private void drawInt(Graphics g, int baseline, int startPos, int maxDigits, int value) {
		int tempPos = startPos + maxDigits * (oneCharWidth + kCharSpacing);
		while (value > 0) {
			tempPos -= (oneCharWidth + kCharSpacing);
			tempChar[0] = (char)('0' + value % 10);
			g.drawChars(tempChar, 0, 1, tempPos, baseline);
			value /= 10;
		}
	}
	
	private void drawArrow(Graphics g, int x, int y) {
		arrowX[0] = arrowX[7] = x;
		arrowX[1] = arrowX[2] = arrowX[5] = arrowX[6] = x + 6;
		arrowX[3] = arrowX[4] = x + 8;
		
		arrowY[0] = arrowY[7] = y;
		arrowY[1] = y - 6;
		arrowY[2] = arrowY[3] = y - 4;
		arrowY[4] = arrowY[5] = y + 4;
		arrowY[6] = y + 6;
		
		g.fillPolygon(arrowX, arrowY, 8);
		g.drawPolygon(arrowX, arrowY, 8);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		PseudoRandomVariable pseudoRandomVar = (PseudoRandomVariable)getVariable(generatorKey);
		plusChars.update(pseudoRandomVar.getPlus());
		timesChars.update(pseudoRandomVar.getTimes());
		
		int noOfPlusChars = plusChars.countDigits();
		int noOfTimesChars = timesChars.countDigits();
		int noOfSeedChars = pseudoRandomVar.getSeedDigits();
		int noOfResultChars = noOfSeedChars + noOfTimesChars;
		
		int widthUsed = getWidth(noOfSeedChars, noOfTimesChars, noOfPlusChars, noOfResultChars);
		
		int leftRightOutside = (getSize().width - widthUsed) / 2;
		
		g.setColor(Color.white);
		g.fillRect(leftRightOutside, 0, widthUsed, getSize().height);
		g.setColor(getForeground());
		
		int seedStart = leftRightOutside + 1 + kLeftRightBorder;
		int timesSignStart = seedStart + noOfSeedChars * (oneCharWidth + kCharSpacing);
		int timesSignMiddle = timesSignStart + kOperatorWidth / 2;
		seedStart += kCharSpacing / 2;
		int timesStart = timesSignStart + kOperatorWidth;
		int plusSignStart = timesStart + noOfTimesChars * (oneCharWidth + kCharSpacing);
		int plusSignMiddle = plusSignStart + kOperatorWidth / 2;
		timesStart += kCharSpacing / 2;
		int plusStart = plusSignStart + kOperatorWidth;
		int equalSignStart = plusStart + noOfPlusChars * (oneCharWidth + kCharSpacing);
		int equalSignMiddle = equalSignStart + kOperatorWidth / 2;
		plusStart += kCharSpacing / 2;
		int resultStart = equalSignStart + kOperatorWidth;
		
		int resultHiliteStart = resultStart + (noOfResultChars - noOfSeedChars) * (oneCharWidth + kCharSpacing);
		int resultHiliteEnd = resultHiliteStart + noOfSeedChars * (oneCharWidth + kCharSpacing);
		
		resultStart += kCharSpacing / 2;
		
		int noOfValues = pseudoRandomVar.noOfValues();
		int maxDisplayValues = (getSize().height - 2 - 2 * kTopBottomBorder + kLineGap)
																							/ (ascent + kLineGap);
		int firstDrawIndex, firstDrawBaseline;
		if (maxDisplayValues >= noOfValues) {
			firstDrawIndex = 0;
			firstDrawBaseline = 1 + kTopBottomBorder + ascent;
		}
		else {
			firstDrawIndex = noOfValues - maxDisplayValues - 1;
			firstDrawBaseline = getSize().height - 1 - kTopBottomBorder
												- (noOfValues - firstDrawIndex - 1) * (ascent + kLineGap);
		}
		
//		NumValue seedValue = new NumValue(0.0, 0);
//		NumValue resultValue = new NumValue(0.0, 0);
		int baseline = firstDrawBaseline;
		for (int i=firstDrawIndex ; i<noOfValues ;  i++) {
			if (i > 0) {
				int lastSeed = (int)Math.round(pseudoRandomVar.doubleValueAt(i - 1));
				g.setColor(Color.blue);
				g.drawRect(seedStart - kCharSpacing/2 - 1, baseline - ascent - 2,
										noOfSeedChars * (oneCharWidth + kCharSpacing) + 1, ascent + 3);
				g.setColor(getForeground());
				drawInt(g, baseline, seedStart, noOfSeedChars, lastSeed);
				
				g.drawLine(timesSignMiddle - kSignHalfWidth, baseline - 2 * kSignHalfWidth - 1,
											timesSignMiddle + kSignHalfWidth, baseline - 1);
				g.drawLine(timesSignMiddle - kSignHalfWidth, baseline - 1,
								timesSignMiddle + kSignHalfWidth, baseline - 2 * kSignHalfWidth - 1);
									
				timesChars.draw(g, timesStart, baseline, oneCharWidth + kCharSpacing);
				
				g.drawLine(plusSignMiddle - kSignHalfWidth, baseline - kSignHalfWidth - 1,
										plusSignMiddle + kSignHalfWidth, baseline - kSignHalfWidth - 1);
				g.drawLine(plusSignMiddle, baseline - 2 * kSignHalfWidth - 1, plusSignMiddle,
																									baseline - 1);
									
				plusChars.draw(g, plusStart, baseline, oneCharWidth + kCharSpacing);
				
				g.drawLine(equalSignMiddle - kSignHalfWidth, baseline - 6,
											equalSignMiddle + kSignHalfWidth, baseline - 6);
				g.drawLine(equalSignMiddle - kSignHalfWidth, baseline - 3,
											equalSignMiddle + kSignHalfWidth, baseline - 3);
				
				g.setColor(Color.yellow);
				int rectTop = Math.max(baseline - ascent - 2, 1);
				int rectBottom = Math.max(baseline + 2, 1);
				if (rectBottom > rectTop)
					g.fillRect(resultHiliteStart, rectTop,
									resultHiliteEnd - resultHiliteStart, rectBottom - rectTop);
				
				g.setColor(getForeground());
				drawInt(g, baseline, resultStart, noOfResultChars,
																(int)pseudoRandomVar.calcBeforeMod(lastSeed));
			}
			else {
				g.setColor(Color.yellow);
				g.fillRect(resultHiliteStart, baseline - ascent - 2,
									resultHiliteEnd - resultHiliteStart, ascent + 4);
				
				g.setColor(getForeground());
				
				int thisSeed = (int)Math.round(pseudoRandomVar.doubleValueAt(i));
				drawInt(g, baseline, resultStart, noOfResultChars, thisSeed);
			}
			if (i > 0) {
				g.setColor(Color.blue);
				g.drawLine(timesSignStart, baseline - ascent, resultHiliteStart, baseline - ascent - kLineGap + 2);
				g.setColor(getForeground());
			}
			if (i == noOfValues - 1) {
				g.setColor(Color.red);
				drawArrow(g, resultHiliteEnd + 2, baseline - ascent / 2);
				g.setColor(getForeground());
			}
			baseline += (ascent + kLineGap);
		}
		g.drawRect(leftRightOutside, 0, widthUsed - 1, getSize().height - 1);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
