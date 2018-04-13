package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import valueList.ProportionView;

import boxPlot.*;


public class BoxAndDotApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	public void setupApplet() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		BoxDotHiliteView theView = new BoxDotHiliteView(data, this, theHorizAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		ProportionView valueView = new ProportionView(data, "y", this);
		thePanel.add(valueView);
		return thePanel;
	}
}