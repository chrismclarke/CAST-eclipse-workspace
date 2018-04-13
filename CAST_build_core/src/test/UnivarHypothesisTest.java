package test;

import java.awt.*;

import dataView.*;
import distn.*;
import qnUtils.*;
import imageGroups.*;


public class UnivarHypothesisTest extends HypothesisTest {

	protected String yKey;
	protected NumValue sigmaValue;
	
	public UnivarHypothesisTest(DataSet data, String yKey, NumValue testValue, int testTail,
																				int paramType, XApplet applet) {
		super(data, testValue, testTail, paramType, applet);
		this.yKey = yKey;
	}
	
	public UnivarHypothesisTest(DataSet data, String yKey, NumValue testValue, NumValue sigmaValue,
																				int testTail, XApplet applet) {
		this(data, yKey, testValue, testTail, MEAN_KNOWN_SIGMA, applet);
		this.sigmaValue = sigmaValue;
	}
	
	public double evaluateStatistic() {
		if (paramType == MEAN_KNOWN_SIGMA) {
			NumVariable yVar = (NumVariable)data.getVariable(yKey);
			double sum = 0.0;
			ValueEnumeration ye = yVar.values();
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				sum += y;
			}
			int count = yVar.noOfValues();
			
			double mean = sum / count;
			double sdMean = sigmaValue.toDouble() / Math.sqrt(count);
			return (mean - testValue.toDouble()) / sdMean;
		}
		else if (paramType == MEAN || paramType == PAIRED_DIFF_MEAN) {
			NumVariable yVar = (NumVariable)data.getVariable(yKey);
			double sum = 0.0;
			double sum2 = 0.0;
			ValueEnumeration ye = yVar.values();
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				sum += y;
				sum2 += y * y;
			}
			int count = yVar.noOfValues();
			
			double mean = sum / count;
			double sdMean = Math.sqrt(((sum2 - sum * mean) / (count - 1)) / count);
			return (mean - testValue.toDouble()) / sdMean;
		}
		else {												//		paramType == PROPN
			CatVariable yVar = (CatVariable)data.getVariable(yKey);
			int count[] = yVar.getCounts();
			int x = count[0];
			int n = yVar.noOfValues();
			double p = testValue.toDouble();
			return (x - n * p) / Math.sqrt(n * p * (1.0 - p));
		}
	}
	
	public double evaluatePValue() {
		double pValue;
		if (paramType == MEAN_KNOWN_SIGMA) {
			NumVariable yVar = (NumVariable)data.getVariable(yKey);
			double sum = 0.0;
			ValueEnumeration ye = yVar.values();
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				sum += y;
			}
			int count = yVar.noOfValues();
			
			double mean = sum / count;
			double sdMean = sigmaValue.toDouble() / Math.sqrt(count);
			double z = (mean - testValue.toDouble()) / sdMean;
			
			pValue = NormalTable.cumulative(z);
			if (testTail == HA_HIGH)
				pValue = 1.0 - pValue;
			else if (testTail == HA_NOT_EQUAL)
				pValue = 2.0 * Math.min(pValue, 1.0 - pValue);
		}
		else if (paramType == MEAN || paramType == PAIRED_DIFF_MEAN) {
			NumVariable yVar = (NumVariable)data.getVariable(yKey);
			double sum = 0.0;
			double sum2 = 0.0;
			ValueEnumeration ye = yVar.values();
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				sum += y;
				sum2 += y * y;
			}
			int count = yVar.noOfValues();
			
			double mean = sum / count;
			double sdMean = Math.sqrt(((sum2 - sum * mean) / (count - 1)) / count);
			double t = (mean - testValue.toDouble()) / sdMean;
			
			pValue = TTable.cumulative(t, count - 1);
			if (testTail == HA_HIGH)
				pValue = 1.0 - pValue;
			else if (testTail == HA_NOT_EQUAL)
				pValue = 2.0 * Math.min(pValue, 1.0 - pValue);
		}
		else if (paramType == PROPN || paramType == PROPN_2) {
			CatVariable yVar = (CatVariable)data.getVariable(yKey);
			int count[] = yVar.getCounts();
			int x = count[0];
			int n = yVar.noOfValues();
			double p = testValue.toDouble();
			
			if (testTail == HA_LOW) {
				if (paramType == PROPN || x == 0)
					pValue = BinomialTable.cumulative(x, n, p);
				else
					pValue = 0.5 * (BinomialTable.cumulative(x, n, p) + BinomialTable.cumulative(x - 1, n, p));
			}
			else if (testTail == HA_HIGH) {
				if (paramType == PROPN || x == n)
					pValue = 1.0 - BinomialTable.cumulative(x - 1, n, p);
				else
					pValue = 1.0 - 0.5 * (BinomialTable.cumulative(x - 1, n, p) + BinomialTable.cumulative(x, n, p));
			}
			else
				pValue = Math.min(1.0, 2.0 * Math.min(BinomialTable.cumulative(x, n, p),
											1.0 - BinomialTable.cumulative(x - 1, n, p)));
		}
		else {						// paramType == PROPN_APPROX
			CatVariable yVar = (CatVariable)data.getVariable(yKey);
			int count[] = yVar.getCounts();
			int x = count[0];
			int n = yVar.noOfValues();
			double p = testValue.toDouble();
			double sd = Math.sqrt(n * p * (1.0 - p));
			if (testTail == HA_LOW)
				pValue = NormalTable.cumulative((x + 0.5 - n * p) / sd);
			else if (testTail == HA_HIGH)
				pValue = 1.0 - NormalTable.cumulative((x - 0.5 - n * p) / sd);
			else
				pValue = Math.min(1.0, 2.0 * Math.min(NormalTable.cumulative((x + 0.5 - n * p) / sd),
											1.0 - NormalTable.cumulative((x - 0.5 - n * p) / sd)));
		}
		return pValue;
	}
	
	protected Image getParamImage() {
		switch (paramType) {
			case MEAN:
			case MEAN_KNOWN_SIGMA:
				return ScalesImages.mu;
			case PROPN:
			case PROPN_2:
			case PROPN_APPROX:
				return ScalesImages.pi;
			case PAIRED_DIFF_MEAN:
				return ScalesImages.muDiff;
		}
		return null;
	}
}
	
