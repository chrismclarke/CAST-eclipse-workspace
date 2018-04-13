package varianceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;


import variance.*;
import ssq.*;


public class Group2ComponentsApplet extends SimpleComponentsApplet {
	
	protected CoreModelDataSet getData() {
		CoreModelDataSet data = new GroupsDataSet(this);
		
		int decimals = data.getResponseDecimals();
		for (int i=0 ; i<componentKeys().length ; i++) {
			String key = componentKeys()[i];
			int compType = TwoGroupComponentVariable.kComponentType[i];
			TwoGroupComponentVariable comp = new TwoGroupComponentVariable(key, data, "x", "y",
																													"ls", compType, decimals);
			data.addVariable(key, comp);
		}
		
		return data;
	}
	
	protected boolean canTakeSample() {
		return false;
	}
	
	protected DataWithComponentsPanel getDataComponentsView(DataSet data) {
		Group2DataComponentsPanel thePanel = new Group2DataComponentsPanel(this);
		thePanel.setupPanel(data, "x", "y", "ls", null, TwoGroupComponentVariable.TOTAL, this);
		thePanel.getView().setRetainLastSelection(true);
		return thePanel;
	}
	
	protected String[] componentKeys() {
		return TwoGroupComponentVariable.kComponentKey;
	}
	
	protected Color[] componentColors() {
		return TwoGroupComponentVariable.kComponentColor;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = super.rightPanel(data);
			
		componentPlot.getView().setShowSD(true);
			
		thePanel.add("South", componentChoicePanel());
				
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			
		AnovaImages.loadGroup2Images(this);
		
			FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
			theEquation = new ComponentEqnPanel(data, componentKeys(), 
							null, AnovaImages.group2Devns, componentColors(), AnovaImages.kGroup2DevnWidth,
							AnovaImages.kGroup2DevnHeight, stdContext);
		thePanel.add(theEquation);
		
			ssqEquation = new ComponentEqnPanel(summaryData, componentKeys(), 
							maxSsq, AnovaImages.group2Ssqs, componentColors(), AnovaImages.kGroup2SsqWidth,
							AnovaImages.kGroup2SsqHeight, stdContext);
		thePanel.add(ssqEquation);
			
		return thePanel;
	}
}