package contin;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class Chi2DFValueView extends ValueImageView {
//	static final private String CHI2DFVIEW = "Chi2DFView";
	
	static final private String kMaxDFString = "999";
	
	private ObsExpTableView oeView;
	
	public Chi2DFValueView(DataSet theData, XApplet applet, ObsExpTableView oeView) {
		super(theData, applet, "chi2/df.png", 15);
		this.oeView = oeView;
	}

	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kMaxDFString);
	}
	
	protected String getValueString() {
		return String.valueOf(oeView.getDF());	
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
