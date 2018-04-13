package contin;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class Chi2ValueView extends ValueImageView {
//	static final private String CHI2VIEW = "Chi2View";
	
	static final public int OVER_EXPECTED = 0;
	static final public int NOT_OVER_EXPECTED = 1;
	static final public int BRIEF = 2;
	
	private ObsExpTableView oeView;
	private int chi2Type;
	private NumValue maxValue;
	
	public Chi2ValueView(DataSet theData, XApplet applet, ObsExpTableView oeView, int chi2Type, NumValue maxValue) {
		super(theData, applet, 
				chi2Type == OVER_EXPECTED ? "chi2/chi2.png" : chi2Type == BRIEF ? "chi2/chi2Short.png"
																		: "chi2/chi2Wrong.png",
				chi2Type == OVER_EXPECTED ? 32 : chi2Type == BRIEF ? 20 : 18);
		this.oeView = oeView;
		this.chi2Type = chi2Type;
		this.maxValue = maxValue;
	}
	
	public void setMaxValue(NumValue maxValue) {
		this.maxValue = maxValue;
	}

	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		double total = 0.0;
		if (chi2Type != NOT_OVER_EXPECTED)
			total = oeView.getChi2();
		else {
			int[][] obs = oeView.getObservedArray();
			double[][] exp = oeView.getExpectedArray();
			for (int j=0 ; j<obs.length ; j++)
				for (int i=0 ; i<obs[0].length ; i++) {
					double diff = obs[j][i] - exp[j][i];
					double term = diff * diff;
					if (chi2Type != NOT_OVER_EXPECTED)
						term /= exp[j][i];
					total += term;
				}
		}
		return new NumValue(total, maxValue.decimals).toString();	
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
