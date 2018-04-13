package linMod;

import java.awt.*;

import dataView.*;
import distn.*;
import qnUtils.*;
import imageGroups.*;


public class SlopeHypothesisTest extends HypothesisTest {
	private String yKey;
	
	public SlopeHypothesisTest(DataSet data, String yKey, NumValue testValue,
																			int testTail, XApplet applet) {
		super(data, testValue, testTail, SLOPE, applet);
		this.yKey = yKey;
	}
	
	public double evaluateStatistic() {
		SlopeDistnVariable slopeDistnVar = (SlopeDistnVariable)data.getVariable(yKey);
		NumValue slope = slopeDistnVar.getMean();
		NumValue slopeSD = slopeDistnVar.getSD();
		return (slope.toDouble() - testValue.toDouble()) / slopeSD.toDouble();
	}
	
	public double evaluatePValue() {
		SlopeDistnVariable slopeDistnVar = (SlopeDistnVariable)data.getVariable(yKey);
		NumValue slope = slopeDistnVar.getMean();
		NumValue slopeSD = slopeDistnVar.getSD();
		int count = slopeDistnVar.getN();
		
		double t = (slope.toDouble() - testValue.toDouble()) / slopeSD.toDouble();
		
		double pValue = TTable.cumulative(t, count - 2);
		if (testTail == HA_HIGH)
			pValue = 1.0 - pValue;
		else if (testTail == HA_NOT_EQUAL)
			pValue = 2.0 * Math.min(pValue, 1.0 - pValue);
			
		return pValue;
	}
	
	protected Image getParamImage() {
		return ScalesImages.beta1;
	}
}
	
