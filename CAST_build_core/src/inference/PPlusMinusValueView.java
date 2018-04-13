package inference;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class PPlusMinusValueView extends ValueImageView {
	
	private String xKey;
	
	private NumValue plusMinusValue;
	
	private boolean ciNotMarginOfError = true;
	
	public PPlusMinusValueView(DataSet theData, XApplet applet, String xKey, int decimals) {
		super(theData, applet, "ci/pPlusMinus.png", 12);
		this.xKey = xKey;
		plusMinusValue = new NumValue(0.0, decimals);
	}
	
	public void setCiNotMarginOfError(boolean ciNotMarginOfError) {
		this.ciNotMarginOfError = ciNotMarginOfError;
		repaint();
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return plusMinusValue.stringWidth(g);
	}
	
	protected String getValueString() {
		CatVariable x = (CatVariable)getVariable(xKey);
		int counts[] = x.getCounts();
		int n = counts[0] + counts[1];
		
		if (ciNotMarginOfError) {
			double p =  counts[0] / (double)n;
			plusMinusValue.setValue(2.0 * Math.sqrt(p * (1 - p) / n));
		}
		else
			plusMinusValue.setValue(1.0 / Math.sqrt(n));
		
		return plusMinusValue.toString();
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
