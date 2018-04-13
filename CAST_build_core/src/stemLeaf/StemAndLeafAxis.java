package stemLeaf;

import java.awt.*;
import java.util.*;

import dataView.*;

public class StemAndLeafAxis {
	static final private int kNaNLeaf = -999999;
	
	static private final int kMaxBins = 50;
	
	static final int kTopBorder = 4;
	static final int kUnitsSpace = 9;			//		space between units heading & plot
	static final int kUnitsOffset = 5;
	static final int kLineGap = 3;
	static final int kLeftOffset = 3;

	class StemLeafException extends Exception {
		public int axisProblem;
		StemLeafException(int theAxisProblem) {
			axisProblem = theAxisProblem;
		}
		
		final public static int FORMAT_ERROR = 0;
		final public static int TOO_LOW_ERROR = 1;
		final public static int TOO_HIGH_ERROR = 2;
	}

	
	int minStem, repeatsPerStem, minStemRepeat, noOfBins;
	int stemPower;
	boolean minStemNeg;
	int leavesPerBin;
	
	int minStemAndLeaf, maxStemAndLeaf;
	
	Font theFont = null;
	int stemWidth, leafStart;
	
	String unitsString = null;
	
	private XApplet applet;
	
	public StemAndLeafAxis(String axisInfo, XApplet applet) {
		readStemInfo(axisInfo);
		this.applet = applet;
	}
	
	public int getHeadingHt() {
		return kTopBorder + kUnitsSpace + LeafDigitImages.kDigitHeight;
	}
	
	public int getLineHt() {
		return LeafDigitImages.kDigitHeight + LeafDigitImages.kVertSpace;
	}
	
	private void readStemInfo(String axisInfo) {
		StringTokenizer theStems = new StringTokenizer(axisInfo);
		
		try {
			if (!theStems.hasMoreTokens())
				throw new StemLeafException(StemLeafException.FORMAT_ERROR);
			String tempString = theStems.nextToken();
			minStemNeg = tempString.charAt(0) == '-';
			minStem = Integer.parseInt(tempString);
			if (minStemNeg)
				minStem--;
			
			if (!theStems.hasMoreTokens())
				throw new StemLeafException(StemLeafException.FORMAT_ERROR);
			tempString = theStems.nextToken();
			repeatsPerStem = Integer.parseInt(tempString);
			if (repeatsPerStem != 1 && repeatsPerStem != 2 && repeatsPerStem != 5)
				throw new StemLeafException(StemLeafException.FORMAT_ERROR);
			leavesPerBin = (repeatsPerStem == 1) ? 10 : (repeatsPerStem == 2) ? 5 : 2;
			
			if (!theStems.hasMoreTokens())
				throw new StemLeafException(StemLeafException.FORMAT_ERROR);
			tempString = theStems.nextToken();
			minStemRepeat = Integer.parseInt(tempString);
			if (minStemRepeat < 0 || minStemRepeat >= repeatsPerStem)
				throw new StemLeafException(StemLeafException.FORMAT_ERROR);
			
			if (!theStems.hasMoreTokens())
				throw new StemLeafException(StemLeafException.FORMAT_ERROR);
			tempString = theStems.nextToken();
			noOfBins = Integer.parseInt(tempString);
			if (noOfBins < 1 || noOfBins >= kMaxBins)
				throw new StemLeafException(StemLeafException.FORMAT_ERROR);
			
			if (!theStems.hasMoreTokens())
				throw new StemLeafException(StemLeafException.FORMAT_ERROR);
			tempString = theStems.nextToken();
			stemPower = Integer.parseInt(tempString);
			
			if (theStems.hasMoreTokens()) {
				unitsString = "";
				while (theStems.hasMoreTokens())
					unitsString += (" " + theStems.nextToken());
			}
		} catch (NumberFormatException e) {
			System.err.println("Badly formatted stem & leaf specification");
			setDefaultStems();
		} catch (StemLeafException e) {
			System.err.println("Not enough values to specify stem & leaf axis");
			setDefaultStems();
		}
		setMinMax();
	}
	
	private void setDefaultStems() {
		minStem = 0;
		minStemNeg = false;
		repeatsPerStem = 1;
		minStemRepeat = 0;
		noOfBins = 10;
		stemPower = 0;
		leavesPerBin = 10;
		setMinMax();
	}
	
