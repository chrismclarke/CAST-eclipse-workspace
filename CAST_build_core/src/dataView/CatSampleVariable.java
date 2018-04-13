package dataView;

import random.*;

public class CatSampleVariable extends CatVariable implements SampleInterface {
	private RandomDiscrete generator;
	private long currentSeed, nextSeed;
	
	public CatSampleVariable(String theName, RandomDiscrete generator) {
		this(theName, generator, NO_REPEATS);
	}
	
	public CatSampleVariable(String theName, RandomDiscrete generator, boolean usesGroups) {
		super(theName, usesGroups);
		this.generator = generator;
		nextSeed = generator.nextLong();
	}
	
	public void clearSample() {
		int[] counts = new int[noOfCategories()];
		setCounts(counts);
	}
	
	public void setSampleSize(int n) {
		generator.setSampleSize(n);
	}
	
	public long generateNextSample() {
		currentSeed = nextSeed;
		generator.setSeed(nextSeed);
		
		doGeneration();
		
		nextSeed = generator.nextLong();
		return currentSeed;
	}
	
	public boolean setSampleFromSeed(long newSeed) {
		if (currentSeed == newSeed)
			return false;
		currentSeed = newSeed;
		generator.setSeed(newSeed);
		
		doGeneration();
		
		return true;
	}
	
	private void doGeneration() {
		if (generator instanceof RandomCat) {
			int cats[] = generator.generate();
			setValues(cats);
		}
		else {
			int counts[] = generator.generate();
			setCounts(counts);
		}
	}
	
	public void setNextSeed(long nextSeed) {		//	only used when setting up exercise with fixed seed
		this.nextSeed = nextSeed;
	}
	
	public RandomDiscrete getGenerator() {
		return generator;
	}
}
