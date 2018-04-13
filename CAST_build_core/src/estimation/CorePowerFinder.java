package estimation;


abstract public class CorePowerFinder {
	static final public int LOW_TAIL = 0;
	static final public int HIGH_TAIL = 1;
	static final public int TWO_TAIL = 2;
	
	protected int tail, sampleSize;
	
	public CorePowerFinder(int tail, int sampleSize) {
		this.tail = tail;
		this.sampleSize = sampleSize;
	}
	
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}
	
	abstract protected double getPower(double param);
}