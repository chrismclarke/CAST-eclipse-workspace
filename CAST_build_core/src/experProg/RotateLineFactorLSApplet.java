package experProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;
import corr.*;
import exper.*;


public class RotateLineFactorLSApplet extends RotateLineFactorApplet {
//	static final private String[] xKey = {"treat1", "treat2"};
	
	private XButton lsButton;
	
	protected DataSet readData() {
		DataSet data = super.readData();
			
		FactorsModel model = (FactorsModel)data.getVariable("model");
		model.setCatToNum(1, 2, zCatToNum, meanZ);
		model.setIdentConstraints(FactorsModel.ZERO_MEAN_EFFECTS);
		
		return data;
	}
	
	protected RotateTwoFactorView create3DView(DataSet data, D3Axis xAxis, D3Axis yAxis,
																													D3Axis zAxis, String yVarKey) {
		RotateTwoFactorView theView = super.create3DView(data, xAxis, yAxis, zAxis, yVarKey);
		theView.setDrawData(true);
		theView.setResidualDisplay(RotateTwoFactorView.SQR_RESIDUALS);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																																ProportionLayout.TOTAL));
			mainPanel.add(ProportionLayout.TOP, new EffectSlidersPanel(data, "treat1", "model", 0,
															getParameter(TREAT1_EFFECT_AXIS_PARAM), false, true, this));
			mainPanel.add(ProportionLayout.BOTTOM, new EffectSlidersPanel(data, "treat2", "model", 1,
																	getParameter(TREAT2_EFFECT_AXIS_PARAM), true, true,
																	getParameter(TREAT2_AXIS_PARAM), zCatToNum, meanZ, this));
		
		thePanel.add("Center", mainPanel);
		
			XPanel lsPanel = new XPanel();
			lsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				lsButton = new XButton(translate("Least squares"), this);
			lsPanel.add(lsButton);
			
		thePanel.add("South", lsPanel);
		
		thePanel.add("North", new MeanView(data, "response", MeanView.GENERIC_TEXT_FORMULA, 2, this));
			
		return thePanel;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		
		if (evt.target == lsButton) {
			FactorsModel model = (FactorsModel)data.getVariable("model");
			model.setLSParams("response", null, false);
//			constantSlider.setParameter(model.getConstant());
			data.variableChanged("model");
			return true;
		}
		return false;
	}
}