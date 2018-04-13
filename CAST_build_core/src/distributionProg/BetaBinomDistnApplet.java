package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;
import valueList.*;


public class BetaBinomDistnApplet extends CoreDiscreteDistnApplet {
	static final private String N_PARAM = "n";
	static final private String P_LIMITS_PARAM = "pLimits";
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String MAX_ALPHA_PARAM = "maxAlpha";
	static final private String MAX_BETA_PARAM = "maxBeta";
	
	static final private Color kDarkRed = new Color(0x990000);
	
	private int aDecimals, bDecimals;
	private int n;
	
	private ParameterSlider piSlider, varianceSlider;
	private FixedValueView alphaView, betaView;
	
	protected DiscreteDistnVariable getDistn() {
		StringTokenizer	st = new StringTokenizer(getParameter(PARAM_DECIMALS_PARAM));
		aDecimals = Integer.parseInt(st.nextToken());
		bDecimals = Integer.parseInt(st.nextToken());
		
		BetaBinomDistnVariable betaBinomVar = new BetaBinomDistnVariable("x");
		n = Integer.parseInt(getParameter(N_PARAM));
		betaBinomVar.setN(n);
		return betaBinomVar;
	}
	
	protected void initialiseParams() {
		BetaBinomDistnVariable betaBinomVar = (BetaBinomDistnVariable)data.getVariable("distn");
		
		NumValue piVal = piSlider.getParameter();
		double variancePropn = varianceSlider.getParameter().toDouble();
		if (variancePropn < 1.00001) {
			betaBinomVar.setBinomial(piVal);
			alphaView.setValue(new NumValue(Double.POSITIVE_INFINITY, 0));
			betaView.setValue(new NumValue(Double.POSITIVE_INFINITY, 0));
		}
		else if (variancePropn > n - 0.00001) {
			betaBinomVar.setZeroN(piVal);
			alphaView.setValue(new NumValue(0, 0));
			betaView.setValue(new NumValue(0, 0));
		}
		else {
			double pi = piVal.toDouble();
			
			double aPlusB = (n - variancePropn) / (variancePropn - 1);
			double a = pi * aPlusB;
			double b = aPlusB - a;
			
			NumValue alphaValue = new NumValue(a, aDecimals);
			betaBinomVar.setAlpha(alphaValue);
			NumValue betaValue = new NumValue(b, bDecimals);
			betaBinomVar.setBeta(betaValue);
			
			alphaView.setValue(alphaValue);
			betaView.setValue(betaValue);
		}
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("East", parameterPanel());
		thePanel.add("Center", meanVarPanel());
		
		return thePanel;
	}
	
	private XPanel parameterPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
		
		NumValue maxAlpha = new NumValue(getParameter(MAX_ALPHA_PARAM));
		alphaView = new FixedValueView(MText.expandText("#alpha# ="), maxAlpha, null, this);
		thePanel.add(alphaView);
		
		NumValue maxBeta = new NumValue(getParameter(MAX_BETA_PARAM));
		betaView = new FixedValueView(MText.expandText("#beta# ="), maxBeta, null, this);
		thePanel.add(betaView);
		
		return thePanel;
	}
	
	private XPanel meanVarPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL));
		
		StringTokenizer st = new StringTokenizer(getParameter(P_LIMITS_PARAM));
		NumValue minP = new NumValue(st.nextToken());
		NumValue maxP = new NumValue(st.nextToken());
		NumValue startP = new NumValue(st.nextToken());
		piSlider = new ParameterSlider(minP, maxP, startP, "P(" + translate("success") + ")", this);
		piSlider.setForeground(Color.blue);
		
		thePanel.add(piSlider);
		
		NumValue minVar = new NumValue(1, 2);
		NumValue maxVar = new NumValue(n, 2);
		NumValue startVar = new NumValue(1, 2);
		varianceSlider = new ParameterSlider(minVar, maxVar, startVar, null, this);
		varianceSlider.setShowValue(false);
		varianceSlider.setAddEquals(false);
		varianceSlider.setTitle(translate("Variance"), this);
		String minVarLabel = translate("Binomial");
		String maxVarLabel = translate("All 0 or n");
		varianceSlider.fixMinMaxText(minVarLabel, maxVarLabel);
		varianceSlider.setForeground(kDarkRed);
		
		thePanel.add(varianceSlider);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == varianceSlider || target == piSlider) {
			initialiseParams();
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