package histo;

import dataView.*;
import axis.*;


public class DensityAxis2 extends MultiVertAxis implements HistoDensityInfo {
						//		Replaces DensityAxis.
						//		Builds on MultiVertAxis rather than VertAxis.
						//		Does not handle changing MAX density
	public static final int DENSITY_LABELS = 0;
	public static final int COUNT_LABELS = 1;
	public static final int REL_FREQ_LABELS = 2;
	public static final int NO_LABELS = 3;
	
	static final private String kEmptyLabelInfo = "0 1 3 1";
	
	private int axisType;
	
	public DensityAxis2(int axisType, String densityInfo, String freqInfo, String propnInfo,
																								XApplet applet) {
		super(applet, 4);
		readNumLabels(densityInfo);
		readExtraNumLabels(freqInfo == null ? kEmptyLabelInfo : freqInfo);
		readExtraNumLabels(propnInfo == null ? kEmptyLabelInfo : propnInfo);
		readExtraNumLabels(kEmptyLabelInfo);
		
		setStartAlternate(axisType);
		this.axisType = axisType;
	}
	
	public void changeLabelType(int axisType) {
		setAlternateLabels(axisType);
		this.axisType = axisType;
	}
	
	public String getAxisName() {
		switch (axisType) {
			case DENSITY_LABELS:
				return getApplet().translate("Density");
			case COUNT_LABELS:
				return getApplet().translate("Frequency");
			case REL_FREQ_LABELS:
				return getApplet().translate("Relative frequency");
			default:
				return "";
		}
	}
	
	public boolean changeMaximumDensity(double maxDensity, int maxWidth) {
											//		This type of axis cannot handle changing max density
		return false;
	}
	
	public int densityToPosition(double density) throws AxisException {
		return numValToPosition(density);
	}
	
	public double positionToDensity(int yPos) throws AxisException {
		return positionToNumVal(yPos);
	}
}
