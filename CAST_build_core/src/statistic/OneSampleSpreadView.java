package statistic;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class OneSampleSpreadView extends DotPlotView {
	static final public Color kRangeColor = Color.red;
	static final public Color kIqrColor = Color.blue;
	static final public Color kSdColor = new Color(0x009900);
	static final private Color kNameColor = new Color(0x999999);
	
	static final private int kUnderLineGap = 5;
	static final private int kLineValueGap = 2;
	static final private int kLineHeight = 3;
	
	private int summaryDecimals;
	
	public OneSampleSpreadView(DataSet theData, XApplet applet, HorizAxis axis, String yKey,
																																	int summaryDecimals) {
		super(theData, applet, axis, 0.5);
		setActiveNumVariable(yKey);
		setCanDragCrosses(false);
		this.summaryDecimals = summaryDecimals;
	}
	
	private void drawSummary(Graphics g, Color c, double summary, String summaryLabel,
											int lowPos, int highPos, int arrowBottom) {
		g.setColor(c);
		g.drawLine(lowPos, arrowBottom, lowPos, getSize().height);
		g.drawLine(highPos, arrowBottom, highPos, getSize().height);
		for (int i=0 ; i<kLineHeight ; i++)
			g.drawLine(lowPos, arrowBottom - i, highPos, arrowBottom - i);
		int baseline = arrowBottom - kLineHeight - kLineValueGap;
		NumValue summaryVal = new NumValue(summary, summaryDecimals);
		LabelValue summaryEquals = new LabelValue(summaryLabel + summaryVal);
		summaryEquals.drawCentred(g, (lowPos + highPos) / 2, baseline);
	}
	
	protected void paintBackground(Graphics g) {
		NumVariable yVar = getNumVariable();
		
		Font stdFont = g.getFont();
		Font nameFont = new Font(stdFont.getName(), Font.PLAIN, stdFont.getSize() * 2);
		g.setFont(nameFont);
		g.setColor(kNameColor);
		int nameBaseline = (getSize().height + g.getFontMetrics().getAscent()) / 2;
		g.drawString(yVar.name, 10, nameBaseline);
		g.setFont(stdFont);
		int ascent = g.getFontMetrics().getAscent();
		
		BoxInfo boxInfo = new BoxInfo();
		boxInfo.initialiseBox(yVar.getSortedData(), false, axis);
		
		Point p = null;
		p = translateToScreen(boxInfo.boxPos[BoxInfo.LOW_QUART], 0, p);
		int lqPos = p.x;
		p = translateToScreen(boxInfo.boxPos[BoxInfo.HIGH_QUART], 0, p);
		int uqPos = p.x;
		
		int arrowBottom = getSize().height - getViewBorder().bottom - currentJitter - kUnderLineGap;
		
		double iqr = boxInfo.boxVal[BoxInfo.HIGH_QUART] - boxInfo.boxVal[BoxInfo.LOW_QUART];
		drawSummary(g, kIqrColor, iqr, "iqr = ", lqPos, uqPos, arrowBottom);
		
		ValueEnumeration ye = yVar.values();
		double sy = 0.0;
		double syy = 0.0;
		int n = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			syy += y * y;
			n ++;
		}
		double mean = sy / n;
		double sd = Math.sqrt((syy - sy * mean) / (n - 1));
		
		p = translateToScreen(axis.numValToRawPosition(mean), 0, p);
		int meanPos = p.x;
		p = translateToScreen(axis.numValToRawPosition(mean + sd), 0, p);
		int meanPlusSdPos = p.x;
		
		arrowBottom -= kLineHeight + kLineValueGap + ascent + kUnderLineGap;
		drawSummary(g, kSdColor, sd, "s = ", meanPos, meanPlusSdPos, arrowBottom);
		
		p = translateToScreen(boxInfo.boxPos[BoxInfo.LOW_EXT], 0, p);
		int minPos = p.x;
		p = translateToScreen(boxInfo.boxPos[BoxInfo.HIGH_EXT], 0, p);
		int maxPos = p.x;
		
		arrowBottom -= kLineHeight + kLineValueGap + ascent + kUnderLineGap;
		double range = boxInfo.boxVal[BoxInfo.HIGH_EXT] - boxInfo.boxVal[BoxInfo.LOW_EXT];
		drawSummary(g, kRangeColor, range, "range = ", minPos, maxPos, arrowBottom);
		
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		paintBackground(g);
		super.paintView(g);
	}
}