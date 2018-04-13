package histo;

import dataView.*;
import axis.*;


public class DensityAxis extends VertAxis implements HistoDensityInfo {
	public static final int COUNT_LABELS = 0;
	public static final int REL_FREQ_LABELS = 1;
	public static final int DENSITY_LABELS = 2;
	public static final int NO_LABELS = 3;
	
	private double baseMaxDensity;
	private double currentMaxDensity;
	private double classWidth;
	private int noOfVals;
	private int axisType;
	
	public DensityAxis(int axisType, double maxDensity, double classWidth, int noOfVals, XApplet applet) {
		super(applet);
		baseMaxDensity = maxDensity;
		showUnlabelledAxis = true;
//		showUnlabelledAxis = false;
		initialiseDensityScale(baseMaxDensity);
		setupLabels(axisType, classWidth, noOfVals);
	}
	
	public void changeLabelType(int axisType) {
		setupLabels(axisType, classWidth, noOfVals);
	}
	
	private void initialiseDensityScale(double newMaxDensity) {
		currentMaxDensity = newMaxDensity;
		String axisString = "0 " + new NumValue(newMaxDensity, 8).toString();
												//		must reduce number of decimals or NumCatAxis throws
												//		a NumberFormatException when reading
//		System.out.println(axisString);
		readNumLabels(axisString);
	}
	
	public String getAxisName() {
		switch (axisType) {
			case COUNT_LABELS:
				return "frequency";
			case REL_FREQ_LABELS:
				return "rel freq";
			case DENSITY_LABELS:
				return "density";
			default:
				return "";
		}
	}
	
	private void setupLabels(int axisType, double classWidth, int noOfVals) {
		this.axisType = axisType;
		this.classWidth = classWidth;
		this.noOfVals = noOfVals;
		switch (axisType) {
			case DENSITY_LABELS:
				setupDensityLabels();
				break;
			case COUNT_LABELS:
				setupCountLabels();
				break;
			case REL_FREQ_LABELS:
				setupRelFreqLabels();
				break;
			case NO_LABELS:
				setupNoLabels();
				break;
		}
		resetLabelSizes();
		if (getParent() != null) {
			invalidate();
			repaint();
			getParent().doLayout();
		}
	}
	
	private void setupDensityLabels() {
		int power = 0;
		double temp = currentMaxDensity > 0 ? currentMaxDensity : 1.0;
		while (temp < 5.0) {
			temp *= 10.0;
			power--;
		}
		while (temp >= 50.0) {
			temp /= 10.0;
			power++;
		}
		double step = ((temp >= 25.0) ? 5.0 : (temp >= 10.0) ? 2.0 : 1.0) * Math.pow(10.0, power);
		int decimals = (power >= 0) ? 0 : -power;
		
		NumValue stepVal = new NumValue(step, decimals);
		setFakeNumLabels(currentMaxDensity, stepVal);
	}
	
	private void setupCountLabels() {
		double maxCount = currentMaxDensity * noOfVals * classWidth;
		
		int factor = 1;
		int temp = (int)maxCount;
		while (temp >= 50) {
			temp /= 10;
			factor *= 10;
		}
		int step = ((temp >= 25) ? 5 : (temp >= 10) ? 2 : 1) * factor;
		
		NumValue stepVal = new NumValue(step, 0);
		
		setFakeNumLabels(maxCount, stepVal);
	}
	
	private void setupRelFreqLabels() {
		double maxRelFreq = currentMaxDensity * classWidth;
		
		int power = 0;
		double temp = maxRelFreq;
		while (temp < 5.0) {
			temp *= 10.0;
			power--;
		}
		double step = ((temp >= 25.0) ? 5.0 : (temp >= 10.0) ? 2.0 : 1.0) * Math.pow(10.0, power);
		
		NumValue stepVal = new NumValue(step, -power);
		setFakeNumLabels(maxRelFreq, stepVal);
	}
	
	private void setupNoLabels() {
		labels.removeAllElements();
	}
	
	public boolean changeMaximumDensity(double maxDensity, int maxWidth) {
													//		maxWidth is not used for this type of HistoDensityInfo
		
		if (maxDensity > baseMaxDensity) {
			initialiseDensityScale(maxDensity);
			setupLabels(axisType, classWidth, noOfVals);
		}
		else if (currentMaxDensity > baseMaxDensity) {
			initialiseDensityScale(baseMaxDensity);
			setupLabels(axisType, classWidth, noOfVals);
		}
		else
			return false;
		return true;
	}
	
	public int densityToPosition(double density) throws AxisException {
		return numValToPosition(density);
	}
	
	public double positionToDensity(int yPos) throws AxisException {
		return positionToNumVal(yPos);
	}
}
