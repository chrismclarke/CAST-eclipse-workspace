package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;


public class PoissonDistnApplet extends CoreDiscreteDistnApplet {
	static final private String LAMBDA_LIMITS_PARAM = "lambdaLimits";
	
	private String lambdaString;
	
	private ParameterSlider lSlider;
	
	protected DiscreteDistnVariable getDistn() {
		lambdaString = translate("Rate of events") + ", " + MText.expandText("#lambda#");
		PoissonDistnVariable poissonVar = new PoissonDistnVariable("x");
		return poissonVar;
	}
	
	protected void initialiseParams() {
		PoissonDistnVariable poissonVar = (PoissonDistnVariable)data.getVariable("distn");
		NumValue lambda_poisson = lSlider.getParameter();
		poissonVar.setLambda(lambda_poisson);
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		StringTokenizer st_poisson = new StringTokenizer(getParameter(LAMBDA_LIMITS_PARAM));
		NumValue sliderMin_poisson = new NumValue(st_poisson.nextToken());
		NumValue sliderMax_poisson = new NumValue(st_poisson.nextToken());
		NumValue sliderStart_poisson = new NumValue(st_poisson.nextToken());
		
		lSlider = new ParameterSlider(sliderMin_poisson, sliderMax_poisson,
																														sliderStart_poisson, lambdaString, this);
		thePanel.add("Center", lSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == lSlider) {
			PoissonDistnVariable poissonVar = (PoissonDistnVariable)data.getVariable("distn");
			NumValue lambda = lSlider.getParameter();
			poissonVar.setLambda(lambda);
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