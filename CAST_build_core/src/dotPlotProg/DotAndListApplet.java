package dotPlotProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import valueList.*;


public class DotAndListApplet extends XApplet {
	static final private String VARIABLES_PARAM = "variables";
	static final private String AXIS_INFO_PARAM = "yAxis";
	
	private int noOfVariables, displayIndex;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new ProportionLayout(0.4, 10, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, dotPlotPanel(data));
		
		add(ProportionLayout.BOTTOM, valueListPanel(data));
	}
	
	protected String getMainVariableKey() {
		return "y" + (displayIndex + 1);
	}
	
	protected DataSet readData() {
		StringTokenizer st = new StringTokenizer(getParameter(VARIABLES_PARAM));
		noOfVariables = Integer.parseInt(st.nextToken());
		displayIndex = Integer.parseInt(st.nextToken());
		
		DataSet data = new DataSet();
		for (int i=1 ; i<=noOfVariables ; i++) {
			String labelsString = getParameter(LABELS_PARAM + i);
			if (labelsString != null)
				data.addLabelVariable("y" + i, getParameter(VAR_NAME_PARAM + i), labelsString);
			else {
				String catLabelsString = getParameter(CAT_LABELS_PARAM + i);
				if (catLabelsString == null)
					data.addNumVariable("y" + i, getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i));
				else
					data.addCatVariable("y" + i, getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i), catLabelsString);
			}
		}
		return data;
	}
	
	protected XPanel valueListPanel(DataSet data) {
//		ScrollImages.loadScroll(this);
		
		ScrollValueList theScrollList = new ScrollValueList(data, this, ScrollValueList.HEADING);
		
		for (int i=1 ; i<=noOfVariables ; i++)
			theScrollList.addVariableToList("y" + i, ScrollValueList.RAW_VALUE);
		theScrollList.setSelectedCols(displayIndex, -1);
		
		return theScrollList;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			CoreVariable yVar = data.getVariable(getMainVariableKey());
			theHorizAxis.setAxisName(yVar.name);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedDotPlotView theDotPlot = new StackedDotPlotView(data, this, theHorizAxis);
			theDotPlot.lockBackground(Color.white);
			theDotPlot.setRetainLastSelection(true);
			theDotPlot.setActiveNumVariable(getMainVariableKey());
		thePanel.add("Center", theDotPlot);
		
		return thePanel;
	}
}