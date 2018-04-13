package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

import boxPlot.*;


public class DragValueMeanMedianView extends DragBoxValueView {
	static private final int kMaxJitter = 30;
	static private final int kMaxHeadingHt = 25;	//	a guess at the max height for values
	
	private CenterCalculator medianCalc = new CenterCalculator(CenterCalculator.MEDIAN);
	private CenterCalculator meanCalc = new CenterCalculator(CenterCalculator.MEAN);
	
	private int decimals;
	
	public DragValueMeanMedianView(DataSet theData, XApplet applet, NumCatAxis theAxis, int decimals) {
		super(theData, applet, theAxis);
		this.decimals = decimals;
	}
	
	public void setMeanMedianDecimals(int decimals) {
		this.decimals = decimals;
	}
	
	protected int getMaxJitter() {
		int maxMaxJitter = (getNumVariable().noOfValues() > 100) ? kMaxJitter * 2 : kMaxJitter;
		return Math.min(maxMaxJitter, (getSize().height - getViewBorder().top - getViewBorder().bottom) - kMaxHeadingHt);
	}
	
	protected void drawBoxPlot(Graphics g, NumValue sortedVal[], BoxInfo boxInfo) {
		Color oldColor = g.getColor();
		Font labelFont = getApplet().getStandardFont();
		g.setFont(labelFont);
		FontMetrics fm = g.getFontMetrics();
		int lineHt = fm.getHeight();
		
		Point p1 = translateToScreen(boxInfo.boxPos[MEDIAN], 0, null);
		g.setColor(Color.red);
		g.drawLine(p1.x, lineHt, p1.x, getSize().height - 1);
		
		NumVariable variable = getNumVariable();
		NumValue value = new NumValue(medianCalc.evaluateStat(variable, boxInfo), decimals);
		String valString = medianCalc.getName(getApplet()) + " = " + value.toString();
		int valWidth = fm.stringWidth(valString);
		int valStart = Math.min(getSize().width - valWidth, Math.max(0, p1.x - valWidth / 2));
		g.drawString(valString, valStart, fm.getLeading() + fm.getAscent());
		
		value = new NumValue(meanCalc.evaluateStat(variable, boxInfo), decimals);
		valString = meanCalc.getName(getApplet()) + " = " + value.toString();
		
		try {
			int meanPos = axis.numValToPosition(value.toDouble());
			Point p2 = translateToScreen(meanPos, 0, null);
			g.setColor(Color.blue);
			g.drawLine(p2.x, 2 * lineHt, p2.x, getSize().height - 1);
			
			valWidth = fm.stringWidth(valString);
			valStart = Math.min(getSize().width - valWidth, Math.max(0, p2.x - valWidth / 2));
			g.drawString(valString, valStart, lineHt + fm.getLeading() + fm.getAscent());
		} catch (AxisException e) {
		}
		
		g.setColor(oldColor);
	}
	
}
	
