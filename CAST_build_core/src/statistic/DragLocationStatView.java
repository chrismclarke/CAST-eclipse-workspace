package statistic;

import java.awt.*;

import dataView.*;
import axis.*;
import random.RandomBits;


public class DragLocationStatView extends DragLocationView {
	static private final int kStatBorder = 6;
	static private final int kSpaceAboveEqn = 5;
	static private final int kSpaceBelowEqn = 4;
	static private final int kJittering = 50;
	static private final int kBarHt = 4;
	static private final int kDotExtraHt = 8;
	
	protected int jittering[] = null;
	
	private boolean initialised = false;
	private Statistic lowStatistic, highStatistic, totalStatistic;
	
	public DragLocationStatView(DataSet theData, XApplet applet, DragValAxis theAxis,
												Statistic lowStatistic, Statistic highStatistic, Statistic totalStatistic) {
		super(theData, applet, theAxis);
		this.lowStatistic = lowStatistic;
		this.highStatistic = highStatistic;
		this.totalStatistic = totalStatistic;
	}
	
	private boolean showGraph() {
		return totalStatistic != null && totalStatistic.getGraphVisibility()
								|| lowStatistic != null && lowStatistic.getGraphVisibility()
								|| highStatistic != null && highStatistic.getGraphVisibility();
	}
	
	private Extremes accumulateExtremes(Statistic theStatistic, Extremes oldExtremes) {
		if (theStatistic == null)
			return oldExtremes;
		else if (oldExtremes == null)
			return theStatistic.getExtremes(axis.minOnAxis, axis.maxOnAxis, getNumVariable());
		else {
			Extremes newExtremes = theStatistic.getExtremes(axis.minOnAxis, axis.maxOnAxis, getNumVariable());
			if (newExtremes == null)
				return oldExtremes;
			else
				return new Extremes(Math.min(oldExtremes.lowExtreme, newExtremes.lowExtreme),
								Math.max(oldExtremes.highExtreme, newExtremes.highExtreme));
		}
	}
	
	private void paintGraph(Graphics g, int graphTop, int graphBottom, NumVariable variable) {
		Extremes ext = accumulateExtremes(totalStatistic, null);
		ext = accumulateExtremes(lowStatistic, ext);
		ext = accumulateExtremes(highStatistic, ext);
		if (totalStatistic != null && totalStatistic.getGraphVisibility() && totalStatistic.drawZeroOnGraph()) {
			double height = -ext.lowExtreme
										/ (ext.highExtreme - ext.lowExtreme) * (graphBottom - graphTop);
			int zeroPos = graphBottom - (int)Math.round(height);
			g.setColor(Color.lightGray);
			g.drawLine(0, zeroPos, getSize().width - 1, zeroPos);
		}
		
		if (totalStatistic != null) {
			g.setColor(Color.black);
			totalStatistic.drawGraph(g, ext, graphTop, graphBottom, getViewBorder().left, axis, variable);
		}
		if (lowStatistic != null) {
			g.setColor(Color.blue);
			lowStatistic.drawGraph(g, ext, graphTop, graphBottom, getViewBorder().left, axis, variable);
		}
		if (highStatistic != null) {
			g.setColor(darkGreen);
			highStatistic.drawGraph(g, ext, graphTop, graphBottom, getViewBorder().left, axis, variable);
		}
	}
	
	private boolean canDrawEquation(Statistic stat) {
		return (stat != null) && stat.getEqnVisibility();
	}
	
