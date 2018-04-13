package dynamic;

import dataView.*;


public class PyramidLagView extends PyramidView {
//	static public final String PYRAMID_LAG_VIEW = "pyramidLagView";
	
	private int lag = 0;			//	number of classes
	
	public PyramidLagView(DataSet theData, XApplet applet,
										String leftKey, String rightKey, int classWidth, int freqMax, int axisMax,
										int axisStep, LabelValue freqLabel) {
		super(theData, applet, leftKey, rightKey, classWidth, freqMax, axisMax, axisStep, freqLabel);
	}
	
	public void setLag(int lag) {
		this.lag = lag;
	}
	
	protected int[] getOuterFrequencies(NumVariable freqVar, NumVariable otherVar) {
		NumSeriesVariable freqSeriesVar = (NumSeriesVariable)freqVar;
		double oldSeriesIndex = freqSeriesVar.getSeriesIndex();
		double lagSeriesIndex = oldSeriesIndex - lag;
		if (lag > 0 && lagSeriesIndex >= 0.0) {
			freqSeriesVar.setSeriesIndex(lagSeriesIndex);
			
			int freq[] = getFrequencies(freqSeriesVar);
			freqSeriesVar.setSeriesIndex(oldSeriesIndex);
			
			for (int i=freq.length-1 ; i>=lag ; i--)
				freq[i] = freq[i - lag];
			
			for (int i=0 ; i<lag ; i++)
				freq[i] = (int)Math.round(freqSeriesVar.doubleValueAt(i));
			
			return freq;
		}
		else
			return null;
	}
	
	protected int[] getInnerFrequencies(NumVariable freqVar, NumVariable otherVar, int[] outerFreq) {
		int freq[] = getFrequencies(freqVar);
		return freq;
	}
	
	protected boolean[] outerAreEstimates(NumVariable freqVar, NumVariable otherVar) {
		NumSeriesVariable freqSeriesVar = (NumSeriesVariable)freqVar;
		double oldSeriesIndex = freqSeriesVar.getSeriesIndex();
		double lagSeriesIndex = oldSeriesIndex - lag;
		if (lag > 0 && lagSeriesIndex >= 0.0) {
			freqSeriesVar.setSeriesIndex(lagSeriesIndex);
			
			boolean ests[] = areEstimates(freqSeriesVar);
			freqSeriesVar.setSeriesIndex(oldSeriesIndex);
			
			if (getCurrentFrame() == 0)				//	we know lagged numbers but not proportions
				for (int i=ests.length-1 ; i>=lag ; i--)
					ests[i] = ests[i - lag];
			
			for (int i=0 ; i<lag ; i++)
				ests[i] = true;
			
			return ests;
		}
		else
			return null;
	}
	
}
	