	public boolean changeNoOfBins(boolean moreNotLess, int maxBins, double minDisplayVal,
																							double maxDisplayVal) {
		int oldStemPower = stemPower;
		int oldRepeatsPerStem = repeatsPerStem;
		int oldLeavesPerBin = leavesPerBin;
		boolean oldMinStemNeg = minStemNeg;
		int oldNoOfBins = noOfBins;
		if (moreNotLess)
			switch (repeatsPerStem) {
				case 1:
					repeatsPerStem = 2;
					leavesPerBin = 5;
					break;
				case 2:
					repeatsPerStem = 5;
					leavesPerBin = 2;
					break;
				case 5:
					repeatsPerStem = 1;
					leavesPerBin = 10;
					stemPower--;
					break;
			}
		else
			switch (repeatsPerStem) {
				case 1:
					repeatsPerStem = 5;
					leavesPerBin = 2;
					stemPower++;
					break;
				case 2:
					repeatsPerStem = 1;
					leavesPerBin = 10;
					break;
				case 5:
					repeatsPerStem = 2;
					leavesPerBin = 5;
					break;
			}
		int minDisplaySL = findStemAndLeaf(new NumValue(minDisplayVal));
		int maxDisplaySL = findStemAndLeaf(new NumValue(maxDisplayVal));
		
		minStemNeg = minDisplaySL < 0;
		int lowBin = minStemNeg ? (minDisplaySL - leavesPerBin + 1) / leavesPerBin
													: minDisplaySL / leavesPerBin;		//	round down
		int highBin = (maxDisplaySL < 0) ? (maxDisplaySL - leavesPerBin + 1) / leavesPerBin
													: maxDisplaySL / leavesPerBin;		//	round down
		noOfBins = highBin - lowBin + 1;
		
		if (noOfBins == 1 || noOfBins > maxBins) {
			stemPower = oldStemPower;
			repeatsPerStem = oldRepeatsPerStem;
			leavesPerBin = oldLeavesPerBin;
			minStemNeg = oldMinStemNeg;
			noOfBins = oldNoOfBins;
			return false;
		}
		minStem = minStemNeg ? (minDisplaySL - 9) / 10 : minDisplaySL / 10;		//	round down
		minStemRepeat = lowBin % repeatsPerStem;
		
		setMinMax();
		return true;
	}
	
	private void setMinMax() {
		minStemAndLeaf = minStem * 10 + minStemRepeat * leavesPerBin;		//	-0:0  --> -1
		maxStemAndLeaf = minStemAndLeaf + noOfBins * leavesPerBin - 1;
	}
	
	private static final int kExtraPower = 5;
	private static final int kReduceFactor = 100000;	//	values are rounded 5 places after leaf digit
																		//	otherwise 1.9 may be truncated to 1.8
	
	protected int findStemAndLeaf(NumValue theValue) {
		if (Double.isNaN(theValue.toDouble()))
			return kNaNLeaf;
		double scaleFactor = Math.pow(10.0, -stemPower + 1 + kExtraPower);
		long scaledVal = Math.round(theValue.toDouble() * scaleFactor);
		return (int)(scaledVal / kReduceFactor) - ((scaledVal < 0) ? 1 : 0);
	}
	
	protected Image getLeaf(int stemAndLeaf, int color) {
		int leaf = (stemAndLeaf >= 0) ? stemAndLeaf % 10 : -((stemAndLeaf + 1) % 10);
		return LeafDigitImages.digit[color][leaf];
	}
	
	protected Image getLeaf(int stemAndLeaf) {
		return getLeaf(stemAndLeaf, LeafDigitImages.BLACK_DIGITS);
	}
	
	protected Image getLeaf(int theRepeat, int indexInRepeat, boolean neg) {
		int leafInt = neg ? 9 - theRepeat * leavesPerBin - indexInRepeat
													: theRepeat * leavesPerBin + indexInRepeat;
		return LeafDigitImages.digit[LeafDigitImages.BLACK_DIGITS][leafInt];
	}
	
	protected int getStem(int stemAndLeaf) {
		if (stemAndLeaf >= 0)
			return stemAndLeaf / 10;
		else if (stemAndLeaf >= -9)
			return -1;
		else
			return (stemAndLeaf + 1) / 10;
	}
	
