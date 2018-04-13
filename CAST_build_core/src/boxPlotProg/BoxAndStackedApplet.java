package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import coreGraphics.*;


public class BoxAndStackedApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	static final private Color kBoxBackgroundColor = new Color(0xDDDDDD);
	
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
			theHorizAxis.setAxisName(data.getNumVariable().name);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedDotPlotView dotView = new StackedDotPlotView(data, this, theHorizAxis);
			dotView.lockBackground(Color.white);
		thePanel.add("Center", dotView);
		
			BoxView boxView = new BoxView(data, this, theHorizAxis);
			boxView.lockBackground(kBoxBackgroundColor);
			boxView.setFillColor(Color.white);
		thePanel.add("BottomMargin", boxView);
		
		
		return thePanel;
	}
}