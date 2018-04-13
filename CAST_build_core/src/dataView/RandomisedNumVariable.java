package dataView;

import java.util.*;


public class RandomisedNumVariable extends NumVariable implements SampleInterface {
	protected Random generator = new Random();
	private long currentSeed, nextSeed;
	private int map[];
	
	public RandomisedNumVariable(String theName) {
		super(theName);
		nextSeed = generator.nextLong();
	}
	
	public Random getGenerator() {
		return generator;
	}
	
	public void setGenerator(Random generator) {
		this.generator = generator;
	}
	
	public void readValues(String valueString) {
		super.readValues(valueString);
		clearSortedValues();
		resetMap();
	}
	
	public void setValues(double values[]) {
		super.setValues(values);
		resetMap();
	}
	
	public void addValue(Value v) {
		clearSortedValues();
		super.addValue(v);
		resetMap();
	}
	
	public void clearSample() {
		resetMap();
	}
	
	protected void resetMap() {
		if (map == null || map.length != noOfValues())
			map = new int[noOfValues()];
		for (int i=0 ; i<map.length ; i++)
			map[i] = i;
	}
	
	public void setSampleSize(int n) {		//	makes no sense
	}
	
	public long generateNextSample() {
		currentSeed = nextSeed;
		generator.setSeed(nextSeed);
		
		randomiseMap();
		
		nextSeed = generator.nextLong();
		
		return currentSeed;
	}
	
	public boolean setSampleFromSeed(long newSeed) {
		if (currentSeed == newSeed)
			return false;
		currentSeed = newSeed;
		generator.setSeed(newSeed);
		
		randomiseMap();
		
		return true;
	}
	
	public void setNextSeed(long nextSeed) {		//	only used when setting up exercise with fixed seed
		this.nextSeed = nextSeed;
	}
	
	protected void randomiseMap() {
		resetMap();
		for (int i=map.length-1 ; i>0 ; i--) {
			int target = (int)Math.round(Math.floor(generator.nextDouble() * (i + 1)));
			int temp = map[i];
			map[i] = map[target];
			map[target] = temp;
		}
	}
	
	public int[] getMap() {
		return map;
	}
	
	public void setMap(int[] map) {
		this.map = map;
	}
	
	public ValueEnumeration values() {
		return new RandomisedEnumeration(valueData, map);
	}
	
	public Value valueAt(int index) {
		return super.valueAt(map[index]);
	}
}
