package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

//import scatterProg.*;
import regn.*;
import regnView.*;


public class DragLine2Applet extends DragLineApplet {
	private XCheckbox residCheck;
	private XButton lsButton;
	private DragLineView theView;
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = (DragLineView)super.createDataView(data, theHorizAxis, theVertAxis);
		theView.setDrawResiduals(false);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER));
		
		XPanel equationPanel = new XPanel();
		equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		equationPanel.add(new LinearEquationView(data, this, "model", getParameter(Y_VAR_NAME_PARAM),
													getParameter(X_VAR_NAME_PARAM), intMin, intMax, slopeMin, slopeMax));
		thePanel.add(equationPanel);
		
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		residCheck = new XCheckbox(translate("Show residuals"), this);
		residCheck.setState(false);
		bottomPanel.add(residCheck);
		
		lsButton = new XButton(translate("Least squares"), this);
		bottomPanel.add(lsButton);
		
		thePanel.add(bottomPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == residCheck) {
			theView.setDrawResiduals(residCheck.getState());
			return true;
		}
		else if (target == lsButton) {
			LinearModel model = (LinearModel)data.getVariable("model");
			model.setLSParams("y", intStart.decimals, slopeStart.decimals, 0);
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}