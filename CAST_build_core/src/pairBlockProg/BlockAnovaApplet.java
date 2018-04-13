package pairBlockProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import multiRegn.*;
import ssq.*;
import pairBlock.*;

public class BlockAnovaApplet extends BlockTreatComponentApplet {
	static final private String JITTER_PARAM = "jitter";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	
	protected BlockDotPlotView dotPlot;
	
	protected XPanel dataPanel(MultiRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel groupLabel = new XLabel(data.getVariable("x").name, XLabel.LEFT, this);
		thePanel.add("North", groupLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis yAxis = new HorizAxis(this);
				String labelInfo = data.getYAxisInfo();
				yAxis.readNumLabels(labelInfo);
				yAxis.setAxisName(data.getYVarName());
			innerPanel.add("Bottom", yAxis);
			
				VertAxis treatAxis = new VertAxis(this);
				CatVariable treatVariable = (CatVariable)data.getVariable("x");
				treatAxis.setCatLabels(treatVariable);
			innerPanel.add("Left", treatAxis);
			
				double jitter = Double.parseDouble(getParameter(JITTER_PARAM));
				dotPlot = new BlockDotPlotView(data, this, "x", "z", yAxis, treatAxis, jitter);
				dotPlot.setShowBlocks(true);
				dotPlot.setActiveNumVariable("y");
				dotPlot.setCrossSize(DataView.LARGE_CROSS);
				dotPlot.lockBackground(Color.white);
			innerPanel.add("Center", dotPlot);
		
		thePanel.add("Center", innerPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			String componentName[] = new String[4];
			for (int i=0 ; i<4 ; i++)
				componentName[i] = st.nextToken();
		
			AnovaTableView tableView = new AnovaTableView(summaryData, this,
										SeqXZComponentVariable.kZXComponentKey, maxSsq, maxMss, maxF, AnovaTableView.SSQ_F_PVALUE);
			tableView.setComponentNames(componentName);
			tableView.setComponentColors(SeqXZComponentVariable.kComponentColor);
				
		thePanel.add(tableView);
		
		return thePanel;
	}
}