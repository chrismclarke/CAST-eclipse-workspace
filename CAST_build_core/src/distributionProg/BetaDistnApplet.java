package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;
import valueList.*;

import distribution.*;


public class BetaDistnApplet extends CoreContinDistnApplet {
	static final private String MEAN_LIMITS_PARAM = "meanLimits";
	static final private String SD_LIMITS_PARAM = "sdPropnLimits";
	static final private String MAX_ALPHA_PARAM = "maxAlpha";
	static final private String MAX_BETA_PARAM = "maxBeta";
	
	static final private Color kMeanSliderColor = new Color(0x000099);
	static final private Color kSdSliderColor = new Color(0x990000);
	
	private ParameterSlider meanSlider, sdSlider;
	private FixedValueView alphaValue, betaValue;
	
	protected ContinDistnVariable getDistn() {
		BetaDistnVariable theDistn = new BetaDistnVariable("x");
		theDistn.setAlpha(1);
		theDistn.setBeta(1);
		return theDistn;
	}
	
	protected void setParamsFromSliders() {
		BetaDistnVariable betaVar = (BetaDistnVariable)data.getVariable("distn");
		
		double mean = meanSlider.getParameter().toDouble();
		double maxVar = mean * (1 - mean);
		double sdPropn = sdSlider.getParameter().toDouble();
		double var = maxVar * sdPropn * sdPropn;
		
		double a = mean * mean * (1 - mean) / var - mean;
		double b = mean * (1 - mean) * (1 - mean) / var - (1 - mean);
		
		betaVar.setAlpha(a);
		betaVar.setBeta(b);
		
		if (a < 1 || b < 1) {
			double lowMax = betaVar.getScaledDensity(0.99);
			double highMax = betaVar.getScaledDensity(0.01);
			pdfView.setPseudoMaxDensity(Math.max(lowMax, highMax));
		}
		pdfView.setTrimCurve(false);
		
		alphaValue.setValue(a);
		betaValue.setValue(b);
	}
	
	protected void setDistnSupport(ContinuousProbView pdfView) {
		pdfView.setSupport(0.0, 1.0);
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel sliderPanel = new InsetPanel(30, 0);
			sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 3));
				
				String meanString = translate("Mean");
				
				StringTokenizer st = new StringTokenizer(getParameter(MEAN_LIMITS_PARAM));
				NumValue meanMin = new NumValue(st.nextToken());
				NumValue meanMax = new NumValue(st.nextToken());
				NumValue meanStart = new NumValue(st.nextToken());
				
				meanSlider = new ParameterSlider(meanMin, meanMax, meanStart, meanString, this);
				meanSlider.setForeground(kMeanSliderColor);
		sliderPanel.add(meanSlider);
		
				String sdString = translate("Standard deviation");
				
				st = new StringTokenizer(getParameter(SD_LIMITS_PARAM));
				NumValue sdMin = new NumValue(st.nextToken());
				NumValue sdMax = new NumValue(st.nextToken());
				NumValue sdStart = new NumValue(st.nextToken());
				
				sdSlider = new ParameterSlider(sdMin, sdMax, sdStart, "",
																					ParameterSlider.SHOW_MIN_MAX, this);
				sdSlider.setShowValue(false);
				sdSlider.setAddEquals(false);
				sdSlider.setTitle(sdString, this);
				sdSlider.fixMinMaxText("Low", "High");
				sdSlider.setForeground(kSdSliderColor);
			sliderPanel.add(sdSlider);
		
		thePanel.add("Center", sliderPanel);
		
			XPanel paramPanel = new XPanel();
			paramPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 12));
			
				String alphaString = MText.expandText("#alpha#") + " =";
				NumValue maxAlpha = new NumValue(getParameter(MAX_ALPHA_PARAM));
				alphaValue = new FixedValueView(alphaString, maxAlpha, 1.0, this);
			paramPanel.add(alphaValue);
			
				String betaString = MText.expandText("#beta#") + " =";
				NumValue maxBeta = new NumValue(getParameter(MAX_BETA_PARAM));
				betaValue = new FixedValueView(betaString, maxBeta, 1.0, this);
			paramPanel.add(betaValue);
		
		thePanel.add("East", paramPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == meanSlider || target == sdSlider) {
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