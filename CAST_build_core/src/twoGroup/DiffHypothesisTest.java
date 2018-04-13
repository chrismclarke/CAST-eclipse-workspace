package twoGroup;

import java.awt.*;

import dataView.*;
import distn.*;
import qnUtils.*;
import imageGroups.*;

import models.*;


public class DiffHypothesisTest extends HypothesisTest {
	static final public int DELTA_IMAGE = 0;
	static final public int DIFF_MEAN_IMAGE = 1;
	static final public int DIFF_PROB_IMAGE = 2;
	
	private int imageType;
	
	public DiffHypothesisTest(DataSet data, NumValue testValue, int testTail,
																			int imageType, XApplet applet) {
		super(data, testValue, testTail, (data instanceof GroupsDataSet) ? DIFF_MEAN : DIFF_PROPN,
																												applet);
		this.imageType = imageType;
	}
	
	public DiffHypothesisTest(DataSet data, NumValue testValue,
																			int testTail, XApplet applet) {
		this(data, testValue, testTail, DELTA_IMAGE, applet);
	}
	
	public double evaluateStatistic() {
		if (paramType == DIFF_MEAN) {
			GroupsDataSet anovaData = (GroupsDataSet)data;
			
			double diff = anovaData.getMean(1) - anovaData.getMean(0);
			
			double s1 = anovaData.getSD(0);
			double s2 = anovaData.getSD(1);
			int n1 = anovaData.getN(0);
			int n2 = anovaData.getN(1);
			
			double sDiff = Math.sqrt(s1 * s1 / n1 + s2 * s2 / n2);
			
			return (diff - testValue.toDouble()) / sDiff;
		}
		else {
			ContinTableDataSet continData = (ContinTableDataSet)data;
			
			double p1 = continData.getPropn(0);
			double p2 = continData.getPropn(1);
			double diff = p2 - p1;
			
			int n1 = continData.getN(0);
			int n2 = continData.getN(1);
			
			double sDiff = Math.sqrt(p1 * (1.0 - p1) / n1 + p2 * (1.0 - p2) / n2);
			
			return (diff - testValue.toDouble()) / sDiff;
		}
	}
	
	public double evaluatePValue() {
		double t = evaluateStatistic();
		
		double pValue;
		if (paramType == DIFF_MEAN) {
			GroupsDataSet anovaData = (GroupsDataSet)data;
			int df = Math.min(anovaData.getN(0), anovaData.getN(1)) - 1;
			pValue = TTable.cumulative(t, df);
		}
		else
			pValue = NormalTable.cumulative(t);
		
		if (testTail == HA_HIGH)
			pValue = 1.0 - pValue;
		else if (testTail == HA_NOT_EQUAL)
			pValue = 2.0 * Math.min(pValue, 1.0 - pValue);
			
		return pValue;
	}
	
	protected Image getParamImage() {
		switch (imageType) {
			case DIFF_MEAN_IMAGE:
				return ScalesImages.diffMeans;
			case DIFF_PROB_IMAGE:
				return ScalesImages.diffProbs;
			default:
				return ScalesImages.delta;
		}
	}
}
	
