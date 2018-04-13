package dataView;


public class BootstrapNumVariable extends RandomisedNumVariable {
	protected int count[];
	
	public BootstrapNumVariable(String theName) {
		super(theName);
	}
	
	public int[] getUsageCounts() {
		return count;
	}
	
	public void setUsageCounts(int[] count) {
		this.count = count;
	}
	
	public void resetMap() {
		super.resetMap();
		int map[] = getMap();
		if (count == null || count.length != map.length)
			count = new int[map.length];
		else
			for (int i=0 ; i<count.length ; i++)
				count[i] = 0;
	}
	
	public void randomiseMap() {
		resetMap();
		int n = noOfValues();
		int map[] = getMap();
		for (int i=0 ; i<n ; i++) {
			int target = (int)Math.round(Math.floor(generator.nextDouble() * n));
			map[i] = target;
			count[target] ++;
		}
		clearSortedValues();
	}
}
