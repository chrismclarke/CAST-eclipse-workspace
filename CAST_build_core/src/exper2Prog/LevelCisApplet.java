package exper2Prog;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import models.*;


import exper2.*;


public class LevelCisApplet extends XApplet {
	
	private GroupsDataSet data;
	private SummaryDataSet summaryData;
	
	private LevelCisView theView;
	
	private XCheckbox pooledSdCheck;
	private XButton sampleButton;
	private XChoice dataSetChoice;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		doTakeSample();
		
		setLayout(new BorderLayout(20, 5));
		
		add("Center", displayPanel(data));
		
		add("South", bottomPanel(summaryData));
	}
	
	private GroupsDataSet getData() {
		return new GroupsDataSet(this);
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
				
				theView = new LevelCisView(data, this, xAxis, yAxis, "x", "y", "ls");
				theView.lockBackground(Color.white);
				
			scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(data.getYVarName(), XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	private SummaryDataSet getSummaryData(GroupsDataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "error");
		return summaryData;
	}
	
	protected XPanel bottomPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
			pooledSdCheck = new XCheckbox(translate("Use pooled st devn"), this);
		thePanel.add(pooledSdCheck);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
			
				dataSetChoice = data.dataSetChoice(this);
			if (dataSetChoice != null)
				bottomPanel.add(dataSetChoice);
			
				sampleButton = new XButton(translate("Another sample"), this);
			bottomPanel.add(sampleButton);
		
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
	
	private void doTakeSample() {
		summaryData.takeSample();
		data.updateForNewSample();
		data.variableChanged("ls");
		summaryData.redoLastSummary();
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			if (data.changeDataSet(dataSetChoice.getSelectedIndex())) {
				data.variableChanged("y");
				summaryData.redoLastSummary();
			}
			return true;
		}
		else if (target == sampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == pooledSdCheck) {
			GroupsModelVariable lsFit = (GroupsModelVariable)data.getVariable("ls");
			lsFit.setUsePooledSd(pooledSdCheck.getState());
			lsFit.updateLSParams("y");
			theView.repaint();
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}