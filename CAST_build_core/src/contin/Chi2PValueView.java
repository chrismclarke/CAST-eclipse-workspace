package contin;

import java.awt.*;

import dataView.*;
import valueList.*;
import distn.*;


public class Chi2PValueView extends ValueView {
//	static final private String CHI2PVALUEVIEW = "Chi2PView";
	
	static final private NumValue kMaxPValue = new NumValue(1.0, 4);
	
	private String pValueLabel;
	
	private ObsExpTableView oeView;
	
	public Chi2PValueView(DataSet theData, XApplet applet, ObsExpTableView oeView) {
		super(theData, applet);
		pValueLabel = applet.translate("p-value") + " = ";
		this.oeView = oeView;
	}
	
	public int getDecimals() {
		return kMaxPValue.decimals;
	}

	protected int getMaxValueWidth(Graphics g) {
		return kMaxPValue.stringWidth(g);
	}
	
	protected String getValueString() {
		int df = oeView.getDF();
		double chi2 = oeView.getChi2();
		double pValue = 1.0 - Chi2Table.cumulative(chi2, df);
		return new NumValue(pValue, getDecimals()).toString();
	}
	
	protected boolean highlightValue() {
		return true;
	}
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(pValueLabel);
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(pValueLabel, startHoriz, baseLine);
	}
}
