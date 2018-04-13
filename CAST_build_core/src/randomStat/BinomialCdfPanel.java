package randomStat;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;


import distribution.*;

public class BinomialCdfPanel extends BinomialDistnPanel {
	
	public BinomialCdfPanel(XApplet applet, int maxN, int startN) {
		super(applet, maxN, startN, ProportionLayout.HORIZONTAL, DiscreteProbView.DRAG_CUMULATIVE);
	}
	
	
	protected DataView barChartView(DataSet data, XApplet applet, int dragType, HorizAxis pAxis,
																	BinomialCountAxis nAxis) {
		DiscreteProbView cdf = new DiscreteCdfView(data, applet, "distn", pAxis, nAxis, dragType);
		cdf.setTitleString("F(x)", applet);
		cdf.lockBackground(Color.white);
		
		DiscreteProbView barChart = new DiscreteProbView(data, applet, "distn", null, pAxis, nAxis, dragType);
		barChart.setTitleString("p(x)", applet);
		barChart.lockBackground(Color.white);
		
		return new MultipleDataView(data, applet, cdf, barChart);
	}
	
	protected VertAxis getVertAxis(XApplet applet) {
		return new CdfHalfAxis(applet);
	}
	
	protected XPanel probPanel(DataSet data, int maxN, XApplet applet) {
		return null;
	}
	
}