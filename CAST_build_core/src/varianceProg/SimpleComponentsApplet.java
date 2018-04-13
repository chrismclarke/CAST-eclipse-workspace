package varianceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;


import variance.*;
import ssq.*;
import ssqProg.*;

public class SimpleComponentsApplet extends Components2Applet {
	static final private String TARGET_PARAM = "target";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String DATA_SPACING_PARAM = "dataSpacing";
	
	static final private int kR2Decimals = 3;
	
	private double target;
	protected NumValue maxSsq;
	
	protected ComponentEqnPanel ssqEquation;
	private XButton sampleButton;
	
	protected CoreModelDataSet getData() {
		CoreModelDataSet data = new GroupsDataSet(this);
		target = Double.parseDouble(getParameter(TARGET_PARAM));
		
		int decimals = data.getResponseDecimals();
		for (int i=0 ; i<componentKeys().length ; i++) {
			String key = componentKeys()[i];
			int compType = SimpleComponentVariable.kComponentType[i];
			SimpleComponentVariable comp = new SimpleComponentVariable(key, data, "y",
																									"model", target, compType, decimals);
			data.addVariable(key, comp);
		}
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
												componentKeys(), maxSsq.decimals, kR2Decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		int spacing = Integer.parseInt(getParameter(DATA_SPACING_PARAM));
		thePanel.setLayout(new BorderLayout(spacing, 0));
		
		thePanel.add("Center", super.dataPanel(data));
		
		thePanel.add("West", new XPanel());
		thePanel.add("East", new XPanel());
		return thePanel;
	}
	
	protected DataWithComponentsPanel getDataComponentsView(DataSet data) {
		SimpleDataComponentsPanel thePanel = new SimpleDataComponentsPanel(target, this);
		thePanel.setupPanel(data, "x", "y", null, "model", SimpleComponentVariable.FROM_TARGET, this);
		thePanel.getView().setRetainLastSelection(true);
		return thePanel;
	}
	
	protected String[] componentKeys() {
		return SimpleComponentVariable.kComponentKey;
	}
	
	protected Color[] componentColors() {
		return SimpleComponentVariable.kComponentColor;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout( 0, 0));
		
			XPanel equationPanel = new XPanel();
			equationPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
				
			AnovaImages.loadRawImages(this);
			
				FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
				theEquation = new ComponentEqnPanel(data, componentKeys(), 
								null, AnovaImages.rawDevns, componentColors(),
								AnovaImages.kRawDevnWidth, AnovaImages.kDevnHeight, bigContext);
			equationPanel.add(theEquation);
			
				ssqEquation = new ComponentEqnPanel(summaryData, componentKeys(), 
								maxSsq, AnovaImages.rawSsqs, componentColors(),
								AnovaImages.kRawSsqWidth, AnovaImages.kRawSsqHeight, bigContext);
			equationPanel.add(ssqEquation);
			
		thePanel.add("Center", equationPanel);
		
			XPanel rightPanel = componentChoicePanel();
				sampleButton = new XButton(translate("Another sample"), this);
			rightPanel.add(sampleButton);
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}
	
	protected void highlightEquation(int newComponentIndex) {
		ssqEquation.highlightComponent(newComponentIndex);
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
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