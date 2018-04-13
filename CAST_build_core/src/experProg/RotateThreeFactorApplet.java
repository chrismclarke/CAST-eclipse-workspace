package experProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;
import exper.*;
import corr.*;


public class RotateThreeFactorApplet extends RotateTwoFactorApplet {
//	static final private String TREAT3_EFFECT_AXIS_PARAM = "treat3EffectAxis";
	
	static final protected String[] kThreeTreatKey = {"treat1", "treat2", "treat3"};
	static final private Color kWColor = Color.black;
	
	private XButton lsButton;
	
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		FactorsModel model = (FactorsModel)data.getVariable("model");
		model.setIdentConstraints(FactorsModel.ZERO_MEAN_EFFECTS);
																						//	only has effect when finding LS estimates
		model.setZeroEffectMeans();		//	only effects initial parameter values
		
		return data;
	}
	
	protected String[] getTreatKeys() {
		return kThreeTreatKey;
	}
	
	protected RotateTwoFactorView create3DView(DataSet data, D3Axis xAxis, D3Axis yAxis,
																													D3Axis zAxis, String yVarKey) {
		RotateThreeFactorView theView = new RotateThreeFactorView(data, this, xAxis, yAxis, zAxis, "treat1",
																												yVarKey, "treat2", "treat3", "model");
		theView.setDrawData(yVarKey != null);
		if (yVarKey != null)
			theView.setResidualDisplay(RotateTwoFactorView.SQR_RESIDUALS);
		return theView;
	}
	
	public Color getFactorColor(int factorIndex) {
		if (factorIndex == 2)
			return kWColor;
		else
			return super.getFactorColor(factorIndex);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new ProportionLayout(0.667, 10, ProportionLayout.VERTICAL,
																																ProportionLayout.TOTAL));
				XPanel xzPanel = new XPanel();
				xzPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																																	ProportionLayout.TOTAL));
				xzPanel.add(ProportionLayout.TOP, createEffectPanel(data, 0, true));
				xzPanel.add(ProportionLayout.BOTTOM, createEffectPanel(data, 1, true));
				
			sliderPanel.add(ProportionLayout.TOP, xzPanel);
			sliderPanel.add(ProportionLayout.BOTTOM, createEffectPanel(data, 2, false));
		
		thePanel.add("Center", sliderPanel);
		
			NumVariable response = (NumVariable)data.getVariable("response");
			if (response == null) {
				ConstantSliderPanel constSlider = new ConstantSliderPanel(data, "model", this);
				FactorsModel model = (FactorsModel)data.getVariable("model");
				model.setConstant(constSlider.getConstant());
				thePanel.add("North", constSlider);
			}
			else {
					XPanel buttonPanel = new XPanel();
					buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
						lsButton = new XButton(translate("Least squares"), this);
					buttonPanel.add(lsButton);
			
				thePanel.add("South", buttonPanel);
				thePanel.add("North", new MeanView(data, "response", MeanView.GENERIC_TEXT_FORMULA, 1, this));
			}
		
		return thePanel;
	}
	
	private EffectSlidersPanel createEffectPanel(DataSet data, int factorIndex,
																																boolean useGrayColors) {
		NumVariable response = (NumVariable)data.getVariable("response");
		boolean keepZeroMeanEffect = response != null;
		String factorKey = "treat" + (factorIndex + 1);
		return new EffectSlidersPanel(data, factorKey, "model", factorIndex,
							getParameter(TREAT1_EFFECT_AXIS_PARAM), useGrayColors, keepZeroMeanEffect, this);
	}
	
	protected XPanel namePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		XLabel yLabel = new XLabel("y:" + getVarName("model"), XLabel.LEFT, this);
		yLabel.setForeground(D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(yLabel);
		
		XLabel xLabel = new XLabel("x:" + getVarName(kThreeTreatKey[0]), XLabel.LEFT, this);
		xLabel.setForeground(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(xLabel);
		
		XLabel zLabel = new XLabel("z:" + getVarName(kThreeTreatKey[1]), XLabel.LEFT, this);
		zLabel.setForeground(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(zLabel);
		
		XLabel wLabel = new XLabel("z:" + getVarName(kThreeTreatKey[2]), XLabel.LEFT, this);
		wLabel.setForeground(kWColor);
		thePanel.add(wLabel);
		return thePanel;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		
		if (evt.target == lsButton) {
			FactorsModel model = (FactorsModel)data.getVariable("model");
			model.setLSParams("response", null, false);
			data.variableChanged("model");
			return true;
		}
		return false;
	}
}