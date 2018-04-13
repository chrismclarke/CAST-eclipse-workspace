package randomStat;

import dataView.*;
import axis.*;

import distribution.*;


public class DiscreteDensityAxis extends VertAxis {
	private DiscreteProbView theView;
	
	public DiscreteDensityAxis(DiscreteProbView theView, XApplet applet) {
		super(applet);
		this.theView = theView;
		show(false);
		setMaxDensity();
	}
	
	public void setMaxDensity() {
		double maxDensity = theView.getMaxProb();
		
		labels.removeAllElements();
		
		minOnAxis = minPower = 0.0;						//		assumes linear scale
		maxOnAxis = maxPower = maxDensity;				//		assumes linear scale
		powerRange = maxOnAxis - minOnAxis;
	}
}