package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;

import boxPlot.*;


public class DragBoxValueApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	public void setupApplet() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		DragBoxValueView theView = new DragBoxValueView(data, this, theHorizAxis);
		theView.lockBackground(Color.white);
		theView.setShowIQR(true);
		thePanel.add("Center", theView);
		return thePanel;
	}
}