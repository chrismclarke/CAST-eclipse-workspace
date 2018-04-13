package statistic;

import java.awt.*;

import dataView.*;
import axis.*;


public class DiffStatistic extends Statistic {
	public DiffStatistic(int lowOrHigh, boolean showOnGraph, boolean showEqn, XApplet applet) {
		super(lowOrHigh, showOnGraph, showEqn, applet);
	}
	
	protected double getSum(NumVariable variable) {
		ValueEnumeration e = variable.values();
		double sum = 0.0;
		while (e.hasMoreValues())
			sum += ((NumValue)e.nextValue()).toDouble();
		return sum;
	}
	
	public boolean drawZeroOnGraph() {			//		only called for lowOrHigh = ALL
		return true;
	}
	
	protected NumValue evaluate(NumVariable variable, NumValue theConst) {
		int decimals = Math.max(variable.getMaxDecimals(), theConst.decimals);
		
		ValueEnumeration e = variable.values();
		double sum = 0.0;
		while (e.hasMoreValues()) {
			double val = e.nextDouble();
			double c = theConst.toDouble();
			if (val < c && lowOrHigh != HIGH)
				sum += (val - c);
			else if (val >= c && lowOrHigh != LOW)
				sum += (val - c);
		}
		return new NumValue(sum, decimals);
	}
	
	protected String openBracket = "(";		//		changed by AbsDiffStatistic
	protected String closeBracket = ")";
	
	protected EquationInfo equationDimensions(Graphics g) {
		EquationInfo result = sigmaSize(g);
		
		Font oldFont = g.getFont();
		g.setFont(bracketFont);
		FontMetrics fm = g.getFontMetrics();
		result.above = Math.max(result.above, fm.getAscent());
		result.below = Math.max(result.below, fm.getDescent());
		result.width += (fm.stringWidth(openBracket) + fm.stringWidth(closeBracket));
		
		g.setFont(textFont);
		result.width += fm.stringWidth("x - k ");		//	final space is drawn after sigma
		
		g.setFont(oldFont);
		return result;
	}
	
	protected int drawEquation(Graphics g, int left, int baseline) {
		int leftPos = drawSigma(g, left, baseline);
		
		Font oldFont = g.getFont();
		g.setFont(textFont);
		FontMetrics fm = g.getFontMetrics();
		leftPos += fm.charWidth(' ');
		
		g.setFont(bracketFont);
		fm = g.getFontMetrics();
		g.drawString(openBracket, leftPos, baseline);
		leftPos += fm.stringWidth(openBracket);
		
		g.setFont(textFont);
		fm = g.getFontMetrics();
		g.drawString("x - k", leftPos, baseline);
		leftPos += fm.stringWidth("x - k");
		
		g.setFont(bracketFont);
		fm = g.getFontMetrics();
		g.drawString(closeBracket, leftPos, baseline);
		leftPos += fm.stringWidth(openBracket);
		
		g.setFont(oldFont);
		return leftPos;
	}
	
	protected Extremes coreGetExtremes(double axisMin, double axisMax, NumVariable variable) {
		int noOfVals = variable.noOfValues();
		double sum = getSum(variable);
		switch (lowOrHigh) {
			case LOW:
				return new Extremes(sum - axisMax * noOfVals, 0.0);
			case HIGH:
				return new Extremes(0.0, sum - axisMin * noOfVals);
			default:
				return new Extremes(sum - axisMax * noOfVals, sum - axisMin * noOfVals);
		}
	}
	
	protected void setXValues(int graphLeft, NumCatAxis axis, NumVariable variable) {
		int noOfVals = variable.noOfValues();
		NumValue value[] = variable.getSortedData();
		gx = new int[noOfVals + 2];
		gx[0] = graphLeft;
		for (int i=0 ; i<noOfVals ; i++)
			try {
				gx[i + 1] = axis.numValToPosition(value[i].toDouble()) + graphLeft;
			} catch (AxisException e) {
			}
		gx[noOfVals + 1] = graphLeft + axis.getAxisLength();
	}
	
	protected void setupGraph(Extremes ext, int graphTop, int graphBottom,
													int graphLeft, NumCatAxis axis, NumVariable variable) {
		int noOfVals = variable.noOfValues();
		if (lowOrHigh == ALL) {
			double sum = getSum(variable);
			gx = new int[2];
			gy = new int[2];
			gx[0] = graphLeft;
			gx[1] = graphLeft + axis.getAxisLength();
			gy[0] = getHeight(sum - axis.minOnAxis * noOfVals, ext, graphTop, graphBottom);
			gy[1] = getHeight(sum - axis.maxOnAxis * noOfVals, ext, graphTop, graphBottom);
			return;
		}
		
		setXValues(graphLeft, axis, variable);
		
		NumValue value[] = variable.getSortedData();
		gy = new int[noOfVals + 2];
		if (lowOrHigh == LOW) {
			double sum = 0.0;
			gy[0] = getHeight(sum, ext,  graphTop, graphBottom);
			double lastVal = axis.minOnAxis;
			for (int i=0 ; i<noOfVals ; i++) {
				sum -= i * (value[i].toDouble() - lastVal);
				gy[i + 1] = getHeight(sum, ext,  graphTop, graphBottom);
				lastVal = value[i].toDouble();
			}
			sum -= noOfVals * (axis.maxOnAxis - lastVal);
			gy[noOfVals + 1] = getHeight(sum, ext,  graphTop, graphBottom);
		}
		else {
			double sum = axis.maxOnAxis * noOfVals - getSum(variable);
			gy[0] = getHeight(sum, ext,  graphTop, graphBottom);
			double lastVal = axis.minOnAxis;
			for (int i=0 ; i<noOfVals ; i++) {
				sum -= (noOfVals - i) * (value[i].toDouble() - lastVal);
				gy[i + 1] = getHeight(sum, ext,  graphTop, graphBottom);
				lastVal = value[i].toDouble();
			}
			gy[noOfVals + 1] = gy[noOfVals];
		}
	}
}
