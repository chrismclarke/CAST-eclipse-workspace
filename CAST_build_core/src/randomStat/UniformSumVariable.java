package randomStat;

import dataView.*;
import distn.*;


public class UniformSumVariable extends DiscreteDistnVariable {
	private int baseOutcomes, maxSampleSize;
	
	private long[][] coreCounts;
	
	private int sampleSize = 1;
	private long counts[];
	
	private int findMaxPower(int maxSampleSize) {
		int k = 0;
		while (maxSampleSize > 0) {
			maxSampleSize /= 2;
			k ++;
		}
		return k;
	}
	
	private long[] countsForSum(long[] countA, long[] countB) {
		long result[] = new long[countA.length + countB.length - 1];		//	initialised to zero
		for (int i=0 ; i<countA.length ; i++)
			for (int j=0 ; j<countB.length ; j++)
				result[i + j] += countA[i] * countB[j];
		return result;
	}
	
	public UniformSumVariable(String theName, int baseOutcomes, int maxSampleSize) {
		super(theName);
		this.baseOutcomes = baseOutcomes;
		this.maxSampleSize = maxSampleSize;
		
		int maxPower = findMaxPower(maxSampleSize);
		coreCounts = new long[maxPower][];
		coreCounts[0] = new long[baseOutcomes + 1];
		for (int i=1 ; i<=baseOutcomes ; i++)
			coreCounts[0][i] = 1;
		for (int i=1 ; i<maxPower ; i++)
			coreCounts[i] = countsForSum(coreCounts[i-1], coreCounts[i-1]);
		
		updateCounts();
	}
	
	public void setParams(String s) {
		if (s == null)
			sampleSize = 1;
		else
			sampleSize = Integer.parseInt(s);
		updateCounts();
		
		double meanVal = getMean().toDouble();
		setMinSelection(meanVal);
		setMaxSelection(meanVal);
	}
	
	public void setSampleSize(int n) {
		sampleSize = n;
		updateCounts();
	}
	
	private void updateCounts() {
		int tempSampleSize = sampleSize;
		int power2 = 0;
		counts = new long[1];
		counts[0] = 1;
		while (tempSampleSize > 0) {
			boolean usesPower = (tempSampleSize % 2) != 0;
			if (usesPower)
				counts = countsForSum(counts, coreCounts[power2]);
			tempSampleSize /= 2;
			power2 ++;
		}
	}
	
	public NumValue getMean() {
		return new NumValue((baseOutcomes - 1) * maxSampleSize / 2.0, 1);
	}
	
	public NumValue getSD() {
		return new NumValue(Math.sqrt((baseOutcomes * baseOutcomes - 1) / 12.0 * sampleSize), 3);
	}
	
	public double getProbFactor() {
		return Math.pow(1.0 / baseOutcomes, sampleSize);
	}
	
	public double getMaxScaledProb() {
		long maxCount = 0;
		for (int i=0 ; i<counts.length ; i++)
			maxCount = Math.max(maxCount, counts[i]);
		return maxCount;
	}
	
	public double getScaledProb(int x) {
		return (x >= 0 && x < counts.length) ? counts[x] : 0;
	}
	
	public double getCumulativeProb(double v) {
		int vFloor = (int)Math.floor(v);
		int cumCount = 0;
		for (int i=0 ; i< vFloor ; i++)
			cumCount += counts[i];
		return cumCount;
	}
	
}
