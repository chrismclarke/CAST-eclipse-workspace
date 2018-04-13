package distn;


public class MixtureInfo extends DistnInfo {
	static final private int kSteps = 100;
	static final private int kNoOfIter = 2;
	
	private ContinDistnVariable distn1, distn2;
	private double p1;
	
	public MixtureInfo (ContinDistnVariable distn1, ContinDistnVariable distn2, double p1) {
		super();
		this.distn1 = distn1;
		this.distn2 = distn2;
		this.p1 = p1;
	}
	
	public double zMin() {
		DistnInfo distnInfo1 = distn1.getDistnInfo();
		DistnInfo distnInfo2 = distn2.getDistnInfo();
		return Math.min(distn1.zToX(distnInfo1.zMin()), distn2.zToX(distnInfo2.zMin()));
	}
	
	public double zMax() {
		DistnInfo distnInfo1 = distn1.getDistnInfo();
		DistnInfo distnInfo2 = distn2.getDistnInfo();
		return Math.max(distn1.zToX(distnInfo1.zMax()), distn2.zToX(distnInfo2.zMax()));
	}
	
	public double zMode() {
		double mode1 = distn1.zToX(distn1.getDistnInfo().zMode());
		double mode2 = distn2.zToX(distn2.getDistnInfo().zMode());
						//	no guarantee, but we hope mode is between these two values
		
		for (int iter=1 ; iter<=kNoOfIter ; iter++) {
			double highDensity = getDensity(mode1);
			double step = (mode2 - mode1) / kSteps;
			int highIndex = 0;
			for (int i=1 ; i<=kSteps ; i++) {
				double density = getDensity(mode1 + i * step);
				if (density > highDensity) {
					highIndex = i;
					highDensity = density;
				}
			}
			
			if (iter == kNoOfIter)
				return mode1 + highIndex * step;
			
			if (highIndex == 0)
				mode2 = mode1 + step;
			else if (highIndex == kSteps)
				mode1 = mode2 - step;
			else {
				mode1 = mode1 + (highIndex - 1) * step;
				mode2 = mode1 + 2 * step;
			}
		}
		
		return mode1;
	}
	
	public boolean isSymmetric() {
		return false;
	}
	
	public double getDensity(double z) {
		double d1 = distn1.getScaledDensity(z) * distn1.getDensityFactor();
		double d2 = distn2.getScaledDensity(z) * distn2.getDensityFactor();
		return d1 * p1 + d2 * (1 - p1);
	}
	
	public boolean sameParams(ContinDistnVariable v) {
		return false;
	}
}