package experProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import exper.*;


public class RotateThreeFactorLSApplet extends RotateThreeFactorApplet {
	protected XCheckbox factorCheck[] = new XCheckbox[3];
	private XCheckbox residualCheck;
	
	protected boolean fitFactor[] = new boolean[3];
	protected boolean fitInteraction = false;
	
	protected RotateTwoFactorView create3DView(DataSet data, D3Axis xAxis, D3Axis yAxis,
																													D3Axis zAxis, String yVarKey) {
		RotateThreeFactorView theView = new RotateThreeFactorView(data, this, xAxis, yAxis, zAxis, "treat1",
											yVarKey, "treat2", "treat3", "model");
		theView.setDrawData(true);
		theView.setResidualDisplay(RotateTwoFactorView.LINE_RESIDUALS);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		
		thePanel.add(ProportionLayout.TOP, effectPanel(data));
		
		thePanel.add(ProportionLayout.BOTTOM, lowerControlPanel(data));
		
		return thePanel;
	}
	
	protected XPanel effectPanel(DataSet data) {
		XPanel effectPanel = new XPanel();
		effectPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 6));
		
		for (int i=0 ; i<3 ; i++) {
			String factorKey = "treat" + (i+1);
			factorCheck[i] = new XCheckbox(translate("Main effect for") + " "
																			+ data.getVariable(factorKey).name, this);
			factorCheck[i].setState(false);
			fitFactor[i] = false;
			effectPanel.add(factorCheck[i]);
			effectPanel.add(new TreatmentLabelsView(data, this, factorKey));
		}
		return effectPanel;
	}
	
	private XPanel lowerControlPanel(DataSet data) {
		XPanel residPanel = new XPanel();
		residPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 6));
		
			residualCheck = new XCheckbox(translate("Show residuals"), this);
			residualCheck.setState(true);
			theView.setResidualDisplay(RotateTwoFactorView.LINE_RESIDUALS);
		residPanel.add(residualCheck);
		return residPanel;
	}
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<3 ; i++)
			if (target == factorCheck[i]) {
				fitFactor[i] = factorCheck[i].getState();
				FactorsModel model = (FactorsModel)data.getVariable("model");
				model.setLSParams("response", fitFactor, fitInteraction);
				data.variableChanged("model");
				return true;
			}
			
		if (target == residualCheck) {
			theView.setResidualDisplay(residualCheck.getState() ? RotateTwoFactorView.LINE_RESIDUALS
																														: RotateTwoFactorView.NO_RESIDUALS);
			theView.repaint();
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