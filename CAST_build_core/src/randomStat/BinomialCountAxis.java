package randomStat;

import dataView.*;
import axis.*;
import distn.*;


public class BinomialCountAxis extends HorizAxis {
	private DataSet data;
	private String variableKey;
	
	public BinomialCountAxis(DataSet data, String variableKey, XApplet applet) {
		super(applet);
		this.data = data;
		this.variableKey = variableKey;
		setCountLabels();
	}
	
	public void setCountLabels() {
		BinomialDistnVariable y = (BinomialDistnVariable)data.getVariable(variableKey);
		labels.removeAllElements();
		int newN = y.getCount();
		double zeroPos = 0.05 / 1.1;		//	assumes that prob axis is -0.05 to 1.05
		double onePos = 1.05 / 1.1;
		addAxisLabel(new NumValue(0, 0), zeroPos);
		addAxisLabel(new NumValue(newN, 0), onePos);
		minOnAxis = minPower = -0.05 * newN;
		maxOnAxis = maxPower = 1.05 * newN;				//		assumes linear scale
		powerRange = maxOnAxis - minOnAxis;
		repaint();
	}
}