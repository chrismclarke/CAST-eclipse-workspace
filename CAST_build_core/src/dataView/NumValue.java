package dataView;

import java.awt.*;
import java.lang.Math;



public class NumValue extends Value {
	static final private String kNaNString = "?";
	static final private int kNaNStringLength = kNaNString.length();
	static final private String kPosInfinityString = "\u221E";
	static final private String kNegInfinityString = "-\u221E";
	
	static final public NumValue NAN_VALUE = new NumValue(Double.NaN, 0);
	static final public NumValue POS_INFINITY_VALUE = new NumValue(Double.POSITIVE_INFINITY, 0);
	static final public NumValue NEG_INFINITY_VALUE = new NumValue(Double.NEGATIVE_INFINITY, 0);
	
	private double value;
	public int decimals;
	protected int leftDigits;
	
	public NumValue(double theValue) {
		value = init(theValue, 10);
	}
	
	public NumValue(double theValue, int theDecimals) {
		value = init(theValue, theDecimals);
	}
	
	public NumValue(NumValue v) {
		value = init(v.value, v.decimals);
	}
	
	public NumValue(String valString) throws NullPointerException, NumberFormatException {
		value = readValueFromString(valString);
	}
	
	protected NumValue() {			//		only used by dynamic.NumSeriesValue
	}
	
	protected double readValueFromString(String valString) throws NullPointerException, NumberFormatException {
		if (valString == null)
			throw new NullPointerException();
		else if (valString.equals(kNaNString))
			return setupNaN();
		else if (valString.equals(kPosInfinityString))
			return Double.POSITIVE_INFINITY;
		else if (valString.equals(kNegInfinityString))
			return Double.NEGATIVE_INFINITY;
		else {
			initString(valString);
			scanSpaces();
			int decimals = 0;
			double theValue = 0.0;
			
			boolean negative = scanCharThenSpaces('-');
			if (!negative)
				scanCharThenSpaces('+');
			
			if (moreToGo()) {
				int units = 0;
				Integer theDigit;
				while ((units < Integer.MAX_VALUE) && ((theDigit = scanDigit()) != null))
					units = units*10 + theDigit.intValue();
				theValue = units;
				
				if (scanChar('.')) {
					int decimalVal = 0, factor = 1;
					while ((factor < (Integer.MAX_VALUE/10)) && ((theDigit = scanDigit()) != null)) {
						decimalVal = decimalVal*10 + theDigit.intValue();
						factor = factor*10;
						decimals++;
					}
					theValue = theValue + decimalVal/((double)factor);
				}
				
				if (scanCharThenSpaces('e') || scanCharThenSpaces('E')) {
					boolean negExponent = scanCharThenSpaces('-');
					int exponent = 0;
					if (!negExponent)
						scanCharThenSpaces('+');
					
					while ((exponent < Integer.MAX_VALUE) && ((theDigit = scanDigit()) != null))
						exponent = exponent*10 + theDigit.intValue();
					if (negExponent)
						exponent = -exponent;
					theValue = theValue * Math.pow(10.0, exponent);
					decimals = Math.max(decimals - exponent, 0);
				}
				
				scanSpaces();
				if (moreToGo())
					throw new NumberFormatException();
			}
			deleteString();
			if (negative)
				theValue = -theValue;
				
			return init(theValue, decimals);
		}
	}
	
	private double setupNaN() {
		decimals = 0;
		leftDigits = kNaNStringLength;
		return Double.NaN;
	}
	
	private double init(double theValue, int theDecimals) {
		if (Double.isNaN(theValue))
			return setupNaN();
		else if (Double.isInfinite(theValue))
			return theValue;
		else {
			decimals = theDecimals;
			double temp = Math.abs(theValue);
			leftDigits = (theValue < 0.0) ? 1 : 0;
			while (temp >= 1.0) {
				leftDigits++;
				temp /= 10.0;
			}
			return theValue;
		}
	}

//--------------------------------------------------------------------------

	public void setValue(double value) {
		this.value = value;
	}
	
	public double toDouble() {
		return value;
	}
	
	public String toString() {
		ParsedValue theParsedValue = parseValue(decimals);
		return theParsedValue.leftDigits + theParsedValue.rightDigits;
	}
	
	public void drawAtPoint(Graphics g, int x, int y) {
		drawAtPoint(g, decimals, x, y);
	}

//--------------------------------------------------------------------------
	
	public void drawAtPoint(Graphics g, int drawDecs, int x, int y) {
		ParsedValue theParsedValue = parseValue(drawDecs);
		
		FontMetrics fMetrics = g.getFontMetrics();
		int leftLength = fMetrics.stringWidth(theParsedValue.leftDigits);
		
		g.drawString(theParsedValue.leftDigits + theParsedValue.rightDigits, x - leftLength, y);
	}
	
	public void drawRight(Graphics g, int drawDecs, int x, int y) {
		ParsedValue theParsedValue = parseValue(drawDecs);
		
		g.drawString(theParsedValue.leftDigits + theParsedValue.rightDigits, x, y);
	}
	
