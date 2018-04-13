package ssqProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import ssq.*;


public class VariabilityApplet extends XApplet {
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String INITIAL_PROPNS_PARAM = "initialPropns";
	static final private String EXPLAINED_PARAM = "explainedName";
	static final private String UNEXPLAINED_PARAM = "unexplainedName";
	
	static final private int kMaxSliderSteps = 100;
	
	protected DataSet data;
	
	private double initialWithinPropn, initialBetweenPropn;
	private XSlider withinSlider, betweenSlider;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new ProportionLayout(0.5, 20, ProportionLayout.HORIZONTAL,
															ProportionLayout.TOTAL));
	
			DataWithComponentsPanel scatterPanel = new DataWithComponentsPanel(this);
			scatterPanel.setupPanel(data, "x", "modY", "lsMod", null, -1, this);
		add(ProportionLayout.LEFT, scatterPanel);
		add(ProportionLayout.RIGHT, controlPanel(data));
	}
	
	private DataSet getData() {
		boolean regnNotGroups = getParameter(X_LABELS_PARAM) == null;
		
		CoreModelDataSet data = null;
		if (regnNotGroups)
			data = new SimpleRegnDataSet(this);
		else
			data = new GroupsDataSet(this);
		
			NumVariable yVar = (NumVariable)data.getVariable("y");
			int decimals = yVar.getMaxDecimals();
			String yName = yVar.name;
			
			AdjustedSsqVariable modY = null;
			if (regnNotGroups)
				modY = new RegnAdjustedSsqVariable(yName, data, "x", "y", decimals);
			else
				modY = new GroupsAdjustedSsqVariable(yName, data, "x", "y", decimals);
		data.addVariable("modY", modY);
				
			StringTokenizer st = new StringTokenizer(getParameter(INITIAL_PROPNS_PARAM));
			initialWithinPropn = Double.parseDouble(st.nextToken());
			initialWithinPropn = initialWithinPropn * initialWithinPropn;
			initialBetweenPropn = Double.parseDouble(st.nextToken());
			initialBetweenPropn = initialBetweenPropn * initialBetweenPropn;
																											// sqr for nonlinear slider scales
			
			modY.setExplainedPropn(initialBetweenPropn);
			modY.setResidPropn(initialWithinPropn);
		
			CoreModelVariable lsMod = null;
			if (regnNotGroups)
				lsMod = new LinearModel("lsMod", data, "x");
			else
				lsMod = new GroupsModelVariable("lsMod", data, "x");
			lsMod.updateLSParams("modY");
		
		data.addVariable("lsMod", lsMod);
			
		return data;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
													VerticalLayout.VERT_CENTER, 20));
		
		String withinName = getParameter(UNEXPLAINED_PARAM);
		if (withinName != null) {
			withinSlider = new XNoValueSlider(translate("Low"), translate("High"), withinName,
							0, kMaxSliderSteps,
							(int)Math.round(kMaxSliderSteps * Math.sqrt(initialWithinPropn)), this);
																											// sqrt for nonlinear slider scales
			thePanel.add(withinSlider);
		}
		
		String betweenName = getParameter(EXPLAINED_PARAM);
		if (betweenName != null) {
			betweenSlider = new XNoValueSlider(translate("Low"), translate("High"), betweenName,
							0, kMaxSliderSteps,
							(int)Math.round(kMaxSliderSteps * Math.sqrt(initialBetweenPropn)), this);
																											// sqrt for nonlinear slider scales
			thePanel.add(betweenSlider);
		}
		
		return thePanel;
	}
	
	private void updateAll() {
		CoreModelVariable lsMod = (CoreModelVariable)data.getVariable("lsMod");
		lsMod.updateLSParams("modY");
		data.variableChanged("modY");
	}

	
	private boolean localAction(Object target) {
		AdjustedSsqVariable yVar = (AdjustedSsqVariable)data.getVariable("modY");
		if (target == withinSlider) {
			double residPropn = withinSlider.getValue() / (double)kMaxSliderSteps;
			residPropn = residPropn * residPropn;
																											// sqr for nonlinear slider scales
			yVar.setResidPropn(residPropn);
			updateAll();
			return true;
		}
		else if (target == betweenSlider) {
			double explPropn = betweenSlider.getValue() / (double)kMaxSliderSteps;
			explPropn = explPropn * explPropn;
																											// sqr for nonlinear slider scales
			yVar.setExplainedPropn(explPropn);
			updateAll();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}