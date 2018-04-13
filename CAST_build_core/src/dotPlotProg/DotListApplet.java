package dotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

import dotPlot.*;


public class DotListApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "vertAxis";
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new ProportionLayout(0.5, 40));
		
		add("Left", listPanel(data));
		add("Right", dotPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	private XPanel listPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("North", new XLabel(getParameter(VAR_NAME_PARAM), XLabel.LEFT, this));
		
		NumberList2 theList = new NumberList2(data, this);
		theList.setRetainLastSelection(true);
		thePanel.add("Center", theList);
		theList.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel dotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("North", new XLabel(getParameter(VAR_NAME_PARAM), XLabel.LEFT, this));
		
		XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
			VertAxis theVertAxis = new VertAxis(this);
				String labelInfo = getParameter(AXIS_INFO_PARAM);
				theVertAxis.readNumLabels(labelInfo);
			plotPanel.add("Left", theVertAxis);
			
				DotPlotView theDotPlot = new DotPlotView(data, this, theVertAxis);
				theDotPlot.lockBackground(Color.white);
				theDotPlot.setViewBorder(new Insets(5, 15, 5, 15));
				theDotPlot.setCrossSize(DataView.LARGE_CROSS);
				theDotPlot.setRetainLastSelection(true);
			plotPanel.add("Center", theDotPlot);
		thePanel.add("Center", plotPanel);
		
		return thePanel;
	}
}