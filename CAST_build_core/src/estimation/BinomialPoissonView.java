package estimation;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import formula.*;

import distribution.*;


public class BinomialPoissonView extends DiscreteProbView {
	static final private Color kDistnTextColor = new Color(0xCC0000);
	
	private String binomialString;
	private NumValue lambda;
	
	public BinomialPoissonView(DataSet theData, XApplet applet, String distnKey, HorizAxis countAxis,
																											NumValue lambda, String binomialString) {
		super(theData, applet, distnKey, null, countAxis, DiscreteProbView.NO_DRAG);
		this.lambda = lambda;
		this.binomialString = binomialString;
		setFont(applet.getBigBoldFont());
	}
	
	
	protected void drawTitleString(Graphics g) {
		BinomialDistnVariable binom = (BinomialDistnVariable)getData().getVariable(distnKey);
		int n = binom.getCount();
		
		g.setColor(kDistnTextColor);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int baseline = ascent + ascent / 2 + 1;
		
		String s1 = binomialString + "(n = " + n + MText.expandText(", #pi# = ");
		int s1Width = fm.stringWidth(s1);
		String s2 = " )";
		int s2Width = fm.stringWidth(s2);
		
		int lambdaWidth = lambda.stringWidth(g);
		NumValue nValue = new NumValue(n, 0);
		int nWidth = nValue.stringWidth(g);
		int fractionWidth = Math.max(lambdaWidth, nWidth);
		
		int overallWidth = s1Width + s2Width + fractionWidth;
		int left = getSize().width - overallWidth - 4;
		g.drawString(s1, left, baseline);
		
		left += fm.stringWidth(s1);
		lambda.drawCentred(g, left + fractionWidth / 2, baseline - ascent / 2 - 2);
		nValue.drawCentred(g, left + fractionWidth / 2, baseline + ascent / 2 + 2);
		g.drawLine(left, baseline - ascent / 2, left + fractionWidth, baseline - ascent / 2);
		
		left += fractionWidth;
		g.drawString(s2, left, baseline);
		
		g.setColor(getForeground());
	}
}