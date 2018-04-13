package logistic;

import java.awt.*;

import dataView.*;
import regn.*;


public class LogisticEquationView extends LinearEquationView {
	
	static final private int kHorizLineHeight = 5;
	static final private int kBracketGap = 3;
	
	private String successName;
	private int topBaseline, bottomBaseline, mainBaseline;
	private int exponWidth, fractionWidth;
	
	private String kExpString, kPrString;
	
	public LogisticEquationView(DataSet theData, XApplet applet, String modelKey, String successName,
								String xName, NumValue minIntercept, NumValue maxIntercept, NumValue minSlope,
								NumValue maxSlope, Color interceptColor, Color slopeColor) {
		super(theData, applet, modelKey, "", xName, minIntercept, maxIntercept,
																			minSlope, maxSlope, interceptColor, slopeColor);
		this.successName = successName;
		kExpString = applet.translate("exp");
		kPrString = applet.translate("Pr");
	}
	
	public LogisticEquationView(DataSet theData, XApplet applet, String modelKey,
							String successName, String xName, NumValue minIntercept, NumValue maxIntercept,
							NumValue minSlope, NumValue maxSlope) {
		this(theData, applet, modelKey, successName, xName, minIntercept, maxIntercept,
																													minSlope, maxSlope, null, null);
	}

//--------------------------------------------------------------------------------
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			FontMetrics fm = g.getFontMetrics();
			int leftWidth = fm.stringWidth(getApplet().translate("Pr") + "() =  " + successName);
			
			exponWidth = fm.stringWidth(kExpString + "()") + linearWidth + 2 * kBracketGap;
			fractionWidth = exponWidth + fm.stringWidth("1 + ");
			
			topBaseline = getValueBaseline(fm);
			bottomBaseline = paramSize[0].height + kHorizLineHeight + topBaseline;
			
			modelHeight = 2 * paramSize[0].height + kHorizLineHeight;
			modelWidth = 2 * kLeftRightBorder + leftWidth + exponWidth + fm.stringWidth("1 + ");
			
			mainBaseline = (modelHeight + fm.getAscent() - fm.getDescent()) / 2;
			return true;
		}
		else
			return false;
	}
	
	private int drawExponPart(Graphics g, int horizPos, int baseline) {
		FontMetrics fm = g.getFontMetrics();
		g.drawString(kExpString + "(", horizPos, baseline);
		horizPos += fm.stringWidth(kExpString + "(") + kBracketGap;
		horizPos = drawLinearPart(g, horizPos, baseline) + kBracketGap;
		g.drawString(")", horizPos, baseline);
		horizPos += fm.stringWidth(")");
		return horizPos;
	}
	
	public int paintModel(Graphics g) {
//		Color oldColor = g.getColor();
		FontMetrics fm = g.getFontMetrics();
		int horizPos = kLeftRightBorder;
		
		String startString = kPrString + "(" + successName + ") =  ";
		g.drawString(startString, horizPos, mainBaseline);
		horizPos += fm.stringWidth(startString);
		
		drawExponPart(g, horizPos + (fractionWidth - exponWidth) / 2, topBaseline);
		
		g.drawLine(horizPos, paramSize[0].height + kHorizLineHeight / 2,
							horizPos + fractionWidth - 1, paramSize[0].height + kHorizLineHeight / 2);
		
		g.drawString("1 + ", horizPos, bottomBaseline);
		horizPos += fm.stringWidth("1 + ");
		drawExponPart(g, horizPos, bottomBaseline);
		
		return horizPos;
	}
}
