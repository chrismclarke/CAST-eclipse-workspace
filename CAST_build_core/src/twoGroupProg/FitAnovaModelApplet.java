package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import twoGroup.*;


public class FitAnovaModelApplet extends RotateAnovaParamApplet {
	static final private String ANOVA_MODEL_PARAM = "anovaModel";
	static final private String X_LABELS_PARAM = "xLabels";
	
	private XButton bestButton;
	
	protected DataSet readData() {
		data = new DataSet();
			
			String yVarName = getParameter(Y_VAR_NAME_PARAM);
		data.addNumVariable("y", yVarName, getParameter(Y_VALUES_PARAM));
		data.addCatVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
																													getParameter(X_LABELS_PARAM));
		
			GroupsModelVariable yDistn = new GroupsModelVariable(yVarName, data, "x");
			yDistn.setParameters(getParameter(ANOVA_MODEL_PARAM));
		data.addVariable("model", yDistn);
		return data;
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateAnovaPDFView theView = (RotateAnovaPDFView)super.getView(data, xAxis, yAxis, densityAxis);
		theView.setShowData(true);
		return theView;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, rotationPanel());
		
			XPanel bestPanel = new XPanel();
			bestPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
				bestButton = new XButton(translate("Best fit"), this);
			bestPanel.add(bestButton);
		
		thePanel.add(ProportionLayout.BOTTOM, bestPanel);
		
		return thePanel;
	}
	
	private void setBestParams() {
		GroupsModelVariable yDistn = (GroupsModelVariable)data.getVariable("model");
		yDistn.updateLSParams("y");
		data.variableChanged("model");
		
		double m0 = yDistn.getMean(0).toDouble();
		meanSlider[0].setParameter(m0);
		
		double m1 = yDistn.getMean(1).toDouble();
		meanSlider[1].setParameter(m1);
		
		double s0 = yDistn.getSD(0).toDouble();
		double s1 = yDistn.getSD(1).toDouble();
		
		if (equalSDCheck != null && equalSDCheck.getState()) {
			CatVariable xVar = (CatVariable)data.getVariable("x");
			int n[] = xVar.getCounts();
			double pooledS = Math.sqrt((s0 * s0 * (n[0] - 1) + s1 * s1 * (n[1] - 1))
																																		/ (n[0] + n[1] - 2));
			yDistn.setSD(pooledS, 0);
			yDistn.setSD(pooledS, 1);
			sdSlider[0].setParameter(pooledS);
		}
		else {
			sdSlider[0].setParameter(s0);
			sdSlider[1].setParameter(s1);
		}
	}

	
	private boolean localAction(Object target) {
		if (target == bestButton) {
			setBestParams();
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