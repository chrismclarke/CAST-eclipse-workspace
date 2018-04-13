package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;


import ssq.*;

public class ComponentsGroupApplet extends Components2Applet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	
	static final private int kR2Decimals = 3;
	
	private ComponentEqnPanel ssqEquation;
	private NumValue maxSsq;
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
												componentKeys(), maxSsq.decimals, kR2Decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	protected boolean canTakeSample() {
		return false;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(50, 0));
		
		thePanel.add("Center", super.dataPanel(data));
		
		thePanel.add("East", new XPanel());
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = super.rightPanel(data);
			
		componentPlot.getView().setShowSD(true);
			
		thePanel.add("South", componentChoicePanel());
				
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			
			AnovaImages.loadGroupImages(this);
		
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			theEquation = new ComponentEqnPanel(data, componentKeys(), 
							null, AnovaImages.basicGroupDevns, componentColors(), AnovaImages.kDevnWidth,
							AnovaImages.kDevnHeight, bigContext);
		thePanel.add(theEquation);
		
			ssqEquation = new ComponentEqnPanel(summaryData, componentKeys(), 
							maxSsq, AnovaImages.basicGroupSsqs, componentColors(), AnovaImages.kSsq2Width,
							AnovaImages.kSsqHeight, bigContext);
		thePanel.add(ssqEquation);
			
		return thePanel;
	}
	
	protected void highlightEquation(int newComponentIndex) {
		super.highlightEquation(newComponentIndex);
		ssqEquation.highlightComponent(newComponentIndex);
	}
}