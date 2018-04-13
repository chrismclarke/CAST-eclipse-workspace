package random;


public class AlmostRandomInteger extends RandomInteger {
															//		ensures that each value in 0-max appears
															//			without duplicates in max successive values
															//			at least once in (max + 2) successive values
	private int min;
	private int history[];		//	zero-based
	private boolean alreadyUsed[];
	
	public AlmostRandomInteger(int min, int max) {
		super(0, max - min, 1);
		this.min = min;
		history = new int[max - min + 1];
		for (int i=0 ; i<history.length ; i++)
			history[i] = -1;
		alreadyUsed = new boolean[max - min + 1];
	}
	
	public AlmostRandomInteger(int min, int max, long randomSeed) {
		this(min, max);
		super.setSeed(randomSeed);
	}
	
	private void clearHistory() {
		for (int i=0 ; i<history.length ; i++)
			history[i] = -1;
		for (int i=0 ; i<alreadyUsed.length ; i++)
			alreadyUsed[i] = false;
	}
	
	public void setSeed(long randomSeed) {
		if (history != null && alreadyUsed != null)
			clearHistory();
		super.setSeed(randomSeed);
	}

	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return min + alreadyUsed.length - 1;
	}
	
	public int generateOne() {
		for (int i=0 ; i<alreadyUsed.length ; i++)
			alreadyUsed[i] = false;
		
		for (int i=0 ; i<history.length ; i++) {
			if (history[i] >= 0)
				alreadyUsed[history[i]] = true;
		}
		
		boolean allUsed = true;
		for (int i=0 ; i<alreadyUsed.length ; i++)
			if (!alreadyUsed[i])
				allUsed = false;
		
		int newValue;
		boolean valueBad;
		do {
			newValue = super.generateOne();
			if (allUsed) {
				valueBad = false;
				for (int i=0 ; i<history.length - 2 ; i++)
					if (history[i] == newValue)
						valueBad = true;
			}
			else
				valueBad = alreadyUsed[newValue];
		} while (valueBad);
		
		for (int i=history.length-1 ; i>0 ; i--)
			history[i] = history[i-1];
		history[0] = newValue;
		
		return newValue + min;
	}
}