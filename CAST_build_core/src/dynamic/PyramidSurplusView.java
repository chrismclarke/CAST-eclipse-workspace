package dynamic;

import dataView.*;


public class PyramidSurplusView extends PyramidView {
//	static public final String PYRAMID_SURPLUS_VIEW = "pyramidSurplusView";
	
	private boolean showSurplus;
	
	public PyramidSurplusView(DataSet theData, XApplet applet,
										String leftKey, String rightKey, int classWidth, int freqMax, int axisMax,
										int axisStep, LabelValue freqLabel) {
		super(theData, applet, leftKey, rightKey, classWidth, freqMax, axisMax, axisStep, freqLabel);
	}
	
	public void setShowSurplus(boolean showSurplus) {
		this.showSurplus = showSurplus;
	}
	
	protected int[] getOuterFrequencies(NumVariable freqVar, NumVariable otherVar) {
		if (showSurplus)
			return getFrequencies(freqVar);
		else
			return null;
	}
	
	protected int[] getInnerFrequencies(NumVariable freqVar, NumVariable otherVar, int[] outerFreq) {
		if (showSurplus) {
			int[] innerFreq = getFrequencies(otherVar);
			if (outerFreq !=  null)
				for (int i=0 ; i<outerFreq.length ; i++)
					innerFreq[i] = Math.min(innerFreq[i], outerFreq[i]);
			return innerFreq;
		}
		else
			return getFrequencies(freqVar);
	}
	
	protected boolean[] outerAreEstimates(NumVariable freqVar, NumVariable otherVar) {
		return areEstimates(otherVar);
	}
	
}
	
