package inference;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class PCIValueView extends ValueImageView {
	
	static final private String kPlusMinus = "  \u00B1  ";
	
	private String xKey;
	
	private NumValue pValue, plusMinusValue;
	
	public PCIValueView(DataSet theData, XApplet applet, String xKey, int decimals) {
		super(theData, applet, "xEquals/pCI.png", 29);
		this.xKey = xKey;
		pValue = new NumValue(0.0, decimals);
		plusMinusValue = new NumValue(0.0, decimals);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return 2 * pValue.stringWidth(g) + g.getFontMetrics().stringWidth(kPlusMinus);
	}
	
	protected String getValueString() {
		CatVariable x = (CatVariable)getVariable(xKey);
		int counts[] = x.getCounts();
		int n = counts[0] + counts[1];
		double p = counts[0] / (double)n;
		
		pValue.setValue(p);
		plusMinusValue.setValue(2.0 * Math.sqrt(p * (1 - p) / n));
		
		return pValue.toString() + kPlusMinus + plusMinusValue.toString();
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
