package multiRegnProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import formula.*;
import graphics3D.*;

import multiRegn.*;
import ssq.*;


public class ComponentsSsqApplet extends ComponentsApplet {
	
	private ComponentEqnPanel ssqEquation;
	private XTextArea dataDescription;
	
	private XChoice dataSetChoice;
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 8);
		thePanel.setLayout(new BorderLayout(0, 3));
		MultiRegnDataSet multiData = (MultiRegnDataSet)data;

			dataSetChoice = multiData.dataSetChoice(this);
			if (dataSetChoice != null) {
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				choicePanel.add(dataSetChoice);
				
				thePanel.add("North", choicePanel);
			}
			
			dataDescription = new XTextArea(multiData.getDescriptionStrings(), 0, 450, this);
			dataDescription.lockBackground(Color.white);
		
		thePanel.add("Center", dataDescription);
		return thePanel;
	}
	
	protected boolean sdDisplayType() {
		return ComponentPlotPanel.SHOW_SD;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(8, 0));
		
			XPanel rotatePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
			rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
			
		thePanel.add("West", rotatePanel);
		
		thePanel.add("Center", super.eastPanel(data));
				
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 8, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("East", componentChoicePanel(data));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
				AnovaImages.loadRegnImages(this);
				FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
				ssqEquation = new ComponentEqnPanel(summaryData, BasicComponentVariable.kComponentKey, 
						maxSsq, AnovaImages.basicRegnSsqs, BasicComponentVariable.kComponentColor, AnovaImages.kSsq2Width,
						AnovaImages.kSsqHeight, bigContext);
			
			leftPanel.add(ssqEquation);
			
				RSquaredPanel r2 = new RSquaredPanel(summaryData, BasicComponentVariable.kComponentKey[1],
															BasicComponentVariable.kComponentKey[0], "rSquared", maxSsq, kMaxR2, true,
															bigContext);
			
			leftPanel.add(r2);
		
		thePanel.add("Center", leftPanel);
		return thePanel;
	}
	
	protected void changeComponentDisplayed(int index) {
		super.changeComponentDisplayed(index);
		ssqEquation.highlightComponent(index);
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			data.changeDataSet(dataSetChoice.getSelectedIndex(), dataDescription);
			setMeanModel(data);
			xAxis.setNumScale(data.getXAxisInfo());
			xAxis.setLabelName(data.getXVarName());
			zAxis.setNumScale(data.getZAxisInfo());
			zAxis.setLabelName(data.getZVarName());
			yAxis.setNumScale(data.getYAxisInfo());
			yAxis.setLabelName(data.getYVarName());
			data.variableChanged("y");
			
			VertAxis componentAxis = componentPlot.getAxis();
			componentAxis.readNumLabels(data.getSummaryAxisInfo());
			componentAxis.repaint();
			summaryData.setSingleSummaryFromData();
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