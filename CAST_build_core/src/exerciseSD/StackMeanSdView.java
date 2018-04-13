package exerciseSD;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import formula.*;


public class StackMeanSdView extends StackedDotPlotView {
//	static public final String STACK_MEAN_SD = "StackMeanSd";
	
	static final private int kArrowHeight = 36;
	static final private int kArrowHead = 4;
	static final private int kMeanSdGap = 5;
	
	private int decimals;
	private char xChar = 'x';
	
	private boolean drawSd = true;
	
	public StackMeanSdView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey, int decimals) {
		super(theData, applet, theAxis, null, false);
		setActiveNumVariable(yKey);
		this.decimals = decimals;
		setFont(applet.getBigFont());
	}
	
	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}
	
	public void setDrawSd(boolean drawSd) {
		this.drawSd = drawSd;
	}
	
	public void setXChar(char xChar) {
		this.xChar = xChar;
	}
	
	public NumValue getMean() {
		ValueEnumeration ye = getNumVariable().values();
		int n = 0;
		double sy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			if (!Double.isNaN(y)) {
				sy += y;
				n ++;
			}
		}
		return new NumValue(sy / n, decimals);
	}
	
	public NumValue getSD() {
		ValueEnumeration ye = getNumVariable().values();
		int n = 0;
		double sy = 0.0;
		double syy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			if (!Double.isNaN(y)) {
				sy += y;
				syy += y * y;
				n ++;
			}
		}
		return new NumValue(Math.sqrt((syy - sy * sy / n) / (n - 1)), decimals);
	}
	
	protected void paintBackground(Graphics g) {
		NumValue meanVal = getMean();
		
		int meanPos = axis.numValToRawPosition(meanVal.toDouble());
		
		int arrowHeight = Math.min(kArrowHeight, getSize().height - getMaxStackHeight() - kMeanSdGap
																											- g.getFontMetrics().getAscent() - 7);
		int arrowBottom = getSize().height - arrowHeight - kMeanSdGap
																							- g.getFontMetrics().getAscent() - 7;
//		int arrowBottom = Math.min(getMaxStackHeight(),
//										getSize().height - arrowHeight - kMeanSdGap - g.getFontMetrics().getAscent() - 7);
		
		Point p1 = translateToScreen(meanPos, arrowBottom + arrowHeight / 2, null);
		Point p2 = null;
		if (drawSd) {
			NumValue sdVal = getSD();
			int meanPlusSdPos = axis.numValToRawPosition(meanVal.toDouble() + sdVal.toDouble());
			
			p2 = translateToScreen(meanPlusSdPos, arrowBottom + arrowHeight / 2, null);
			g.setColor(Color.red);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			LabelValue sdLabel = new LabelValue("s = " + sdVal);
			int sdWidth = sdLabel.stringWidth(g);
			int sdCentre = Math.max(2 + p1.x + sdWidth / 2, (p1.x + p2.x) / 2);
					
			sdLabel.drawCentred(g, sdCentre, p1.y - kMeanSdGap);
			
			p1 = translateToScreen(meanPlusSdPos - kArrowHead, arrowBottom + arrowHeight / 2 + kArrowHead, p1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			p1 = translateToScreen(meanPlusSdPos - kArrowHead, arrowBottom + arrowHeight / 2 - kArrowHead, p1);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	
		g.setColor(Color.blue);
		p1 = translateToScreen(meanPos, arrowBottom + arrowHeight, p1);
		p2 = translateToScreen(meanPos, arrowBottom, p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		LabelValue meanLabel = new LabelValue(MText.expandText(xChar + "#bar# = ") + meanVal);
		meanLabel.drawCentred(g, p1.x, p1.y - kMeanSdGap);
		
		p1 = translateToScreen(meanPos - kArrowHead, arrowBottom + kArrowHead, p1);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		p1 = translateToScreen(meanPos + kArrowHead, arrowBottom + kArrowHead, p1);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		
		g.setColor(getForeground());
	}
}