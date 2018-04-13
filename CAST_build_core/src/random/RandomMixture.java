package random;


public class RandomMixture extends RandomContinuous {
	static final private long kGen1SeedOffset = 23485736395l;
	static final private long kGen2SeedOffset = 34857629340l;
	
	private RandomContinuous gen1, gen2;
	private double prob1;
	private boolean fixedCounts = true;
	private boolean fixedCount2;
	
	public RandomMixture(int count, RandomContinuous gen1, RandomContinuous gen2, double prob1) {
		super(count);
		this.gen1 = gen1;
		this.gen2 = gen2;
		this.prob1 = prob1;
		fixedCount2 = false;
	}
	
	public RandomMixture(RandomContinuous gen1, RandomContinuous gen2) {
		super(gen1.getSampleSize() + gen2.getSampleSize());
		this.gen1 = gen1;
		this.gen2 = gen2;
//		prob1 = gen1.getSampleSize() / count;
		fixedCount2 = true;
	}
	
	public void setSeed(long seed) {
		super.setSeed(seed);
		if (gen1 != null)
			gen1.setSeed(seed + kGen1SeedOffset);
		if (gen2 != null)
			gen2.setSeed(seed + kGen2SeedOffset);
	}
	
	public void setFixedCount(boolean fixedCounts) {
		this.fixedCounts = fixedCounts;
	}
	
	protected double getQuantile(double p) {
											//	not needed since generate() and generateOne() overridden
		return Double.NaN;
	}
	
	public double generateOne() {
		if (nextDouble() < prob1)
			return gen1.generateOne();
		else
			return gen2.generateOne();
	}
	
	public double[] generate() {
		if (fixedCounts) {
			double y1[] = gen1.generate();
			double y2[] = gen2.generate();
			double y[] = new double[count];
			int n1 = y1.length;
			int n2 = y2.length;
			for (int i=0 ; i<count ; i++)
				if (nextDouble() * (n1 + n2) < n1)
					y[i] = y1[--n1];
				else
					y[i] = y2[--n2];
			return y;
		}
		else
			return super.generate();
	}
	
	public void setSampleSize(int count) {
		this.count = count;
		if (fixedCount2)			//		keeps 2nd generator is often a single outlier
			gen1.setSampleSize(count - gen2.count);
		else {
			int count1 = (int)Math.round(count * prob1);
			gen1.setSampleSize(count1);
			gen2.setSampleSize(count - count1);
		}
	}
}