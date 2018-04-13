package stdError;

import java.awt.*;

import dataView.*;
import distn.*;
import imageUtils.*;


public class PErrorSDValueView extends ValueImageView {
	
	private String normalApproxKey, xKey;
	
	private NumValue tempValue;
	
	public PErrorSDValueView(DataSet theData, XApplet applet, String normalApproxKey, String xKey,
																	int decimals, String gifFile, int ascent) {
		super(theData, applet, gifFile, ascent);
		this.normalApproxKey = normalApproxKey;
		this.xKey = xKey;
		tempValue = new NumValue(0.0, decimals);
	}
	
	public PErrorSDValueView(DataSet theData, XApplet applet, String normalApproxKey, String xKey, int decimals) {
		this(theData, applet, normalApproxKey, xKey, decimals,
													(xKey == null) ? "ci/normalErrorApproxSD.png" : "xEquals/equals.png", (xKey == null) ? 29 : 7);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return tempValue.stringWidth(g);
	}
	
	protected String getValueString() {
		if (xKey == null) {
			NormalDistnVariable dist = (NormalDistnVariable)getVariable(normalApproxKey);
			tempValue.setValue(dist.getSD().toDouble());
		}
		else {
			CatVariable xVar = (CatVariable)getVariable(xKey);
			int n = xVar.noOfValues();
			int counts[] = xVar.getCounts();
			double p = counts[0] / (double)n;
			tempValue.setValue(Math.sqrt(p * (1 - p) / n));
		}
		return tempValue.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
