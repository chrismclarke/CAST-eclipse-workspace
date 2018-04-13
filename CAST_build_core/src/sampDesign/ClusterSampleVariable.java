package sampDesign;

import java.util.*;

import dataView.*;
import random.RandomContinuous;



class ClusterValueEnumeration implements ValueEnumeration {
	private double clusterValue[];
	private int nextIndex, clusterSize;
	private double mean, a, b;
	
	private Enumeration e;
	private NumValue tempValue = null;
	
	public ClusterValueEnumeration(Vector v, double[] clusterValue, int clusterSize,
																									double mean, double a, double b) {
		e = v.elements();
		this.clusterValue = clusterValue;
		this.clusterSize = clusterSize;
		this.a = a;
		this.b = b;
		this.mean = mean;
		nextIndex = 0;
	}
	
	public boolean hasMoreValues() {
		return e.hasMoreElements();
	}
	
	public Value nextValue() {
		NumValue v = (NumValue)e.nextElement();
		if (tempValue == null)
			tempValue = new NumValue(0.0, v.decimals);
		tempValue.setValue(transformValue(v.toDouble(), nextIndex));
		nextIndex ++;
		return tempValue;
	}
	
	public RepeatValue nextGroup() {
		RepeatValue nextRepeat = new RepeatValue((Value)e.nextElement(), 1);
		nextIndex ++;
		return nextRepeat;
	}
	
	public double nextDouble() {
		double nextVal = transformValue(((NumValue)e.nextElement()).toDouble(), nextIndex);
		nextIndex ++;
		return nextVal;
	}
	
	private double transformValue(double v, int index) {
		return mean + a * v + b * clusterValue[index / clusterSize];
	}
}


public class ClusterSampleVariable extends NumVariable implements SampleInterface {
	private RandomContinuous generator;		//	assumes generator has mean 0.0;
	private int decimals;
	
	private int noOfClusters, clusterSize, highlightCluster, clustersPerSample;
	
	private double clusterValue[] = null;
	private double mean, proportion, a, b;
	
	private NumValue tempValue;
	
	public ClusterSampleVariable(String theName, RandomContinuous generator,
												double mean, double initialProportion, int decimals) {
		super(theName);
		this.generator = generator;
		this.decimals = decimals;
		noOfClusters = 0;
		clusterSize = generator.getSampleSize();
		highlightCluster = -999;
		setProportion(initialProportion);
		this.mean = mean;
		tempValue = new NumValue(0.0, decimals);
		clustersPerSample = 1;
	}
	
	public void setProportion(double p) {
		proportion = p;
		a = Math.sqrt(proportion);
		b = Math.sqrt(1.0 - proportion);
	}
	
	public void setClustersPerSample(int clustersPerSample) {
		this.clustersPerSample = clustersPerSample;
	}
	
	public int getClustersPerSample() {
		return clustersPerSample;
	}
	
	public int getSampleSize() {
		return clusterSize * clustersPerSample;
	}
	
	public int getClusterSize() {
		return clusterSize;
	}
	
	public int getHighlightSample() {
		return highlightCluster / clustersPerSample;
	}
	
	public int getNoOfClusters() {
		return noOfClusters;
	}
	
	public void setSampleSize(int n) {
		clusterSize = n / clustersPerSample;
		generator.setSampleSize(clusterSize);
	}
	
	public int noOfValues() {
		return noOfClusters * clusterSize;
	}
	
	public void clearSample() {
		noOfClusters = 0;
		highlightCluster = -999;
		setValues(new double[0]);
	}
	
	public long generateNextSample() {
		highlightCluster = noOfClusters;
		
		for (int k=0 ; k<clustersPerSample ; k++) {
			double values[] = generator.generate();
			for (int i=0 ; i<values.length ; i++)
				addValue(new NumValue(values[i], decimals));
			
			if (clusterValue == null)
				clusterValue = new double[20];
			else if (clusterValue.length <= noOfClusters) {
				double temp[] = clusterValue;
				clusterValue = new double[2 * noOfClusters];
				System.arraycopy(temp, 0, clusterValue, 0, noOfClusters);
			}
			clusterValue[noOfClusters] = generator.generateOne();
			noOfClusters ++;
		}
		
		return highlightCluster;
	}
	
	public boolean setSampleFromSeed(long newSeed) {
		int newHighlightCluster = (int)newSeed;
		if (highlightCluster == newHighlightCluster)
			return false;
		highlightCluster = newHighlightCluster;
		return true;
	}
	
	public void setNextSeed(long nextSeed) {		//	only used when setting up exercise with fixed seed
		generator.setSeed(nextSeed);
	}
	
	public int getMaxDecimals() {
		return decimals;
	}
	
	public double[] extractLastSample(double[] clusterValue) {
		if (clusterValue == null || clusterValue.length != clusterSize * clustersPerSample)
			clusterValue = new double[clusterSize * clustersPerSample];
		
		for (int i=0 ; i<clusterSize * clustersPerSample ; i++)
			clusterValue[i] = doubleValueAt((noOfClusters - clustersPerSample) * clusterSize + i);
		
		return clusterValue;
	}
	
	public double[] extractSample(int clusterIndex, double[] clusterValue) {
		if (clusterValue == null || clusterValue.length != clusterSize * clustersPerSample)
			clusterValue = new double[clusterSize * clustersPerSample];
		
		for (int i=0 ; i<clusterSize * clustersPerSample ; i++)
			clusterValue[i] = doubleValueAt(clusterIndex * clusterSize + i);
		
		return clusterValue;
	}
	
	public Value valueAt(int index) {
		NumValue val = (NumValue)super.valueAt(index);
		tempValue.setValue(mean + a * val.toDouble() + b * clusterValue[index / clusterSize]);
		return tempValue;
	}
	
	public ValueEnumeration values() {
		return new ClusterValueEnumeration(valueData, clusterValue, clusterSize,
																									mean, a, b);
	}
		
}
