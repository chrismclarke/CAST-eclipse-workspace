package regn;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import utils.*;
import models.*;


public class ResidSsqView extends ValueView {
//	static public final String RESID_SSQ = "residSsq";
	
	static final protected int kDefaultDecimals = 3;
	static final private String kEqualsString = " =";
	
	private String residString;
	
	protected String xKey, yKey, lineKey;
	protected NumValue biggestRss;
	
	public ResidSsqView(DataSet theData, String xKey, String yKey,
												String lineKey, NumValue biggestRss, XApplet applet) {
		super(theData, applet);
		this.xKey = xKey;
		this.yKey = yKey;
		this.lineKey = lineKey;
		this.biggestRss = biggestRss;
		residString = applet.translate("resid");
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth(residString + kEqualsString);
		width += Sigma.getWidth(g);
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2));
		FontMetrics fms = g.getFontMetrics();
		width += fms.stringWidth("2") + 2;
		g.setFont(oldFont);
		return width;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return biggestRss.stringWidth(g);
	}
	
	protected int getLabelAscent(Graphics g) {
		return Sigma.getAbove(g);
	}
	
	protected int getLabelDescent(Graphics g) {
		return Sigma.getBelow(g);
	}
	
	protected String getValueString() {
		NumVariable x = (NumVariable)getVariable(xKey);
		NumVariable y = (NumVariable)getVariable(yKey);
		LinearModel model = (LinearModel)getVariable(lineKey);
		ValueEnumeration xe = x.values();
		ValueEnumeration ye = y.values();
		double rss = 0.0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double xVal = xe.nextDouble();
			double yVal = ye.nextDouble();
			double resid = yVal - model.evaluateMean(xVal);
			rss += resid * resid;
		}
		return (new NumValue(rss, biggestRss.decimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		FontMetrics fm = g.getFontMetrics();
		
		Sigma.drawSigma(g, startHoriz, baseLine);
		startHoriz += Sigma.getWidth(g);
		g.drawString(residString, startHoriz, baseLine);
		startHoriz += fm.stringWidth(residString) + 2;
		
		Font oldFont = g.getFont();
		g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2));
		FontMetrics fms = g.getFontMetrics();
		g.drawString("2", startHoriz, baseLine - 5);
		startHoriz += fms.stringWidth("2");
		g.setFont(oldFont);
		
		g.drawString(kEqualsString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
