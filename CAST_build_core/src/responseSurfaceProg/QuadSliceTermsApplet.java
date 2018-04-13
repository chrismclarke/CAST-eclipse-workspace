package responseSurfaceProg;

import java.awt.*;

import dataView.*;
import utils.*;

import responseSurface.*;


public class QuadSliceTermsApplet extends QuadSurfaceSliceApplet {
	
	XCheckbox x2Check, z2Check, w2Check, xzCheck, xwCheck, zwCheck;
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", super.controlPanel(data));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
			
				XPanel quadPanel = new XPanel();
				quadPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
					x2Check = new XCheckbox("Quadratic " + getParameter(X_VAR_NAME_PARAM), this);
					x2Check.setState(true);
				quadPanel.add(x2Check);
				
					z2Check = new XCheckbox("Quadratic " + getParameter(Z_VAR_NAME_PARAM), this);
					z2Check.setState(true);
				quadPanel.add(z2Check);
				
					w2Check = new XCheckbox("Quadratic " + getParameter(W_VAR_NAME_PARAM), this);
					w2Check.setState(true);
				quadPanel.add(w2Check);
			
			 bottomPanel.add(quadPanel);
			
				XPanel interactPanel = new XPanel();
				interactPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
					xzCheck = new XCheckbox(getParameter(X_VAR_NAME_PARAM) + " by " + getParameter(Z_VAR_NAME_PARAM), this);
					xzCheck.setState(true);
				interactPanel.add(xzCheck);
				
					xwCheck = new XCheckbox(getParameter(X_VAR_NAME_PARAM) + " by " + getParameter(W_VAR_NAME_PARAM), this);
					xwCheck.setState(true);
				interactPanel.add(xwCheck);
				
					zwCheck = new XCheckbox(getParameter(Z_VAR_NAME_PARAM) + " by " + getParameter(W_VAR_NAME_PARAM), this);
					zwCheck.setState(true);
				interactPanel.add(zwCheck);
			
			 bottomPanel.add(interactPanel);
			 
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == x2Check || target == z2Check  || target == w2Check
				|| target == xzCheck || target == xwCheck || target == zwCheck) {
			double constraints[] = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
					Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
			if (!x2Check.getState())
				constraints[4] = 0.0;
			if (!z2Check.getState())
				constraints[5] = 0.0;
			if (!w2Check.getState())
				constraints[6] = 0.0;
			if (!xzCheck.getState())
				constraints[7] = 0.0;
			if (!xwCheck.getState())
				constraints[8] = 0.0;
			if (!zwCheck.getState())
				constraints[9] = 0.0;

			ResponseSurface3Model fullModel = (ResponseSurface3Model)data.getVariable("fullModel");
			fullModel.updateLSParams("y", constraints);

			data.variableChanged("sliceModel");
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