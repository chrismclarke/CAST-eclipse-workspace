package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;
import valueList.*;

import distribution.*;


public class GammaDistnApplet extends CoreContinDistnApplet {
	static final private String MEAN_LIMITS_PARAM = "meanLimits";
	static final private String ALPHA_LIMITS_PARAM = "alphaLimits";
	static final private String MAX_BETA_PARAM = "maxBeta";
	
	private ParameterSlider meanSlider, aSlider;
	private FixedValueView betaValue;
	
	protected ContinDistnVariable getDistn() {
		GammaDistnVariable theDistn = new GammaDistnVariable("x");
		theDistn.setScale(1);
		return theDistn;
	}
	
	protected void setParamsFromSliders() {
		GammaDistnVariable gammaVar = (GammaDistnVariable)data.getVariable("distn");
		NumValue alpha = aSlider.getParameter();
		gammaVar.setShape(alpha.toDouble());
		
		double mean = meanSlider.getParameter().toDouble();
		double beta = alpha.toDouble() / mean;
		gammaVar.setScale(1 / beta);
		
		double alphaValue = alpha.toDouble();
		if (alphaValue < 1.0)
			pdfView.setPseudoMaxDensity(1 / mean * alphaValue);
		//	to expand height a bit (ad hoc) when max density is really infinite
		
		betaValue.setValue(beta);
	}
	
	protected void setDistnSupport(ContinuousProbView pdfView) {
		pdfView.setSupport(0.0, Double.POSITIVE_INFINITY);
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		XPanel sliderPanel = new XPanel();
		sliderPanel.setLayout(new ProportionLayout(0.5, 5));
		
		String meanString = translate("Mean");
		
		StringTokenizer st = new StringTokenizer(getParameter(MEAN_LIMITS_PARAM));
		NumValue meanMin = new NumValue(st.nextToken());
		NumValue meanMax = new NumValue(st.nextToken());
		NumValue meanStart = new NumValue(st.nextToken());
		
		meanSlider = new ParameterSlider(meanMin, meanMax, meanStart, meanString, this);
		sliderPanel.add(ProportionLayout.RIGHT, meanSlider);
		
		String alphaString = translate("Shape") + ", " + MText.expandText("#alpha#");
		
		st = new StringTokenizer(getParameter(ALPHA_LIMITS_PARAM));
		NumValue alphaMin = new NumValue(st.nextToken());
		NumValue alphaMax = new NumValue(st.nextToken());
		NumValue alphaStart = new NumValue(st.nextToken());
		
		aSlider = new ParameterSlider(alphaMin, alphaMax, alphaStart, alphaString, this);
		sliderPanel.add(ProportionLayout.LEFT, aSlider);
		
		thePanel.add("Center", sliderPanel);
		
		XPanel betaPanel = new XPanel();
		betaPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
		
		String betaString = MText.expandText("#beta#") + " =";
		NumValue maxBeta = new NumValue(getParameter(MAX_BETA_PARAM));
		betaValue = new FixedValueView(betaString, maxBeta, 1.0, this);
		betaPanel.add(betaValue);
		
		thePanel.add("East", betaPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == meanSlider || target == aSlider) {
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