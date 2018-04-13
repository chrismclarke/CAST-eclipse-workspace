package dataView;

import java.util.*;


public class RandomisedCatVariable extends CatVariable implements SampleInterface {
	protected Random generator = new Random();
	private long currentSeed, nextSeed;
	protected int map[];
	
	private int blockSize = 0;
	
	public RandomisedCatVariable(String theName) {
		super(theName);
		nextSeed = generator.nextLong();
	}
	
	public RandomisedCatVariable(String theName, int blockSize) {
		this(theName);
		this.blockSize = blockSize;
	}
	
	public void readValues(String valueString) {
		super.readValues(valueString);
		resetMap();
	}
	
	public void setValues(int values[]) {
		super.setValues(values);
		resetMap();
	}
	
	public void addValue(Value v) {
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
		int nInBlock = (blockSize == 0) ? map.length : blockSize;
		int nBlocks = map.length / nInBlock;
		for (int j=0 ; j<nBlocks ; j++) {
			int base = j * nInBlock;
			for (int i=nInBlock-1 ; i>0 ; i--) {
				int target = (int)Math.round(Math.floor(generator.nextDouble() * (i + 1)));
				int temp = map[base + i];
				map[base + i] = map[base + target];
				map[base + target] = temp;
			}
		}
	}
	
	public ValueEnumeration values() {
		return new RandomisedEnumeration(valueData, map);
	}
	
	public Value valueAt(int index) {
		return super.valueAt(map[index]);
	}
}
