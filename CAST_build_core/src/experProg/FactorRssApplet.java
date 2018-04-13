package experProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import exper.*;


public class FactorRssApplet extends CoreMultiFactorApplet {
	static final protected String RESPONSE_AXIS_INFO_PARAM = "responseAxis";
	static final protected String TREAT_EFFECT_AXIS_PARAM = "treatEffectAxis";
	
	static final private String effect1Keys[] = {"treat1"};
	
	private XButton lsButton;
	private XCheckbox residCheck;
	private ConstantSliderPanel constantSliderPanel;
	
	private TreatRssView yView;
	
	public void setupApplet() {
		readEffects();
		
		data = readData();
		
		setLayout(new ProportionLayout(0.7, 10, ProportionLayout.HORIZONTAL,
																																ProportionLayout.TOTAL));
		add(ProportionLayout.LEFT, displayPanel(data));
		add(ProportionLayout.RIGHT, parameterPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			FactorsModel model = new FactorsModel(getParameter(RESPONSE_NAME_PARAM),
																													data, effect1Keys, constant, effects, 0);
			model.setIdentConstraints(FactorsModel.ZERO_MEAN_EFFECTS);
		data.addVariable("model", model);
			
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(RESPONSE_AXIS_INFO_PARAM));
			plotPanel.add("Left", yAxis);
			
				HorizAxis treatAxis = new HorizAxis(this);
				CatVariable treat1Var = (CatVariable)data.getVariable("treat1");
				treatAxis.setCatLabels(treat1Var);
				treatAxis.setAxisName(treat1Var.name);
				
			plotPanel.add("Bottom", treatAxis);
			
				yView = new TreatRssView(data, this, yAxis, treatAxis, "response", "treat1", "model");
				yView.lockBackground(Color.white);
			plotPanel.add("Center", yView);
		
		thePanel.add("Center", plotPanel);
		
			NumVariable respVar = (NumVariable)data.getVariable("response");
			XLabel respLabel = new XLabel(respVar.name, XLabel.LEFT, this);
			respLabel.setFont(yAxis.getFont());
		thePanel.add("North", respLabel);
		
		return thePanel;
	}
	
	protected XPanel parameterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 15));
		
		thePanel.add("Center", new EffectSlidersPanel(data, "treat1", "model", 0,
													getParameter(TREAT_EFFECT_AXIS_PARAM), false, false, this));
			
			constantSliderPanel = new ConstantSliderPanel(data, "model", this);
		thePanel.add("North", constantSliderPanel);
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 10));
						
				lsButton = new XButton(translate("Least squares"), this);
			controlPanel.add(lsButton);
			
				residCheck = new XCheckbox(translate("Show residuals"), this);
			controlPanel.add(residCheck);
		
		thePanel.add("South", controlPanel);
		
		return thePanel;
	}
	
	public void finishAnimation() {
	}
	
	private boolean localAction(Object target) {
		if (target == residCheck) {
			yView.setShowResiduals(residCheck.getState());
			yView.repaint();
			return true;
		}
		else if (target == lsButton) {
			FactorsModel model = (FactorsModel)data.getVariable("model");
			model.setLSParams("response", null, false);
			constantSliderPanel.setConstant(model.getConstant());
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