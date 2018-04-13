package statistic;

import java.awt.*;

import dataView.*;
import axis.*;


public class CountStatistic extends Statistic {
	static private final String kCountString = "count";
	
	public CountStatistic(int lowOrHigh, boolean showOnGraph, boolean showEqn, XApplet applet) {
		super(lowOrHigh, showOnGraph, showEqn, applet);
	}
	
	protected NumValue evaluate(NumVariable variable, NumValue theConst) {
		ValueEnumeration e = variable.values();
		int lowCount=0;
		while (e.hasMoreValues()) {
			double val = e.nextDouble();
			if (val < theConst.toDouble())
				lowCount++;
		}
		int value = (lowOrHigh == LOW) ? lowCount
								: (lowOrHigh == HIGH) ? variable.noOfValues() - lowCount
								: variable.noOfValues();
		return new NumValue(value, 0);
	}
	
	protected EquationInfo equationDimensions(Graphics g) {
		Font oldFont = g.getFont();
		g.setFont(textFont);
		FontMetrics fm = g.getFontMetrics();
		EquationInfo result = new EquationInfo(fm.stringWidth(kCountString), fm.getAscent(), 0);
		g.setFont(oldFont);
		return result;
	}
	
	protected int drawEquation(Graphics g, int left, int baseline) {
		Font oldFont = g.getFont();
		g.setFont(textFont);
		g.drawString(kCountString, left, baseline);
		FontMetrics fm = g.getFontMetrics();
		int leftPos = left + fm.stringWidth(kCountString);
		g.setFont(oldFont);
		return leftPos;
	}
	
	protected Extremes coreGetExtremes(double axisMin, double axisMax, NumVariable variable) {
		return new Extremes(0.0, variable.noOfValues());
	}
	
	protected void setupGraph(Extremes ext, int graphTop, int graphBottom,
													int graphLeft, NumCatAxis axis, NumVariable variable) {
		if (lowOrHigh == ALL) {
			gx = new int[2];
			gy = new int[2];
			gx[0] = graphLeft;
			gx[1] = graphLeft + axis.getAxisLength();
			gy[0] = graphTop;
			gy[1] = graphTop;
			return;
		}
		
		NumValue value[] = variable.getSortedData();
		gx = new int[2 * (value.length + 1)];
		gy = new int[2 * (value.length + 1)];
		gx[0] = graphLeft;
		for (int i=0 ; i<value.length ; i++)
			try{
				gx[2 * i + 1] = gx[2 * i + 2] = axis.numValToPosition(value[i].toDouble()) + graphLeft;
			} catch (AxisException e) {
			}
		gx[2 * value.length + 1] = graphLeft + axis.getAxisLength();
		
		if (lowOrHigh == LOW) {
			gy[0] = getHeight(0, ext,  graphTop, graphBottom);
			for (int i=0 ; i<value.length ; i++) {
				gy[2 * i + 1] = gy[2 * i];
				gy[2 * i + 2] = getHeight(i + 1, ext,  graphTop, graphBottom);
			}
		}
		else {
			gy[0] = getHeight(value.length, ext,  graphTop, graphBottom);
			for (int i=0 ; i<value.length ; i++) {
				gy[2 * i + 1] = gy[2 * i];
				gy[2 * i + 2] = getHeight(value.length - i - 1, ext,  graphTop, graphBottom);
			}
		}
		gy[2 * value.length + 1] = gy[2 * value.length];
	}
}
