package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class StackedMeanSDView extends StackedDotPlotView {
	static final protected Color kDarkRed = new Color(0x990000);
	static final private Color k2SDColor = new Color(0xDDDDFF);
	static final private Color k1SDColor = new Color(0xBBBBFF);
	
	static final private int kMeanSDBorder = 40;
	
	private NumValue meanValue, sdValue;
	
	private Color meanSDColor = null;
	
	public StackedMeanSDView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																														int meanDecimals, int sdDecimals) {
		super(theData, applet, theAxis, null, false);
		setViewBorder(new Insets(kMeanSDBorder, 5, 5, 5));
		meanValue = new NumValue(0.0, meanDecimals);
		sdValue = new NumValue(0.0, sdDecimals);
	}
	
	public void setMeanSDColor(Color meanSDColor) {
		this.meanSDColor = meanSDColor;
	}
	
	protected void draw2SdBands(Graphics g) {
		NumVariable yVar = getNumVariable();
		ValueEnumeration ye = yVar.values();
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
		
		double mean = sy / n;
		double sd = Math.sqrt((syy - sy * mean) / (n - 1));
		int ascent = g.getFontMetrics().getAscent();
			
		Point p = translateToScreen(axis.numValToRawPosition(mean - 2 * sd), 0, null);
		int lowPos = p.x;
		p = translateToScreen(axis.numValToRawPosition(mean + 2 * sd), 0, null);
		int highPos = p.x;
		g.setColor(k2SDColor);
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
		g.setColor(k1SDColor);
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		g.drawLine(highPos, 0, highPos, getSize().height);
		
		p = translateToScreen(axis.numValToRawPosition(mean - sd), 0, null);
		lowPos = p.x;
		p = translateToScreen(axis.numValToRawPosition(mean + sd), 0, null);
		highPos = p.x;
		g.setColor(k1SDColor);
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
		g.setColor(Color.blue);
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		g.drawLine(highPos, 0, highPos, getSize().height);
		
		int meanBaseline = ascent + 2;
		p = translateToScreen(axis.numValToRawPosition(mean), 0, null);
		int meanPos = p.x;
		g.setColor(Color.black);
		g.drawLine(meanPos, meanBaseline + 2, meanPos, getSize().height);
		
		if (meanSDColor != null) {
			g.setColor(meanSDColor);
			g.drawLine(meanPos, meanBaseline + 1, meanPos, meanBaseline + 9);
		}
		meanValue.setValue(mean);
		LabelValue l = new LabelValue(getApplet().translate("mean") + " = " + meanValue.toString());
		l.drawCentred(g, meanPos, meanBaseline);
		
		int sdBaseline = meanBaseline + ascent + 10;
		p = translateToScreen(axis.numValToRawPosition(mean + sd), 0, null);
		int sdPos = p.x;
		
		int arrowCentre = meanBaseline + 5;
		g.drawLine(meanPos, arrowCentre, sdPos, arrowCentre);
		g.drawLine(meanPos + 1, arrowCentre - 1, sdPos - 1, arrowCentre - 1);
		g.drawLine(meanPos + 1, arrowCentre + 1, sdPos - 1, arrowCentre + 1);
		
		for (int i=2 ; i<5 ; i++) {
//					g.drawLine(meanPos + i, arrowCentre - i, meanPos + i, arrowCentre + i);
			g.drawLine(sdPos - i, arrowCentre - i, sdPos - i, arrowCentre + i);
		}
		
		sdValue.setValue(sd);
		l = new LabelValue("s = " + sdValue.toString());
		l.drawRight(g, meanPos + 3, sdBaseline);
	}
	
	protected void paintBackground(Graphics g) {
		draw2SdBands(g);
		
		g.setColor(getForeground());
	}
}