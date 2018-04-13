package randomStat;

import dataView.*;
import axis.*;


public class UniformSumAxis extends HorizAxis {
	private int baseOutcomes;
	
	public UniformSumAxis(int baseOutcomes, XApplet applet) {
		super(applet);
		this.baseOutcomes = baseOutcomes;
		setCountLabels(1);
	}
	
	public void setCountLabels(int sampleSize) {
		int maxCount = baseOutcomes * sampleSize;
		labels.removeAllElements();
		addAxisLabel(new NumValue(sampleSize, 0), 0.5 / baseOutcomes);
		addAxisLabel(new NumValue(maxCount, 0), 1.0 - 0.5 / baseOutcomes);
		minOnAxis = minPower = sampleSize - 0.5 * sampleSize;
		maxOnAxis = maxPower = maxCount + 0.5 * sampleSize;				//		assumes linear scale
		powerRange = maxOnAxis - minOnAxis;
		repaint();
	}
}