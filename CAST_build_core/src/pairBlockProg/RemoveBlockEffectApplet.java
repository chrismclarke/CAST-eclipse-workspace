package pairBlockProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import ssq.*;
import pairBlock.*;


public class RemoveBlockEffectApplet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	static final private String JITTER_PARAM = "jitter";
	
	private CoreModelDataSet data;
	private AnovaSummaryData summaryData;
	
	private AnimateBlockDiffView theDotPlot;
	
	private NumValue maxSsq, maxMss, maxF, maxRSquared;
	
	private XCheckbox centerBlocksCheck;
	
	public void setupApplet() {
		data = readData();
		
		summaryData = getSummaryData(data);
			summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(10, 10));
		
		add("Center", dataDisplayPanel(data));
		
		add("South", controlPanel(data, summaryData));
	}
	
	private CoreModelDataSet readData() {
		RemoveBlockDataSet data = new RemoveBlockDataSet(this, -1);
		
		data.addBasicComponents();
		
		return data;
	}
	
	private AnovaSummaryData getSummaryData(CoreModelDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMss = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		AnovaSummaryData summaryData =  new AnovaSummaryData(data, "error",
						BasicComponentVariable.kComponentKey, maxSsq.decimals, maxRSquared.decimals);
		return summaryData;
	}
	
	private XPanel dataDisplayPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel groupLabel = new XLabel(data.getVariable("x").name, XLabel.LEFT, this);
		thePanel.add("North", groupLabel);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis responseAxis = new HorizAxis(this);
				String labelInfo = data.getYAxisInfo();
				responseAxis.readNumLabels(labelInfo);
				responseAxis.setAxisName(data.getVariable("y").name);
			innerPanel.add("Bottom", responseAxis);
			
				VertAxis groupAxis = new VertAxis(this);
				CatVariable groupVariable = (CatVariable)data.getVariable("x");
				groupAxis.setCatLabels(groupVariable);
			innerPanel.add("Left", groupAxis);
			
				double jitter = Double.parseDouble(getParameter(JITTER_PARAM));
				theDotPlot = new AnimateBlockDiffView(data, this, "x", "block", responseAxis, groupAxis, jitter);
				theDotPlot.setActiveNumVariable("y");
				theDotPlot.setCrossSize(DataView.LARGE_CROSS);
				theDotPlot.setShowBlocks(true);
				theDotPlot.setDrawType(BlockDotPlotView.BLOCK_MEANS);
				theDotPlot.lockBackground(Color.white);
			innerPanel.add("Center", theDotPlot);
		
		thePanel.add("Center", innerPanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel blockPanel = new XPanel();
			blockPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				centerBlocksCheck = new XCheckbox(translate("Make all block means equal"), this);
				
			blockPanel.add(centerBlocksCheck);
		
		thePanel.add("North", blockPanel);
			
			XPanel anovaPanel = new XPanel();
			anovaPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			anovaPanel.add(anovaTablePanel(summaryData));
			
		thePanel.add("Center", anovaPanel);
		
		return thePanel;
	}
	
	private XPanel anovaTablePanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			String componentName[] = new String[3];
			for (int i=0 ; i<3 ; i++)
				componentName[i] = st.nextToken();
		
			AnovaTableView tableView = new AnovaTableView(summaryData, this, BasicComponentVariable.kComponentKey,
																							maxSsq, maxMss, maxF, AnovaTableView.SSQ_F_PVALUE);
			tableView.setComponentNames(componentName);
		
		thePanel.add(tableView);
		
		return thePanel;
	}
	
	protected void frameChanged(DataView theView) {
		summaryData.redoLastSummary();
	}
	
	public void finishedAnimation(boolean toDifferences) {
		summaryData.redoLastSummary();
		centerBlocksCheck.enable();
	}

	
	private boolean localAction(Object target) {
		if (target == centerBlocksCheck) {
			centerBlocksCheck.disable();
			
			theDotPlot.animateDifferences(centerBlocksCheck.getState());
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}