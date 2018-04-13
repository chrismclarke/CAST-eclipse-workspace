package statistic;

import java.awt.*;

import dataView.*;
import axis.*;


class MomentInfo {
	MomentInfo(NumVariable variable) {
		int noOfVals = variable.noOfValues();				//		only implemented for lowOrHigh = ALL
		ValueEnumeration e = variable.values();
		double sum = 0.0;
		while (e.hasMoreValues())
			sum += e.nextDouble();
		mean = sum / noOfVals;
		
		ssq = 0.0;
		double diff;
		e = variable.values();
		while (e.hasMoreValues()) {
			diff = e.nextDouble() - mean;
			ssq += diff * diff;
		}
	}
	
	double mean, ssq;
}


public class SqrDiffStatistic extends DiffStatistic {
	static private final int kNoOfGraphPts = 100;
	
	public SqrDiffStatistic(int lowOrHigh, boolean showOnGraph, boolean showEqn,
																						XApplet applet) {
		super(lowOrHigh, showOnGraph, showEqn, applet);
	}
	
	public boolean drawZeroOnGraph() {			//		only called for lowOrHigh = ALL
		return false;
	}
	
	protected NumValue evaluate(NumVariable variable, NumValue theConst) {
		int decimals = Math.max(variable.getMaxDecimals(), theConst.decimals);
		
		ValueEnumeration e = variable.values();
		double sum = 0;
		while (e.hasMoreValues()) {
			double diff = e.nextDouble() - theConst.toDouble();
//			if (diff < 0.0 && lowOrHigh != HIGH || diff >= 0.0 && lowOrHigh != LOW)
				sum += diff * diff;		//		only implemented for lowOrHigh = ALL
		}
		return new NumValue(sum, 2 * decimals);
	}
	
	protected EquationInfo equationDimensions(Graphics g) {
		EquationInfo result = super.equationDimensions(g);
		
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		FontMetrics fm = g.getFontMetrics();
		
		result.above = Math.max(result.above, kSuperscript + fm.getAscent());
		g.setFont(oldFont);
		return result;
	}
	
	protected int drawEquation(Graphics g, int left, int baseline) {
		int leftPos = super.drawEquation(g, left, baseline);
		
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		FontMetrics fm = g.getFontMetrics();
		
		g.drawString("2", leftPos, baseline - kSuperscript);
		leftPos += fm.stringWidth("2");
		g.setFont(oldFont);
		
		return leftPos;
	}
	
	protected Extremes coreGetExtremes(double axisMin, double axisMax, NumVariable variable) {
		MomentInfo moments = new MomentInfo(variable);	//		only implemented for lowOrHigh = ALL
		
		double maxDiff = Math.max(moments.mean - axisMin, axisMax - moments.mean);
		double maxSsq = moments.ssq + maxDiff * maxDiff * variable.noOfValues();
		
		return new Extremes(moments.ssq, maxSsq);
	}
	
	protected void setupGraph(Extremes ext, int graphTop, int graphBottom,
													int graphLeft, NumCatAxis axis, NumVariable variable) {
		MomentInfo moments = new MomentInfo(variable);	//		only implemented for lowOrHigh = ALL
		
		gx = new int[kNoOfGraphPts];
		gy = new int[kNoOfGraphPts];
		
		for (int i=0 ; i<kNoOfGraphPts ; i++) {
			gx[i] = graphLeft + axis.getAxisLength() * i / (kNoOfGraphPts - 1);
			double diff = axis.minOnAxis + (axis.maxOnAxis - axis.minOnAxis) * i / (kNoOfGraphPts - 1)
																										- moments.mean;
			gy[i] = getHeight(moments.ssq + diff * diff * variable.noOfValues(), ext, graphTop, graphBottom);
		}
	}
}