	private void paintStats(Graphics g, NumValue constValue, NumVariable variable) {
		Color oldColor = g.getColor();
		
		EquationInfo totalDim = null;
		EquationInfo lowDim = null;
		EquationInfo highDim = null;
		int statBottom = 0;
		int topStatBottom = 0;
		int totalBaseLine = 0;
		int lowHighBaseLine = 0;
		
		if (canDrawEquation(totalStatistic)) {
			totalStatistic.setupValue(variable, constValue);
			totalDim = totalStatistic.getSize(g);
			totalBaseLine = kSpaceAboveEqn + totalDim.above;
			topStatBottom = statBottom = totalBaseLine + totalDim.below;
		}
		if (canDrawEquation(lowStatistic) && canDrawEquation(highStatistic)) {
			lowStatistic.setupValue(variable, constValue);
			highStatistic.setupValue(variable, constValue);
			lowDim = lowStatistic.getSize(g);
			highDim = highStatistic.getSize(g);
			statBottom += kSpaceAboveEqn;
			lowHighBaseLine = statBottom + Math.max(lowDim.above, highDim.above);
			statBottom = lowHighBaseLine + Math.max(lowDim.below, highDim.below);
		}
		statBottom += kSpaceBelowEqn;
		
		int dotPlotTop = translateToScreen(0, kJittering + kDotExtraHt, null).y;
		int graphBottom = dotPlotTop - kBarHt;
		int graphTop = showGraph() ? statBottom : graphBottom;
		
		g.setColor(getApplet().getBackground());
		g.fillRect(0, 0, getSize().width, graphTop);
		g.fillRect(0, graphBottom, getSize().width, dotPlotTop - graphBottom);
		
		int markedPos = 0;
		int modMarkedPos = 0;
		try {
			markedPos = translateToScreen(axis.numValToPosition(constValue.toDouble()), 0,  null).x;
			modMarkedPos = markedPos;
			
			if (canDrawEquation(lowStatistic) && canDrawEquation(highStatistic))
				modMarkedPos = Math.min(Math.max(markedPos, lowDim.width + 2 * kStatBorder),
														getSize().width - (highDim.width + 2 * kStatBorder));
			
			if (selectedVal) {
				g.setColor(Color.yellow);
				g.fillRect(markedPos - 2, dotPlotTop, 5, getSize().height - dotPlotTop);
			}
			
			g.setColor(Color.red);
			g.drawLine(modMarkedPos, topStatBottom - 1, modMarkedPos, statBottom);
			g.drawLine(modMarkedPos, statBottom, markedPos, graphTop);
			g.drawLine(markedPos, graphTop, markedPos, getSize().height - 1);
		} catch (AxisException e) {
			return;
		}
		
		if (canDrawEquation(totalStatistic)) {
			g.setColor(Color.black);
			totalStatistic.draw(g, (getSize().width - totalDim.width) / 2, totalBaseLine);
		}
		
		if (canDrawEquation(lowStatistic) && canDrawEquation(highStatistic)) {
			int valLeft = (modMarkedPos - lowDim.width) / 2;
			g.setColor(Color.blue);
			lowStatistic.draw(g, valLeft, lowHighBaseLine);
			
			valLeft = (getSize().width + modMarkedPos - highDim.width) / 2;
			g.setColor(darkGreen);	
			highStatistic.draw(g, valLeft, lowHighBaseLine);
		}
		
		if (showGraph())
			paintGraph(g, graphTop, graphBottom, variable);
		
		g.setColor(oldColor);
	}
	
	private void checkJittering() {
		int dataLength = getNumVariable().noOfValues();
		if (jittering == null || jittering.length != dataLength) {
			RandomBits generator = new RandomBits(14, dataLength);			//	between 0 and 2^14 = 16384
			jittering = generator.generate();
		}
	}
	
	private Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		try {
			int horizPos = axis.numValToPosition(theVal.toDouble());
			int vertPos = (jittering != null) ? ((kJittering * jittering[index]) >> 14) : 0;
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	public void paintView(Graphics g) {
		if (!initialised) {
			requestFocus();
			initialised = true;
		}
		NumVariable variable = getNumVariable();
		DragValAxis theAxis = (DragValAxis)axis;
		NumValue axisValue = theAxis.getAxisVal();
		paintStats(g, axisValue, variable);
		
		checkJittering();
		
		ValueEnumeration e = variable.values();
		int index = 0;
		Point thePoint = null;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null) {
				if (nextVal.toDouble() < axisValue.toDouble())
					g.setColor(Color.blue);
				else
					g.setColor(darkGreen);		
				drawCross(g, thePoint);
			}
			index++;
		}
	}
}
	
