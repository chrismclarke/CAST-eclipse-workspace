package cat;

import java.awt.*;

import axis.*;
import dataView.*;


public class CountPropnAxis extends MultiVertAxis {
	private int maxCount;
	
//	private boolean initialised = false;
	
	public CountPropnAxis(XApplet applet) {
		super(applet, 3);
	}
	
	public void setUpAxes(int maxCount, int labelStep, String maxPropn, String propnLabelStep) {
		readNumLabels("0.0 " + maxPropn + " 0.0 " + propnLabelStep);
		readExtraNumLabels("0 " + maxCount + " 0 " + labelStep);
			NumValue step = new NumValue(propnLabelStep);
			step.setValue(step.toDouble() * 100);
			step.decimals = Math.max(step.decimals - 2, 0);
		readExtraNumLabels("0 " + (Double.parseDouble(maxPropn) * 100.0)
																			+ " 0 " + step.toString());
		this.maxCount = maxCount;
		setStartAlternate(1);		//		start displaying counts
	}
	
	public double getAxisProportion(int count) {
		return ((double)count) / maxCount * maxOnAxis;
	}
	
/*	
	public boolean setAlternateLabels(int index) {
		if (initialised)
			return super.setAlternateLabels(index);
		else {
			setStartAlternate(index);
			return true;
		}
	}
*/	
	public void corePaint(Graphics g) {
//		initialised = true;
		super.corePaint(g);
	}
}