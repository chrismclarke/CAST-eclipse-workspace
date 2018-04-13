package qnUtils;

import java.awt.*;

import dataView.*;
import coreSummaries.*;
import imageGroups.*;


abstract public class HypothesisTest extends CoreHypothesisTest {
	static final public int HA_LOW = 0;
	static final public int HA_HIGH = 1;
	static final public int HA_NOT_EQUAL = 2;
	
	static final public int CUSTOM = -1;
	static final public int MEAN = 0;
	static final public int PROPN = 1;
	static final public int PROPN_APPROX = 2;
	static final public int MEAN_KNOWN_SIGMA = 3;
	static final public int SLOPE = 4;
	static final public int DIFF_MEAN = 5;
	static final public int DIFF_PROPN = 6;
	static final public int PROPN_2 = 7;		//		For 1-tailed test, gives 0.5 if n1 = n2
	static final public int PAIRED_DIFF_MEAN = 8;
	
	static final public int PARAM_DRAW = 0;
	static final public int PARAM_BOUNDARY_DRAW = 1;
	static final public int GENERIC_DRAW = 2;
	
	static final private int kValueGap = 3;
	
	protected int testTail, paramType;
	protected NumValue testValue;
	
	protected DataSet data;
	
	private boolean compositeNull = false;
	
	public HypothesisTest(DataSet data, NumValue testValue, int testTail, int paramType,
																									XApplet applet) {
		super(applet);
		ScalesImages.loadScales(applet);
		this.testValue = testValue;
		this.testTail = testTail;
		this.paramType = paramType;
		this.data = data;
	}

//------------------------------------------------------------------
	
	abstract public double evaluateStatistic();
	abstract public double evaluatePValue();
	abstract protected Image getParamImage();

//------------------------------------------------------------------
	
	public NumValue getTestValue() {
		return testValue;
	}
	
	public void setTestValue(NumValue testValue) {
		this.testValue = testValue;
	}
	
	public int getTestTail() {
		return testTail;
	}
	
	public void setTestTail(int tail) {
		testTail = tail;
	}
	
	public void setCompositeNull(boolean compositeNull) {
		this.compositeNull = compositeNull;
	}
	
	public Dimension getSize(Graphics g, int drawType) {
		if (drawType == GENERIC_DRAW)
			return new Dimension(ScalesImages.kGenericWidth, ScalesImages.kGenericHeight);
		else {
			Font oldFont = g.getFont();
			g.setFont(applet.getBigBoldFont());
//			int paramWidth = ScalesImages.kParamWidth;
			int paramWidth = getParamImage().getWidth(applet);
			int width = paramWidth + ScalesImages.kSignWidth + kValueGap + testValue.stringWidth(g);
			g.setFont(oldFont);
			return new Dimension(width, ScalesImages.kHeight);
		}
	}
	
	public int getBaselineFromTop(Graphics g, int drawType) {
		return (drawType == GENERIC_DRAW) ? ScalesImages.kGenericBaselineFromTop
																					: ScalesImages.kBaselineFromTop;
	}
	
	private Image getSignImage(boolean showNull, int testTail) {
		switch (testTail) {
			case HA_LOW:
				if (showNull) {
					if (compositeNull) {
						if (paramType == PROPN_2)
							return ScalesImages.greater;
						else
							return ScalesImages.greaterEquals;
					}
					else
						return ScalesImages.equals;
				}
				else
					return ScalesImages.less;
			case HA_HIGH:
				if (showNull) {
					if (compositeNull) {
						if (paramType == PROPN_2)
							return ScalesImages.less;
						else
							return ScalesImages.lessEquals;
					}
					else
						return ScalesImages.equals;
				}
				else
					return ScalesImages.greater;
			case HA_NOT_EQUAL:
				if (showNull)
					return ScalesImages.equals;
				else
					return ScalesImages.notEquals;
		}
		return null;
	}
	
	public void paintBlue(Graphics g, int left, int baseline, boolean showNull,
																						int drawType, Component c) {
		if (drawType == GENERIC_DRAW) {
			Image hypothesisImage = showNull ? ScalesImages.h0 : ScalesImages.hA;
			g.drawImage(hypothesisImage, left, baseline - ScalesImages.kGenericBaselineFromTop, c);
		}
		else {
			Image paramImage = getParamImage();
			g.drawImage(paramImage, left, baseline - ScalesImages.kBaselineFromTop, c);
//			left += ScalesImages.kParamWidth;
			left += paramImage.getWidth(c);
			
			Image signImage = (drawType == PARAM_DRAW) ? getSignImage(showNull, testTail)
																	:	getSignImage(true, HA_NOT_EQUAL);
			g.drawImage(signImage, left, baseline - ScalesImages.kBaselineFromTop, c);
			left += ScalesImages.kSignWidth + kValueGap;
			
			Font oldFont = g.getFont();
			Color oldColor = g.getColor();
			g.setFont(applet.getBigBoldFont());
			g.setColor(Color.blue);
			
			testValue.drawRight(g, left, baseline);
			
			g.setFont(oldFont);
			g.setColor(oldColor);
		}
	}
}
	
