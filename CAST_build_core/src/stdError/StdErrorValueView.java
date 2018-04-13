package stdError;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class StdErrorValueView extends ValueImageView {
	static final private String kStdErrorImageName = "ci/stdErrorFormula.png";		//	uses s
	static final private String kStdError2ImageName = "ci/stdError2Formula.png";	//	uses sigma
	
	private String distnKey;
	private NumValue maxSD = null;
	
	public StdErrorValueView(DataSet theData, XApplet applet, String distnKey,
																				boolean usesS, NumValue maxSD) {
		super(theData, applet, usesS ? kStdErrorImageName : kStdError2ImageName, 18);
		this.distnKey = distnKey;
		this.maxSD = maxSD;
		setForeground(Color.red);
	}
	
	public StdErrorValueView(DataSet theData, XApplet applet, String distnKey, boolean usesS) {
		this(theData, applet, distnKey, usesS, null);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		if (maxSD != null)
			return maxSD.stringWidth(g);
		else {
			DistnVariable y = (DistnVariable)getVariable(distnKey);
			return y.getSD().stringWidth(g);
		}
	}
	
	protected String getValueString() {
		DistnVariable y = (DistnVariable)getVariable(distnKey);
		NumValue value = y.getSD();
		return value.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}