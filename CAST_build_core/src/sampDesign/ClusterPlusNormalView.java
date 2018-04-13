package sampDesign;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class ClusterPlusNormalView extends JitterPlusNormalView {
	
	private DataSet clusterData;
	private String normalKey, clusterKey;
	private boolean usesFixedMeanDistn;
	
	private double popnSize = Double.POSITIVE_INFINITY;
	
	private double sampleValue[] = null;
	
	public ClusterPlusNormalView(DataSet theData, XApplet applet, NumCatAxis theAxis, String normalKey,
									double initialJittering, DataSet clusterData, String clusterKey,
									boolean usesFixedMeanDistn) {
		super(theData, applet, theAxis, normalKey, initialJittering);
		this.normalKey = normalKey;
		this.clusterData = clusterData;
		this.clusterKey = clusterKey;
		this.usesFixedMeanDistn = usesFixedMeanDistn;
	}
	
	public void setFinitePopnSize(double popnSize) {
		this.popnSize = popnSize;
	}
	
	protected void paintBackground(Graphics g) {
		if (!usesFixedMeanDistn) {
			ClusterSampleVariable clusterY = (ClusterSampleVariable)clusterData.getVariable(clusterKey);
			
			int hiliteIndex = getData().getSelection().findSingleSetFlag();
			if (hiliteIndex < 0)
				return;
			
			sampleValue = clusterY.extractSample(hiliteIndex, sampleValue);
			
			double sy = 0.0;
			double syy = 0.0;
			for (int i=0 ; i<sampleValue.length ; i++) {
				sy += sampleValue[i];
				syy += sampleValue[i] * sampleValue[i];
			}
			double mean = sy / sampleValue.length;
			double n = sampleValue.length;
			double finitePopnCorrection = (1.0 - n / popnSize);
			double sdMean = Math.sqrt(finitePopnCorrection * (syy - sy * sy / n) / (n - 1.0) / n);
			
			NormalDistnVariable yDistn = (NormalDistnVariable)getData().getVariable(normalKey);
			yDistn.setMean(mean);
			yDistn.setSD(sdMean);
		}
		
		super.paintBackground(g);
	}
	
}