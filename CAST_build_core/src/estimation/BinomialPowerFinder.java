package estimation;

import distn.*;


public class BinomialPowerFinder extends CorePowerFinder {
	private int cutoff;
	
	public BinomialPowerFinder(int cutoff, int tail, int sampleSize) {
		super(tail, sampleSize);
		this.cutoff = cutoff;
	}
	
	public void setCutoff(int cutoff) {
		this.cutoff = cutoff;
	}
	
	protected double getPower(double param) {
		if (tail == LOW_TAIL)
			return BinomialTable.cumulative(cutoff, sampleSize, param);
		else if (tail == HIGH_TAIL)
			return 1 - BinomialTable.cumulative(cutoff - 1, sampleSize, param);
		else			//	TWO_TAIL
			return 2 * Math.min(BinomialTable.cumulative(cutoff, sampleSize, param),
																1 - BinomialTable.cumulative(cutoff - 1, sampleSize, param));
	}
}