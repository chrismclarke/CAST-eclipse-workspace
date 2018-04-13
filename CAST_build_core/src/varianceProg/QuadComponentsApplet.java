package varianceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;


import variance.*;
import ssq.*;
import ssqProg.*;

public class QuadComponentsApplet extends Components2Applet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	
	static final private int kR2Decimals = 3;
	
	private NumValue maxSsq;
	private ComponentEqnPanel ssqEquation;
	
	protected CoreModelDataSet getData() {
		CoreModelDataSet data = new QuadRegnDataSet(this);
		
		QuadComponentVariable.addComponentsToData(data, "x", "y",
																				QuadRegnDataSet.kLinLsKey, QuadRegnDataSet.kQuadLsKey);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
												componentKeys(), maxSsq.decimals, kR2Decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	protected DataWithComponentsPanel getDataComponentsView(DataSet data) {
		QuadraticComponentsPanel thePanel = new QuadraticComponentsPanel(this);
		thePanel.setupPanel(data, "x", "y", QuadRegnDataSet.kLinLsKey,
							QuadRegnDataSet.kQuadLsKey, null, QuadComponentVariable.TOTAL, this);
		
		thePanel.getView().setRetainLastSelection(true);
		return thePanel;
	}
	
	protected String[] componentKeys() {
		return QuadComponentVariable.kComponentKey;
	}
	
	protected Color[] componentColors() {
		return QuadComponentVariable.kComponentColor;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("East", super.rightPanel(data));
			componentPlot.getView().setShowSD(true);
			
		thePanel.add("South", componentChoicePanel());
				
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			AnovaImages.loadQuadImages(this);
			
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			theEquation = new ComponentEqnPanel(data, componentKeys(), 
								null, AnovaImages.quadDevns, componentColors(),
								AnovaImages.kQuadDevnWidth, AnovaImages.kQuadDevnHeight, bigContext);
			
		thePanel.add(theEquation);
		
			ssqEquation = new ComponentEqnPanel(summaryData, componentKeys(), 
							maxSsq, AnovaImages.quadSsqs, componentColors(), AnovaImages.kQuadSsqWidth,
							AnovaImages.kQuadSsqHeight, bigContext);
		thePanel.add(ssqEquation);
		
		return thePanel;
	}
	
	protected void highlightEquation(int newComponentIndex) {
		super.highlightEquation(newComponentIndex);
		ssqEquation.highlightComponent(newComponentIndex);
	}
}