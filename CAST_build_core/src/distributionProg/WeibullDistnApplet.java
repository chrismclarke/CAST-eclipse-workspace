package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;
import valueList.*;

import distribution.*;


public class WeibullDistnApplet extends CoreContinDistnApplet {
	static final private String MEAN_LIMITS_PARAM = "meanLimits";
	static final private String ALPHA_LIMITS_PARAM = "alphaLimits";
	static final private String LAMBDA_DECIMALS_PARAM = "lambdaDecimals";
	static final private String MAX_LAMBDA_PARAM = "maxLambda";
	
	private ParameterSlider meanSlider, aSlider;
	private FixedValueView lambdaValue;
	
	protected ContinDistnVariable getDistn() {
		int lambdaDecimals = Integer.parseInt(getParameter(LAMBDA_DECIMALS_PARAM));
		WeibullDistnVariable theDistn = new WeibullDistnVariable("x");
		theDistn.setLambda(new NumValue(1.0, lambdaDecimals));
		return theDistn;
	}
	
	protected void setParamsFromSliders() {
		WeibullDistnVariable weibullVar = (WeibullDistnVariable)data.getVariable("distn");
		NumValue alpha = aSlider.getParameter();
		weibullVar.setAlpha(alpha);
		
		double mean = meanSlider.getParameter().toDouble();
		double lambda = Math.exp(GammaDistnVariable.aLoGam(1 + 1 / alpha.toDouble())) / mean;
		weibullVar.setLambda(lambda);
		
		double alphaValue = alpha.toDouble();
		if (alphaValue < 1.0)
			pdfView.setPseudoMaxDensity(1 / mean * alphaValue);
											//	to expand height a bit (ad hoc) when max density is really infinite
		lambdaValue.setValue(lambda);
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
		
			XPanel lambdaPanel = new XPanel();
			lambdaPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				String lambdaString = MText.expandText("#lambda#") + " =";
				NumValue maxLambda = new NumValue(getParameter(MAX_LAMBDA_PARAM));
				lambdaValue = new FixedValueView(lambdaString, maxLambda, 1.0, this);
			lambdaPanel.add(lambdaValue);
		
		thePanel.add("East", lambdaPanel);
		
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