	public void drawCentred(Graphics g, int drawDecs, int x, int y) {
		ParsedValue theParsedValue = parseValue(drawDecs);
		String wholeValue = theParsedValue.leftDigits + theParsedValue.rightDigits;
		
		FontMetrics fMetrics = g.getFontMetrics();
		int wholeLength = fMetrics.stringWidth(wholeValue);
		
		g.drawString(wholeValue, x - wholeLength / 2, y);
	}
	
	public void drawLeft(Graphics g, int drawDecs, int x, int y) {
		ParsedValue theParsedValue = parseValue(drawDecs);
		String wholeValue = theParsedValue.leftDigits + theParsedValue.rightDigits;
		
		FontMetrics fMetrics = g.getFontMetrics();
		int wholeLength = fMetrics.stringWidth(wholeValue);
		
		g.drawString(wholeValue, x - wholeLength, y);
	}
	
	public int stringWidth(Graphics g, int drawDecs) {
		ParsedValue theParsedValue = parseValue(drawDecs);
		String wholeValue = theParsedValue.leftDigits + theParsedValue.rightDigits;
		
		FontMetrics fMetrics = g.getFontMetrics();
		return fMetrics.stringWidth(wholeValue);
	}

	public String toString(int drawDecs) {
		ParsedValue theParsedValue = parseValue(drawDecs);
		return theParsedValue.leftDigits + theParsedValue.rightDigits;
	}

//--------------------------------------------------------------------------
	
	public void drawWithCommas(Graphics g, int x, int y) {
		String valueString = getValueStringWithCommas();
		if (valueString !=  null) {
			FontMetrics fm = g.getFontMetrics();
			int wholeLength = fm.stringWidth(valueString);
			
			g.drawString(valueString, x - wholeLength, y);
		}
	}
	
	public int stringWidthWithCommas(Graphics g) {
		String valueString = getValueStringWithCommas();
		if (valueString == null)
			return 0;
		else
			return g.getFontMetrics().stringWidth(valueString);
	}
	
	public String getValueStringWithCommas() {
		ParsedValue parsedWithoutCommas = parseValue(decimals);
		StringBuffer sb = new StringBuffer(parsedWithoutCommas.leftDigits);
		sb.reverse();
		for (int index=3 ; index<sb.length() ; index+=4)
			sb.insert(index, ',');
		sb.reverse();
		return sb.toString() + parsedWithoutCommas.rightDigits;
		
/*		
		long posIntVal = Math.round(value);
		if (posIntVal < 0)
			return null;
		else if (posIntVal < 1000)
			return toString();
		else {
			StringBuffer digitBuffer = new StringBuffer(20);
			int commaPos = 3;
			while (posIntVal > 0) {
				if (commaPos == 0) {
					digitBuffer.append(',');
					commaPos = 3;
				}
				digitBuffer.append((char)('0' + posIntVal % 10));
				posIntVal /= 10;
				commaPos --;
			}
			
			digitBuffer.reverse();
			return digitBuffer.toString();
		}
*/
	}

//--------------------------------------------------------------------------
	
	protected ParsedValue parseValue(int drawDecs) {
		ParsedValue theParsedValue = new ParsedValue();
		if (Double.isNaN(toDouble())) {
			theParsedValue.leftDigits = kNaNString;
			theParsedValue.rightDigits = "";
		}
		else if (Double.isInfinite(toDouble())) {
			theParsedValue.leftDigits = (toDouble() < 0.0) ? kNegInfinityString : kPosInfinityString;
			theParsedValue.rightDigits = "";
		}
		else {
			boolean negative = false;
			double tempVal = toDouble();
			if (tempVal < 0.0) {
				tempVal = -tempVal;
				negative = true;
			}
			
			double roundHalf = 0.5;
			for (int i=0 ; i<drawDecs ; i++)
				roundHalf /= 10.0;
			tempVal += roundHalf;			//		otherwise 0.999999 is truncated at final pos
			
			long posIntVal = Math.round(tempVal - 0.5);	//		round down
			long intVal = negative ? -posIntVal : posIntVal;
			
			StringBuffer digitBuffer = new StringBuffer(20);
			boolean zeroDecimals = true;
			tempVal = tempVal - posIntVal;
			if (drawDecs > 0) {
				digitBuffer.append('.');
				while (drawDecs > 0) {
					tempVal *= 10.0;
					if (drawDecs == 1) {
						long digit = Math.round(tempVal - 0.5);
						if (digit != 0)
							zeroDecimals = false;
						digitBuffer.append(digit);
					}
					else {
						long wholePart = Math.round(tempVal - 0.5);		//		round down
						if (wholePart != 0)
							zeroDecimals = false;
						digitBuffer.append(wholePart);
						tempVal -= wholePart;
					}
					drawDecs--;
				}
			}
			if (intVal == 0 && zeroDecimals)
				negative = false;
			if (negative && intVal == 0)
				theParsedValue.leftDigits = "-0";
			else
				theParsedValue.leftDigits = String.valueOf(intVal);
			
			theParsedValue.rightDigits = digitBuffer.toString();
		}
		return theParsedValue;
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof NumValue))
			return false;
			
		NumValue objValue = (NumValue)obj;
		return (value == objValue.value) && (decimals == objValue.decimals);
	}
}