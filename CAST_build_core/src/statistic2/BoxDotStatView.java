package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

import boxPlot.*;


public class BoxDotStatView extends BoxAndDotView {
	private static final int kMaxStatJitter = 20;
	private static final int kStatTopBorder = 20;
	private static final int kLineAboveJitter = 8;
	private static final int kValAboveLine = 8;
	
	static final private Color kHiliteBackColor = new Color(0xFFDDEE);
//	static final private Color kHiliteCrossColor = new Color(0xCCCCFF);
	static final private Color kHiliteCrossColor = new Color(0xFF9933);
	
	private SpreadCalculator spreadCalc = new SpreadCalculator(SpreadCalculator.RANGE);
	private int decimals;
	
	public BoxDotStatView(DataSet theData, XApplet applet, NumCatAxis theAxis, int decimals) {
		super(theData, applet, theAxis);
		this.decimals = decimals;
		setFillColor(Color.white);
	}
	
	private void selectBetweenLimits() {
		NumVariable variable = getNumVariable();
		SpreadLimits limits = spreadCalc.findSpreadLimits(getNumVariable(), boxInfo);
		boolean selection[] = new boolean[variable.noOfValues()];
		
		ValueEnumeration e = variable.values();
		int i = 0;
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			selection[i] = (nextVal >= limits.low)  && (nextVal <= limits.high);
			i++;
		}
		
		getData().setSelection(selection);
	}
	
	protected int getMaxJitter() {
		return Math.min(kMaxStatJitter, (getSize().height - getViewBorder().top - getViewBorder().bottom - kStatTopBorder) / 3);
	}
	
	protected void shadeBackground(Graphics g) {
		SpreadLimits limits = spreadCalc.findSpreadLimits(getNumVariable(), boxInfo);

		int startPos = axis.numValToRawPosition(limits.low);
		int endPos = axis.numValToRawPosition(limits.high);
		Point p0 = translateToScreen(startPos, 0, null);
		Point p1 = translateToScreen(endPos, 0, null);
		g.setColor(kHiliteBackColor);
		g.fillRect(p0.x, 0, p1.x - p0.x, getSize().height);
		
		g.setColor(getForeground());
	}
	
	protected void drawCounts(Graphics g) {
		Color oldColor = g.getColor();
		g.setFont(getApplet().getStandardFont());
		LabelValue name = new LabelValue(spreadCalc.getName(getApplet()) + " = ");
		NumValue value = new NumValue(spreadCalc.evaluateStat(getNumVariable(), boxInfo), decimals);
		int leftLength = name.stringWidth(g);
		int rightLength = value.stringWidth(g);
		int horizValPos = axis.getAxisLength() / 2;
		
		int startPos = -50;
		int endPos = axis.getAxisLength() + 50;
		SpreadLimits limits = spreadCalc.findSpreadLimits(getNumVariable(), boxInfo);
		try {
			startPos = axis.numValToPosition(limits.low);
		} catch (AxisException e) {
		}
		try {
			endPos = axis.numValToPosition(limits.high);
		} catch (AxisException e) {
		}
		
		Point startPt = translateToScreen(startPos, 3 * currentJitter, null);
		Point endPt = translateToScreen(endPos, 3 * currentJitter, null);
		g.setColor(Color.blue);
		g.drawLine(startPt.x, startPt.y - kLineAboveJitter - 1, endPt.x, endPt.y - kLineAboveJitter - 1);
		g.drawLine(startPt.x, startPt.y - kLineAboveJitter, endPt.x, endPt.y - kLineAboveJitter);
		g.drawLine(startPt.x, startPt.y - kLineAboveJitter - 1, startPt.x + 3, endPt.y - kLineAboveJitter - 4);
		g.drawLine(startPt.x, startPt.y - kLineAboveJitter, startPt.x + 3, endPt.y - kLineAboveJitter + 3);
		g.drawLine(endPt.x, startPt.y - kLineAboveJitter - 1, endPt.x - 3, endPt.y - kLineAboveJitter - 4);
		g.drawLine(endPt.x, startPt.y - kLineAboveJitter, endPt.x - 3, endPt.y - kLineAboveJitter + 3);
		
		if (spreadCalc.getStat() == SpreadCalculator.STDEV) {
			CenterCalculator meanCalc = new CenterCalculator(CenterCalculator.MEAN);
			double mean = meanCalc.evaluateStat(getNumVariable(), boxInfo);
			try {
				int meanPos = axis.numValToPosition(mean);
				Point meanPt = translateToScreen(meanPos, 3 * currentJitter, null);
				g.drawLine(meanPt.x, meanPt.y - kLineAboveJitter - 4, meanPt.x,
																					meanPt.y - kLineAboveJitter + 3);
			} catch (AxisException e) {
			}
			
			g.setColor(Color.red);
			Font oldFont = g.getFont();
			g.setFont(getApplet().getStandardBoldFont());
			horizValPos = (axis.getAxisLength() - rightLength + leftLength) / 2;
			Point valCentre = translateToScreen(horizValPos,
					3 * currentJitter + kLineAboveJitter + kValAboveLine + g.getFontMetrics().getHeight(), null);
			name.drawLeft(g, valCentre.x, valCentre.y);
			value.drawRight(g, valCentre.x, valCentre.y);
			
			g.setFont(oldFont);
			g.setColor(Color.blue);
			int labelPos = Math.min(axis.getAxisLength() - rightLength, Math.max(leftLength,
																								(startPos + endPos) / 2));
			valCentre = translateToScreen(labelPos,
											3 * currentJitter + kLineAboveJitter + kValAboveLine, null);
			double multiplier = spreadCalc.getSDMultiplier() * 2.0;
			String sdString = Long.toString(Math.round(multiplier)) + " SD";
			LabelValue sdLabel = new LabelValue(sdString);
			sdLabel.drawCentred(g, valCentre.x, valCentre.y);
		}
		else {
			horizValPos = Math.min(axis.getAxisLength() - rightLength, Math.max(leftLength,
																	(startPos + endPos - rightLength + leftLength) / 2));
			
			Point valCentre = translateToScreen(horizValPos,
																3 * currentJitter + kLineAboveJitter + kValAboveLine, null);
			name.drawLeft(g, valCentre.x, valCentre.y);
			value.drawRight(g, valCentre.x, valCentre.y);
		}
		
		g.setColor(oldColor);
	}
	
	protected void initialiseBox(NumValue sortedVal[], BoxInfo boxInfo) {
		super.initialiseBox(sortedVal, boxInfo);
		selectBetweenLimits();		//		selection cannot be made until box is initialised
	}
	
	protected Color getHiliteColor() {
		return kHiliteCrossColor;
	}
	
	public void setSpreadStat(int newStat) {
		spreadCalc.setStat(newStat);
		selectBetweenLimits();			//	setting the selection will repaint the view
	}
	
	public int getSpreadStat() {
		return spreadCalc.getStat();
	}
	
	public void setSDMultiplier(double sdMultiplier) {
		spreadCalc.setSDMultiplier(sdMultiplier);
		selectBetweenLimits();			//	setting the selection will repaint the view
	}
}