package groupedDotPlotProg;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.*;

import coreGraphics.*;


public class JitteredGroupApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	private DataSet data;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 5));
		add("Center", dotPlotPanel(data));
		add("North", valuePanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addCatVariable("group", getParameter(CAT_NAME_PARAM),
									getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
		
		return data;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			NumVariable yVar = (NumVariable)data.getVariable("y");
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			theHorizAxis.setAxisName(yVar.name);
			
		thePanel.add("Bottom", theHorizAxis);
		
			DotPlotView theDotPlot = new DotPlotView(data, this, theHorizAxis, 1.0);
			if (yVar.noOfValues() > 50)
				theDotPlot.setCrossSize(DataView.SMALL_CROSS);
			theDotPlot.lockBackground(Color.white);
			
		thePanel.add("Center", theDotPlot);
		
		return thePanel;
	}
	
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		thePanel.add(new OneValueView(data, "group", this));
		thePanel.add(new OneValueView(data, "y", this));
		return thePanel;
	}
}