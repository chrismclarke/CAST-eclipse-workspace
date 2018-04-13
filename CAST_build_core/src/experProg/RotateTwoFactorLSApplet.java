package experProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import exper.*;


public class RotateTwoFactorLSApplet extends RotateTwoFactorApplet {
	private XButton lsButton;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		FactorsModel model = (FactorsModel)data.getVariable("model");
		model.setIdentConstraints(FactorsModel.ZERO_MEAN_EFFECTS);
		model.setZeroEffectMeans();
		
		return data;
	}
	
	protected XPanel rotatePanel(DataSet data) {
		XPanel thePanel = super.rotatePanel(data);
		
		theView.setResidualDisplay(RotateTwoFactorView.SQR_RESIDUALS);
		theView.setDrawData(true);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		thePanel.add("Center", super.controlPanel(data));
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				lsButton = new XButton(translate("Least squares"), this);
			buttonPanel.add(lsButton);
			
		thePanel.add("South", buttonPanel);
		
//		thePanel.add("North", new MeanView(data, "response", MeanView.GENERIC_TEXT_FORMULA, 2, this));
		
		return thePanel;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		
		if (evt.target == lsButton) {
			FactorsModel model = (FactorsModel)data.getVariable("model");
			model.setLSParams("response", null, false);
			constantSliderPanel.setConstant(model.getConstant());
			data.variableChanged("model");
			return true;
		}
		return false;
	}
}