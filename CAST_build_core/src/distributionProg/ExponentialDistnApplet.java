package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;

import distribution.*;


public class ExponentialDistnApplet extends CoreContinDistnApplet {
	static final private String LAMBDA_LIMITS_PARAM = "lambdaLimits";
	
	private ParameterSlider lSlider;
	
	protected ContinDistnVariable getDistn() {
		return new ExponDistnVariable("x");
	}
	
	protected void setParamsFromSliders() {
		ExponDistnVariable exponVar = (ExponDistnVariable)data.getVariable("distn");
		NumValue lambda = lSlider.getParameter();
		exponVar.setLambda(lambda);
	}
	
	protected void setDistnSupport(ContinuousProbView pdfView) {
		pdfView.setSupport(0.0, Double.POSITIVE_INFINITY);
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		String lambdaString = translate("Rate of events") + ", " + MText.expandText("#lambda#");
		
		StringTokenizer st = new StringTokenizer(getParameter(LAMBDA_LIMITS_PARAM));
				NumValue sliderMin = new NumValue(st.nextToken());
				NumValue sliderMax = new NumValue(st.nextToken());
				NumValue sliderStart = new NumValue(st.nextToken());
				
				lSlider = new ParameterSlider(sliderMin, sliderMax, sliderStart,
																												lambdaString, this);
				thePanel.add("Center", lSlider);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == lSlider) {
			setParamsFromSliders();
			data.variableChanged("distn");
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