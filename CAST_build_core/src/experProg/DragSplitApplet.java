package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;

import exper.*;


public class DragSplitApplet extends XApplet {
	static final private String BOUNDARY_PARAM = "boundary";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String LOW_GROUP_PARAM = "lowMean";
	static final private String HIGH_GROUP_PARAM = "highMean";
	static final private String DECIMALS_PARAM = "decimals";
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(20, 0));
		add("Center", displayPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
			
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
			
			StringTokenizer st = new StringTokenizer(getParameter(BOUNDARY_PARAM));
			double boundary = Double.parseDouble(st.nextToken());
			double minBoundary = Double.parseDouble(st.nextToken());
			double maxBoundary = Double.parseDouble(st.nextToken());
			int meanDecimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			DragSplitDotPlotView theDotPlot = new DragSplitDotPlotView(data, this, theHorizAxis, boundary,
															minBoundary, maxBoundary, new LabelValue(getParameter(LOW_GROUP_PARAM)),
															new LabelValue(getParameter(HIGH_GROUP_PARAM)), meanDecimals);
									
		thePanel.add("Center", theDotPlot);
			theDotPlot.lockBackground(Color.white);
		
		return thePanel;
	}
}