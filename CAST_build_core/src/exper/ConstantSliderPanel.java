package exper;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import experProg.*;


public class ConstantSliderPanel extends XPanel {
	private DataSet data;
	private String modelKey;
	
	private ParameterSlider constantSlider;
	
	public ConstantSliderPanel(DataSet data, String modelKey, XApplet applet) {
		setLayout(new BorderLayout());
		
			StringTokenizer st = new StringTokenizer(applet.getParameter(CoreMultiFactorApplet.CONSTANT_PARAM));
			NumValue startConst = new NumValue(st.nextToken());
			NumValue minConst = new NumValue(st.nextToken());
			NumValue maxConst = new NumValue(st.nextToken());
			constantSlider = new ParameterSlider(minConst, maxConst, startConst,
																																	"Base value", applet);
			constantSlider.setFont(applet.getStandardBoldFont());
		add("Center", constantSlider);
		
		this.data = data;
		this.modelKey = modelKey;
	}
	
	public void setConstant(double newConstant) {
		constantSlider.setParameter(newConstant);
	}
	
	public double getConstant() {
		return constantSlider.getParameter().toDouble();
	}
	
	private boolean localAction(Object target) {
		if (target == constantSlider) {
			double newConst = constantSlider.getParameter().toDouble();
			
			FactorsModel model = (FactorsModel)data.getVariable(modelKey);
			model.setConstant(newConst);
			data.variableChanged(modelKey);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}