package control;

import java.awt.*;

import dataView.*;
import axis.*;


public class RangeControlView extends MeanControlView {
//	static public final String RANGE_CONTROL_PLOT = "RangeControlPlot";
	
	private NumCatAxis dataAxis;
	
	public RangeControlView(DataSet meanData, XApplet applet, TimeAxis timeAxis, ControlLimitAxis numAxis,
										int problemFlags, DataSet rawData, int noOfTrainingSamples,
										NumCatAxis dataAxis) {
		super(meanData, applet, timeAxis, numAxis, problemFlags, rawData, noOfTrainingSamples);
		this.dataAxis = dataAxis;
	}
	
	static final private double lowFactor[] = {0, 0, 0, 0, 0, 0.08, 0.14, 0.18, 0.22, 0.26, 0.28, 0.31, 0.33, 0.35, 0.36, 0.38, 0.39, 0.40, 0.41};
	static final private double highFactor[] = {3.27, 2.57, 2.28, 2.11, 2.00, 1.92, 1.86, 1.82, 1.78, 1.74, 1.72, 1.69, 1.67, 1.65, 1.64, 1.62, 1.61, 1.60, 1.59};
	
	public void setShowSummaries(boolean showSummaries) {
		this.showSummaries = true;			//		always show summaries for range chart
		NumVariable rawVariable = rawData.getNumVariable();
		GroupMeanVariable meanVariable = (GroupMeanVariable)getNumVariable();
		int noInGroup = meanVariable.getNoInGroup();
		
		ValueEnumeration e = rawVariable.values();
		double total = 0.0;
		for (int group=0 ; group<noOfTrainingSamples ; group++) {
			double min = e.nextDouble();
			double max = min;
			for (int i=1 ; i<noInGroup ; i++) {
				double nextVal = e.nextDouble();
				if (nextVal < min)
					min = nextVal;
				else if (nextVal > max)
					max = nextVal;
			}
			total += (max - min);
		}
		double centre = total / noOfTrainingSamples;
		int decimals = meanVariable.getMaxDecimals() + 1;
		getNumAxis().setControlLimits(new NumValue(centre * lowFactor[noInGroup - 2], decimals),
												new NumValue(centre, decimals),
												new NumValue(centre * highFactor[noInGroup - 2], decimals));
		repaint();
	}
	
	protected Point getRawScreenPoint(int index, double theVal, int noInGroup, Point thePoint) {
		try {
			int vertPos = dataAxis.numValToPosition(theVal);
			int horizPos = getTimeAxis().timePosition(index / noInGroup);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
}
	
