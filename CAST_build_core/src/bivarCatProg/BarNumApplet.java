package bivarCatProg;

import java.awt.*;

import dataView.*;
import axis.*;

import bivarCat.*;
import cat.CountPropnAxis;


public class BarNumApplet extends BarTimeApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	protected XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			CountPropnAxis vertAxis = createCountPropnAxis();
		thePanel.add("Left", vertAxis);
		
			HorizAxis numAxis = new HorizAxis(this);
			numAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			horizAxis = numAxis;
		thePanel.add("Bottom", numAxis);
		
			barView = new BarTimeView(data, this, vertAxis, horizAxis, "x", "y", getBarWidth());
			barView.lockBackground(Color.white);
		thePanel.add("Center", barView);
		
		return thePanel;
	}
	
	protected int initialVertIndex() {
		return 1;
	}
}