package responseSurfaceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;


public class QuadSurfaceTermsApplet extends QuadSurfaceApplet {
	XCheckbox x2Check, z2Check, xzCheck;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		ResponseSurfaceModel model = (ResponseSurfaceModel)data.getVariable("model");
		double constraints[] = {Double.NaN, Double.NaN, Double.NaN, 0.0, 0.0, 0.0};
		model.updateLSParams("y", constraints);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		
		boolean[] showParameter = {true, true, true, false, false, false};
		theEqn.setDrawParameters(showParameter);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
		thePanel.add(ProportionLayout.LEFT, super.controlPanel(data));
			
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				x2Check = new XCheckbox(translate("Quadratic term in") + " " + getParameter(X_VAR_NAME_PARAM), this);
			rightPanel.add(x2Check);
			
				z2Check = new XCheckbox(translate("Quadratic term in") + " " + getParameter(Z_VAR_NAME_PARAM), this);
			rightPanel.add(z2Check);
			
				xzCheck = new XCheckbox(translate("Interaction term"), this);
			rightPanel.add(xzCheck);
			
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == x2Check || target == z2Check || target == xzCheck) {
			double constraints[] = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
			if (!x2Check.getState())
				constraints[3] = 0.0;
			if (!z2Check.getState())
				constraints[4] = 0.0;
			if (!xzCheck.getState())
				constraints[5] = 0.0;
			
			ResponseSurfaceModel model = (ResponseSurfaceModel)data.getVariable("model");
			model.updateLSParams("y", constraints);
			
			boolean[] showParameter = {true, true, true, x2Check.getState(), z2Check.getState(), xzCheck.getState()};
			theEqn.setDrawParameters(showParameter);
			
			data.variableChanged("model");
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