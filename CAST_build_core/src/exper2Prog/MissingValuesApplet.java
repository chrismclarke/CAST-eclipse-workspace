package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import models.*;


import ssq.*;
import exper2.*;


public class MissingValuesApplet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	
	static final private Color kTableBackgroundColor = new Color(0xDAE4FF);
	
	private GroupsDataSet data;
	private AnovaSummaryData summaryData;
	private NumValue maxSsq, maxMss, maxF, maxRSquared;
	
	private LevelCisView theView;
	private XButton resetButton;
	
	public void setupApplet() {
		data = getData();
		
		summaryData = getSummaryData(data);
			summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(20, 5));
		
			XPanel listPanel = new InsetPanel(0, 0, 0, 60);
			listPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			listPanel.add(valueList(data));
			
				resetButton = new XButton(translate("Reset"), this);
				resetButton.disable();
			listPanel.add(resetButton);
			
		add("West", listPanel);
		
		add("Center", displayPanel(data));
		
		add("South", anovaTablePanel(summaryData));
	}
	
	private GroupsDataSet getData() {
		GroupsDataSet data = new GroupsDataSet(this);
		
			GroupsModelVariable lsFit = (GroupsModelVariable)data.getVariable("ls");
			lsFit.setUsePooledSd(true);
		
			NumVariable yVar = (NumVariable)data.getVariable("y");
			MissingValueVariable yMissing = new MissingValueVariable(yVar.name, yVar);
		
		data.addVariable("yMissing", yMissing);
		
		data.setResponseKey("yMissing");
		data.updateForNewSample();
		data.addBasicComponents();
		
		return data;
	}
	
	private AnovaSummaryData getSummaryData(GroupsDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMss = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		return new AnovaSummaryData(data, "error", BasicComponentVariable.kComponentKey,
																									maxSsq.decimals, maxRSquared.decimals);
	}
	
	private XPanel displayPanel(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
				CatVariable xVar = (CatVariable)data.getVariable("x");
				
				HorizAxis xAxis = new HorizAxis(this);
				xAxis.setCatLabels(xVar);
				xAxis.setAxisName(xVar.name);
				
			scatterPanel.add("Bottom", xAxis);
				
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(data.getYAxisInfo());
				
			scatterPanel.add("Left", yAxis);
				
				theView = new LevelCisView(data, this, xAxis, yAxis, "x", "yMissing", "ls");
				theView.lockBackground(Color.white);
				
			scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(data.getYVarName(), XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	private XPanel valueList(GroupsDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			MissingValuesView theView = new MissingValuesView(data, this, "yMissing", "x");
		thePanel.add(theView);
		
		return thePanel;
	}
	
	private XPanel anovaTablePanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel tablePanel = new InsetPanel(20, 5);
			tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
				String componentName[] = new String[3];
				for (int i=0 ; i<3 ; i++)
					componentName[i] = st.nextToken();
			
				AnovaTableView tableView = new AnovaTableView(summaryData, this,
											BasicComponentVariable.kComponentKey, maxSsq, maxMss, maxF, AnovaTableView.SSQ_F_PVALUE);
				tableView.setComponentNames(componentName);
			
			tablePanel.add(tableView);
		
			tablePanel.lockBackground(kTableBackgroundColor);
		thePanel.add(tablePanel);
		
		return thePanel;
	}
	
	public void recalculateAnova() {
		GroupsModelVariable lsFit = (GroupsModelVariable)data.getVariable("ls");
		lsFit.updateLSParams("yMissing");
		
		summaryData.setSingleSummaryFromData();
		
		MissingValueVariable yVar = (MissingValueVariable)data.getVariable("yMissing");
		if (yVar.anyMissing())
			resetButton.enable();
		else
			resetButton.disable();
	}
	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			MissingValueVariable yVar = (MissingValueVariable)data.getVariable("yMissing");
			yVar.clearMissingValues();
			
			data.variableChanged("yMissing");
			recalculateAnova();
			resetButton.disable();
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}