package sampling;

import java.awt.*;
import java.util.*;

import dataView.*;
import imageUtils.*;

import samplingProg.*;


public class RandomDigitsPanel extends XPanel implements RandDigActionInterface {
	static final public int NO_DECIMALS = 0;
	static final public int DECIMALS = 1;
	
	static final public int LARGE_DIGITS = 0;
	static final public int SMALL_DIGITS = 1;
	
	static final private Color kStudentBackground = new Color(0xFFCC99);
	static final private Color kLecturerBackground = new Color(0xF0E4BE);
	
	private CoreRandomDigit digits[];
	
	private boolean digitReady[];
	
	private RandomDigitProgInterface valueAction;
	
	public RandomDigitsPanel(XApplet applet, RandomDigitProgInterface valueAction,
													int decimals, String seedString, int digitSize, int maxFirstDigit) {
		this.valueAction = valueAction;
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
		
		if (decimals == DECIMALS) {
			if (digitSize == LARGE_DIGITS) {
				Color background = applet.getBackground();
				int studentDist = colorDistance(background, kStudentBackground);
				int lecturerDist = colorDistance(background, kLecturerBackground);
				Image zpi = (studentDist < lecturerDist) ? DigitImages.zeroPointImage : DigitImages.zeroPointLecturerImage;
				add(new ImageCanvas(zpi, DigitImages.kZeroPointWidth, DigitImages.kZeroPointHeight, applet));
				
			}
			else {
				MiniZeroPointCanvas zeroPt = new MiniZeroPointCanvas();
				zeroPt.setFont(applet.getStandardFont());
				add(zeroPt);
			}
		}
		
		long seeds[] = readSeeds(seedString);
		digits = new CoreRandomDigit[seeds.length];
		digitReady = new boolean[seeds.length];
		for (int i=0 ; i<seeds.length ; i++) {
			int maxDigit = (i == 0) ? maxFirstDigit : 9;
			if (digitSize == LARGE_DIGITS)
				digits[i] = new RandomDigit(maxDigit, applet, this, seeds[i]);
			else {
				digits[i] = new MiniRandomDigit(maxDigit, applet, this, seeds[i]);
				digits[i].setFont(applet.getStandardFont());
			}
			add(digits[i]);
			digitReady[i] = true;
		}
	}
	
	public RandomDigitsPanel(XApplet applet, RandomDigitProgInterface valueAction,
																											int decimals, String seedString) {
		this(applet, valueAction, decimals, seedString, LARGE_DIGITS, 9);
	}
	
	private int colorDistance(Color c1, Color c2) {
		int dist = 0;
		dist += Math.abs(c1.getRed() - c2.getRed());
		dist += Math.abs(c1.getGreen() - c2.getGreen());
		dist += Math.abs(c1.getBlue() - c2.getBlue());
		return dist;
	}
	
	public int getValue() {
		int value = 0;
		for (int i=0 ; i<digits.length ; i++)
			value = value * 10 + digits[i].getDigit();
		return value;
	}
	
	public double getDecimalValue() {
		double value = getValue();
		for (int i=0 ; i<digits.length ; i++)
			value *= 0.1;
		return value;
	}
	
	private long[] readSeeds(String seedString) {
		StringTokenizer st = new StringTokenizer(seedString);
		int noOfSeeds = 0;
		while (st.hasMoreTokens()) {
			noOfSeeds ++;
			st.nextToken();
		}
		long seeds[] = new long[noOfSeeds];
		st = new StringTokenizer(seedString);
		for (int i=0 ; i<noOfSeeds ; i++)
			seeds[i] = Long.parseLong(st.nextToken());
		
		return seeds;
	}
	
//---------------------------------------------------------------------
	
	public void animateNextDigits(int[] millisecPerFrame) {
		for (int i=0 ; i<digits.length ; i++)
			digits[i].animateNextDigit(millisecPerFrame[i]);
	}
	
//---------------------------------------------------------------------
	
	public void noteNewDigit(CoreRandomDigit theDigit) {
		boolean allDigitsReady = true;
		for (int i=0 ; i<digits.length ; i++) {
			if (digits[i] == theDigit)
				digitReady[i] = true;
			allDigitsReady = allDigitsReady && digitReady[i];
		}
		
		if (allDigitsReady)
			valueAction.noteNewValue(this);
	}
	
	public void noteClearedDigit(CoreRandomDigit theDigit) {
		boolean newlyCleared = true;
		for (int i=0 ; i<digits.length ; i++) {
			if (!digitReady[i])
				newlyCleared = false;
			if (digits[i] == theDigit)
				digitReady[i] = false;
		}
		if (newlyCleared)
			valueAction.noteClearedValue();
	}
}