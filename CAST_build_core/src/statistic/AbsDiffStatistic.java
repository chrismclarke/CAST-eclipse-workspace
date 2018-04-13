package statistic;

import dataView.*;
import axis.*;


public class AbsDiffStatistic extends DiffStatistic {
	public AbsDiffStatistic(int lowOrHigh, boolean showOnGraph, boolean showEqn,
																							XApplet applet) {
		super(lowOrHigh, showOnGraph, showEqn, applet);
		openBracket = "|";
		closeBracket = "|";
	}
	
	public boolean drawZeroOnGraph() {			//		only called for lowOrHigh = ALL
		return false;
	}
	
	protected NumValue evaluate(NumVariable variable, NumValue theConst) {
		int decimals = Math.max(variable.getMaxDecimals(), theConst.decimals);
		
		ValueEnumeration e = variable.values();
		double sumAbs = 0.0;
		while (e.hasMoreValues()) {
			double val = e.nextDouble();
			double c = theConst.toDouble();
			if (val < c && lowOrHigh != HIGH)
				sumAbs += (c - val);
			else if (val >= c && lowOrHigh != LOW)
				sumAbs += (val - c);
		}
		return new NumValue(sumAbs, decimals);
	}
	
	protected Extremes coreGetExtremes(double axisMin, double axisMax, NumVariable variable) {
		switch (lowOrHigh) {
			case HIGH:
				return super.coreGetExtremes(axisMin, axisMax, variable);
			case LOW:
				Extremes signedExtremes = super.coreGetExtremes(axisMin, axisMax, variable);
				return new Extremes(0.0, -signedExtremes.lowExtreme);
			default:
			case ALL:
				int noOfVals = variable.noOfValues();
				NumValue sortedData[] = variable.getSortedData();
				double median = sortedData[(noOfVals + 1) / 2].toDouble();			//	if n is even, may be val below median
				
				double minSum = 0.0;
				for (int i=0 ; i<noOfVals ; i++)
					minSum += Math.abs(sortedData[i].toDouble() - median);
				
				Extremes endExtremes = super.coreGetExtremes(axisMin, axisMax, variable);
				double maxSum = Math.max(-endExtremes.lowExtreme, endExtremes.highExtreme);
				
				return new Extremes(minSum, maxSum);
		}
	}
	
	protected void setupGraph(Extremes ext, int graphTop, int graphBottom,
													int graphLeft, NumCatAxis axis, NumVariable variable) {
		setXValues(graphLeft, axis, variable);
		
		int noOfVals = variable.noOfValues();
		NumValue value[] = variable.getSortedData();
		gy = new int[noOfVals + 2];
		switch (lowOrHigh) {
			case LOW:
				double sum = 0.0;
				gy[0] = getHeight(sum, ext,  graphTop, graphBottom);
				double lastVal = axis.minOnAxis;
				for (int i=0 ; i<noOfVals ; i++) {
					sum += i * (value[i].toDouble() - lastVal);
					gy[i + 1] = getHeight(sum, ext,  graphTop, graphBottom);
					lastVal = value[i].toDouble();
				}
				sum += noOfVals * (axis.maxOnAxis - lastVal);
				gy[noOfVals + 1] = getHeight(sum, ext,  graphTop, graphBottom);
				break;
			case HIGH:
				sum = getSum(variable) - axis.minOnAxis * noOfVals;
				gy[0] = getHeight(sum, ext,  graphTop, graphBottom);
				lastVal = axis.minOnAxis;
				for (int i=0 ; i<noOfVals ; i++) {
					sum -= (noOfVals - i) * (value[i].toDouble() - lastVal);
					gy[i + 1] = getHeight(sum, ext,  graphTop, graphBottom);
					lastVal = value[i].toDouble();
				}
				gy[noOfVals + 1] = gy[noOfVals];
				break;
			case ALL:
				sum = getSum(variable) - axis.minOnAxis * noOfVals;
				gy[0] = getHeight(sum, ext,  graphTop, graphBottom);
				lastVal = axis.minOnAxis;
				for (int i=0 ; i<noOfVals ; i++) {
					sum += (2 * i - noOfVals) * (value[i].toDouble() - lastVal);
					gy[i + 1] = getHeight(sum, ext,  graphTop, graphBottom);
					lastVal = value[i].toDouble();
				}
				sum += noOfVals * (axis.maxOnAxis - lastVal);
				gy[noOfVals + 1] = getHeight(sum, ext,  graphTop, graphBottom);
		}
	}
}
