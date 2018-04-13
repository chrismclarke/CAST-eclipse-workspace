package statistic;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import formula.*;


public class JitterMeanSdView extends DotPlotView {
	static final private int kArrowHeight = 36;
	static final private int kArrowHead = 4;
	static final private int kMeanSdGap = 5;
	
	static final private Color kResidArrowColor = Color.green;
	static final private Color kResidValueColor = new Color(0x009900);
	
	private int decimals;
	private String residKey;
	
	public JitterMeanSdView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey,
																															String residKey, int decimals) {
		super(theData, applet, theAxis, 1.0);
		setActiveNumVariable(yKey);
		this.decimals = decimals;
		this.residKey = residKey;
		setFont(applet.getBigFont());
	}
	
	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}
	
	public NumValue getMean() {
		ValueEnumeration ye = getNumVariable().values();
		int n = 0;
		double sy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			n ++;
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
			sy += y;
			syy += y * y;
			n ++;
		}
		return new NumValue(Math.sqrt((syy - sy * sy / n) / (n - 1)), decimals);
	}
	
	protected void paintForeground(Graphics g) {
		NumValue meanVal = getMean();
		NumValue sdVal = getSD();
		
		int meanPos = axis.numValToRawPosition(meanVal.toDouble());
		int meanPlusSdPos = axis.numValToRawPosition(meanVal.toDouble() + sdVal.toDouble());
		
		int arrowBottom = getMeanArrowBottom(g);
		
		drawHorizArrow(meanPos, meanPlusSdPos, arrowBottom + kArrowHeight / 2,
																	new LabelValue("s = " + sdVal), Color.red, Color.red, g);
		
		g.setColor(Color.blue);
		Point p1 = translateToScreen(meanPos, arrowBottom + kArrowHeight, null);
		Point p2 = translateToScreen(meanPos, arrowBottom, null);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		LabelValue meanLabel = new LabelValue(MText.expandText("x#bar# = ") + meanVal);
		meanLabel.drawCentred(g, p1.x, p1.y - kMeanSdGap);
		
		p1 = translateToScreen(meanPos - kArrowHead, arrowBottom + kArrowHead, p1);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		p1 = translateToScreen(meanPos + kArrowHead, arrowBottom + kArrowHead, p1);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	private int getMeanArrowBottom(Graphics g) {
		int meanSdHeight = kArrowHeight + kMeanSdGap + g.getFontMetrics().getAscent() + 7;
		int arrowBottom = currentJitter;
		if (currentJitter + meanSdHeight < getSize().height)
			arrowBottom += (getSize().height - meanSdHeight - currentJitter) / 2;
		return arrowBottom;
	}
	
	private void drawHorizArrow(int startHoriz, int endHoriz, int vert, Value label,
																		Color arrowColor, Color textColor, Graphics g) {
		g.setColor(arrowColor);
		
		Point p1 = translateToScreen(startHoriz, vert, null);
		Point p2 = translateToScreen(endHoriz, vert, null);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		int sdWidth = label.stringWidth(g);
		int sdCentre = (endHoriz > startHoriz) ? Math.max(2 + p1.x + sdWidth / 2, (p1.x + p2.x) / 2)
															: Math.min(p1.x - sdWidth / 2 - 2, (p1.x + p2.x) / 2);
		
		int direction = (startHoriz < endHoriz) ? 1 : -1;
		p1 = translateToScreen(endHoriz - direction * kArrowHead, vert + kArrowHead, p1);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		p1 = translateToScreen(endHoriz - direction * kArrowHead, vert - kArrowHead, p1);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		
		g.setColor(textColor);
		label.drawCentred(g, sdCentre, p1.y - kMeanSdGap);
	}
	
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		if (thePoint != null) {
			int meanPos = axis.numValToRawPosition(getMean().toDouble());
			NumVariable yVar = getNumVariable();
			double y = ((NumValue)yVar.valueAt(index)).toDouble();
			int yPos = axis.numValToRawPosition(y);
		
			NumVariable residVar = (NumVariable)getVariable(residKey);
			drawHorizArrow(meanPos, yPos, currentJitter + kArrowHead, residVar.valueAt(index),
																								kResidArrowColor, kResidValueColor, g);
			
			g.setColor(kResidArrowColor);
//			int arrowBottom = getMeanArrowBottom(g);
			Point p1 = translateToScreen(meanPos, 0, null);
			Point p2 = translateToScreen(yPos, 0, null);
			g.drawLine(p1.x, 0, p1.x, getSize().height);
			
			g.drawLine(p2.x, 0, p2.x, getSize().height);
			
//			drawCrossBackground(g, thePoint);
		}
	}
	
	
	public void paintView(Graphics g) {
		super.paintView(g);
		paintForeground(g);
	}
}