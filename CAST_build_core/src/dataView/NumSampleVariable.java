package dataView;


import random.RandomContinuous;

public class NumSampleVariable extends NumVariable implements SampleInterface {
	private RandomContinuous generator;
	private int decimals;
	private long currentSeed, nextSeed;
	
	public NumSampleVariable(String theName, RandomContinuous generator, int decimals) {
		super(theName);
		this.generator = generator;
		this.decimals = decimals;
		nextSeed = generator.nextLong();
	}
	
	public void setSampleSize(int n) {
		generator.setSampleSize(n);
		clearSample();
	}
	
//	public int noOfValues() {
//		return generator.getSampleSize();
//	}
	
	public void clearSample() {
		setValues(new double[0]);
	}
	
	public long generateNextSample() {
		currentSeed = nextSeed;
		generator.setSeed(nextSeed);
		double values[] = generator.generate();
		nextSeed = generator.nextLong();
		
		setValues(values);
		return currentSeed;
	}
	
	public boolean setSampleFromSeed(long newSeed) {
		if (currentSeed == newSeed)
			return false;
		currentSeed = newSeed;
		generator.setSeed(newSeed);
		double values[] = generator.generate();
		
		setValues(values);
		return true;
	}
	
	public void setNextSeed(long nextSeed) {		//	only used when setting up exercise with fixed seed
		this.nextSeed = nextSeed;
	}
	
	public void setValues(double values[]) {		//		side effect: changes values array
		for (int i=0 ; i<values.length ; i++)
			values[i] = round(values[i]);
		
		if (getNoOfGroups() == values.length) {
			for (int i=0 ; i<values.length ; i++) {
				NumValue val = (NumValue)valueAt(i);
				val.setValue(values[i]);
			}
			clearSortedValues();
		}
		else {
			super.setValues(values);
			setDecimals(decimals);
		}
	}
	
	private double round(double v) {
		double temp = v;
		double factor = 1.0;
		for (int i=0 ; i<decimals ; i++) {
			temp *= 10.0;
			factor *= 0.1;
		}
		return Math.round(temp) * factor;
	}
	
	public int getMaxDecimals() {
		return decimals;
	}
	
	public RandomContinuous getGenerator() {
		return generator;
	}
	
	public void setGenerator(RandomContinuous generator) {
		this.generator = generator;
	}
}