	private int countStemChars(int stemAndLeaf) {
		int tempVal = stemAndLeaf;
		int chars = 0;
		if (minStemAndLeaf < 0) {
			chars = 1;
			tempVal = -tempVal;
		}
		if (tempVal < 10)
			return chars + 1;
		while (tempVal >= 10) {
			chars ++;
			tempVal /= 10;
		}
		return chars;
	}
	
	protected void setMaxStemWidth() {
		int chars = Math.max(countStemChars(minStemAndLeaf), countStemChars(maxStemAndLeaf));
		
		stemWidth = chars * (LeafDigitImages.kDigitWidth + LeafDigitImages.kHorizSpace);
		leafStart = kLeftOffset + stemWidth + 2 * kLineGap + 1;
	}
	
	public int getHeadingHt(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return kTopBorder + kUnitsSpace + Math.max(LeafDigitImages.kDigitHeight, fm.getAscent()) + fm.getDescent();
	}
	
	public int getDigitWidth() {
		return LeafDigitImages.getWidth();
	}
	
	protected void drawStem(Graphics g, int currentStem, int stemVert, int stemRight,
																			int color, DataView theView) {
		boolean negative = currentStem < 0;
		if (negative)
			currentStem = -1 - currentStem;		//	-1 becomes 0, -2 becomes 1
		
		Image digit[] = LeafDigitImages.digit[color];
		
		int charLeftPos = stemRight - LeafDigitImages.kDigitWidth;
		if (currentStem <= 9) {
			g.drawImage(digit[currentStem], charLeftPos, stemVert - LeafDigitImages.kDigitHeight, theView);
			charLeftPos -= getDigitWidth();
		}
		else
			while (currentStem > 0) {
				int ch = currentStem % 10;
				g.drawImage(digit[ch], charLeftPos, stemVert - LeafDigitImages.kDigitHeight, theView);
				charLeftPos -= getDigitWidth();
				currentStem /= 10;
			}
		
		if (negative) {
			Image minus = (color == LeafDigitImages.GREY_DIGITS) ? LeafDigitImages.minusGrey
																					: LeafDigitImages.minusBlue;
			g.drawImage(minus, charLeftPos, stemVert - LeafDigitImages.kDigitHeight, theView);
		}
	}
	
	protected void drawStem(Graphics g, int currentStem, int stemVert, int stemRight, DataView theView) {
		drawStem(g, currentStem, stemVert, stemRight, LeafDigitImages.BLUE_DIGITS, theView);
	}
	
	protected void drawLeaf(Graphics g, Image leaf, int currentCharPos, int stemVert, DataView theView) {
		g.drawImage(leaf, currentCharPos, stemVert - LeafDigitImages.kDigitHeight, theView);
	}
	
	protected void drawHeading(Graphics g, int offset, NumValue testValue, DataView theView) {
		int stemLeafInt = findStemAndLeaf(testValue);
		int stem = getStem(stemLeafInt);
		Image leaf = getLeaf(stemLeafInt, LeafDigitImages.RED_DIGITS);
		
		double stemLeaf = stemLeafInt * 0.1;
		if (stemPower > 0)
			for (int i=0 ; i<stemPower ; i++)
				stemLeaf *= 10.0;
		else if (stemPower < 0)
			for (int i=0 ; i<-stemPower ; i++)
				stemLeaf *= 0.1;
		
		NumValue value = new NumValue(stemLeaf, Math.max(1-stemPower, 0));
		
//		FontMetrics fm = g.getFontMetrics();
//		Color oldColor = g.getColor();
		g.setColor(Color.red);
		
		int horiz = offset + kUnitsOffset + kLeftOffset + stemWidth;
		int baseline = kTopBorder + LeafDigitImages.kDigitHeight;
		drawStem(g, stem, baseline, horiz, LeafDigitImages.RED_DIGITS, theView);
		
		g.drawLine(horiz+2, kTopBorder - 2, horiz+2, baseline + 2);
		horiz += 5;
		g.drawImage(leaf, horiz, baseline - LeafDigitImages.kDigitHeight, theView);
		horiz += LeafDigitImages.kDigitWidth + 2 * LeafDigitImages.kHorizSpace;
		
		String representsString = (unitsString == null) ? (" " + applet.translate("represents value") + " " + value.toString())
												: (" " + applet.translate("represents") + " " + value.toString() + unitsString);
		g.setColor(Color.red);
		g.drawString(representsString, horiz, baseline);
		g.setColor(theView.getForeground());
	}
}