package glmAnovaProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;

import ssq.*;


public class FactorCombineSsqApplet extends CombineSsqApplet {
	static final private String FACTOR_NAME_PARAM = "factorVarName";
	static final private String FACTOR_LABELS_PARAM = "factorLabels";
	static final private String FACTOR_VALUES_PARAM = "factorValues";
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		CatVariable factor = new CatVariable(getParameter(FACTOR_NAME_PARAM));
		factor.readLabels(getParameter(FACTOR_LABELS_PARAM));
		factor.readValues(getParameter(FACTOR_VALUES_PARAM));
		data.addVariable("factor", factor);
		
			GroupsModelVariable lsFactorModel = new GroupsModelVariable("Factor LS", data, "factor");
			lsFactorModel.updateLSParams("y");
		data.addVariable("lsFactor", lsFactorModel);
		
		for (int i=1 ; i<componentColor.length-1 ; i++)
			componentColor[i] = Color.red;
		componentColor[componentColor.length - 1] = Color.blue;
		
		return data;
	}
	
	protected XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			XPanel graphPanel = new InsetPanel(50, 0);
			graphPanel.setLayout(new BorderLayout(0, 0));
			
				DataWithComponentsPanel scatterPanel = new DataWithComponentsPanel(this);
				scatterPanel.setupPanel(data, "factor", "y", "lsFactor", null,
																									BasicComponentVariable.EXPLAINED, this);
			graphPanel.add("Center", scatterPanel);
		
		thePanel.add("Center", graphPanel);
		
		thePanel.add("South", super.dataDisplayPanel(data));
		
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new InsetPanel(0, 20, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add(super.controlPanel());
		return thePanel;
	}
}