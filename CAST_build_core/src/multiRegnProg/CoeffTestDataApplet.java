package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;

import multiRegn.*;


public class CoeffTestDataApplet extends CoeffTestApplet {
	private XChoice dataSetChoice;
	private int currentDataSetIndex = 0;
	
	private XTextArea dataDescription;
	private XTextArea dataConclusion;
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected void addDescription(DataSet data, XPanel thePanel) {
		MultiRegnDataSet modelData = (MultiRegnDataSet)data;
		XPanel descPanel = new XPanel();
		descPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 5));
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
				dataSetChoice = modelData.dataSetChoice(this);
			samplePanel.add(dataSetChoice);
		
		descPanel.add(samplePanel);
		
		dataDescription = new XTextArea(modelData.getDescriptionStrings(), 0, 200, this);
		dataDescription.lockBackground(Color.white);
		
		descPanel.add(dataDescription);
		
		thePanel.add(descPanel);
	}
	
	protected XPanel getSamplePanel() {
		return null;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", super.controlPanel(data));
		thePanel.add("South", conclusionPanel(data));
		return thePanel;
	}
	
	private XPanel conclusionPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				XLabel conclusionLabel = new XLabel("Conclusion:", XLabel.LEFT, this);
				conclusionLabel.setFont(getBigBoldFont());
			leftPanel.add(conclusionLabel);
			
		thePanel.add("West", leftPanel);
		
			MultiRegnDataSet modelData = (MultiRegnDataSet)data;
			dataConclusion = new XTextArea(modelData.getQuestionStrings(), 0, 400, this);
			dataConclusion.lockBackground(Color.white);
		
		thePanel.add("Center", dataConclusion);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != currentDataSetIndex) {
				currentDataSetIndex = newChoice;
				data.changeDataSet(newChoice, dataDescription, dataConclusion);
				
				xAxis.setNumScale(data.getXAxisInfo());
				xAxis.setLabelName(data.getXVarName());
				yAxis.setNumScale(data.getYAxisInfo());
				yAxis.setLabelName(data.getYVarName());
				zAxis.setNumScale(data.getZAxisInfo());
				zAxis.setLabelName(data.getZVarName());
				
				NumVariable yVar = (NumVariable)data.getVariable("y");
				errorDF = yVar.noOfValues() - 3;
				for (int i=0 ; i<2 ; i++)
					tView[i].setDistnLabel(new LabelValue("t(" + errorDF + " df)"), kDistnLabelColor);
				
				setCoeffNames();
				for (int i=1 ; i<3 ; i++) {
					String coeffKey = "b" + i;
					SingleLSCoeffVariable coeffVar = (SingleLSCoeffVariable)summaryData.getVariable(coeffKey);
					coeffVar.name = coeffName[i];
				}
				summaryData.setSingleSummaryFromData();
				data.variableChanged("y");
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what)) 
			return true;
		else
			return localAction(evt.target);
	}
}