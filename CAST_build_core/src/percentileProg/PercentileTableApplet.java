package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;

import percentile.*;


public class PercentileTableApplet extends GroupedQuartileApplet {
	static final private String QUANTILES_PARAM = "quantiles";
	static final protected String MAX_VALUE_PARAM = "maxValue";
	
	GroupedPercentileView bandView;
	
	protected XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
				CatVariable xVar = (CatVariable)data.getVariable("x");
			horizAxis.setCatLabels(xVar);
			horizAxis.setAxisName(xVar.name);
		thePanel.add("Bottom", horizAxis);
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", vertAxis);
		
			bandView = new GroupedPercentileView(data, this, vertAxis, horizAxis);
			bandView.setPlotType(GroupedPercentileView.PERCENTILE_PLOT);
			double prob = Double.parseDouble(getParameter(QUANTILES_PARAM));
			bandView.setPercentiles(prob);
			theView = bandView;
		thePanel.add("Center", bandView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			NumValue maxY = new NumValue(getParameter(MAX_VALUE_PARAM));
			PercentileTable percentileTable = new PercentileTable(data, this, bandView, "x", maxY);
		thePanel.add(percentileTable);
		
		return thePanel;
	}
}