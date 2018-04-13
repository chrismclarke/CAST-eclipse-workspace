package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;
import valueList.*;


public class NegBinom2DistnApplet extends CoreDiscreteDistnApplet {
	static final private String MEAN_LIMITS_PARAM = "meanLimits";
	static final private String VARIANCE_LIMITS_PARAM = "varianceLimits";
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String MAX_K_PARAM = "maxK";
	static final private String MAX_PSUCCESS_PARAM = "maxPSuccess";
	
	static final private Color kDarkRed = new Color(0x990000);
	
	private int pDecimals, kDecimals;
	
	private ParameterSlider meanSlider, varianceSlider;
	private FixedValueView kView, pSuccessView;
	
	protected DiscreteDistnVariable getDistn() {
		StringTokenizer	st = new StringTokenizer(getParameter(PARAM_DECIMALS_PARAM));
		pDecimals = Integer.parseInt(st.nextToken());
		kDecimals = Integer.parseInt(st.nextToken());
		
		NegBinDistnVariable negBinVar = new NegBinDistnVariable("x", false);		// version that starts at zero
		return negBinVar;
	}
	
	protected void initialiseParams() {
		NegBinDistnVariable negBinVar = (NegBinDistnVariable)data.getVariable("distn");
		
		NumValue meanVal = meanSlider.getParameter();
		double variancePropn = varianceSlider.getParameter().toDouble();
		if (variancePropn == 1.0) {
			negBinVar.setPoisson(meanVal);
			kView.setValue(new NumValue(Double.POSITIVE_INFINITY));
			pSuccessView.setValue(new NumValue(1, 0));
		}
		else {
			double mean = meanVal.toDouble();
			double variance = variancePropn * mean;
			double pSuccess = mean / variance;
			double k = mean * pSuccess / (1 - pSuccess);
			
			NumValue pSuccessValue = new NumValue(pSuccess, pDecimals);
			negBinVar.setPSuccess(pSuccessValue);
			NumValue kValue = new NumValue(k, kDecimals);
			negBinVar.setK(kValue);
			
			pSuccessView.setValue(pSuccessValue);
			kView.setValue(kValue);
		}
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
		
		NumValue maxK = new NumValue(getParameter(MAX_K_PARAM));
		kView = new FixedValueView(MText.expandText("k ="), maxK, null, this);
		topPanel.add(kView);
		
		NumValue maxPSuccess = new NumValue(getParameter(MAX_PSUCCESS_PARAM));
		pSuccessView = new FixedValueView(MText.expandText("#pi# ="), maxPSuccess, null, this);
		topPanel.add(pSuccessView);
		
		thePanel.add("East", topPanel);
		thePanel.add("Center", meanVarPanel());
		
		return thePanel;
	}
	
	private XPanel meanVarPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL));
		
		StringTokenizer st = new StringTokenizer(getParameter(MEAN_LIMITS_PARAM));
		NumValue minMean = new NumValue(st.nextToken());
		NumValue maxMean = new NumValue(st.nextToken());
		NumValue startMean = new NumValue(st.nextToken());
		meanSlider = new ParameterSlider(minMean, maxMean, startMean, translate("Mean") + ", E[X]", this);
		meanSlider.setForeground(Color.blue);
		
		thePanel.add(meanSlider);
		
		st = new StringTokenizer(getParameter(VARIANCE_LIMITS_PARAM));
		NumValue maxVar = new NumValue(st.nextToken());
		NumValue startVar = new NumValue(st.nextToken());
		varianceSlider = new ParameterSlider(new NumValue(1, maxVar.decimals), maxVar, startVar, null, this);
		varianceSlider.setShowValue(false);
		varianceSlider.setAddEquals(false);
		varianceSlider.setTitle("Variance", this);
		String minVarLabel = "E[X]";
		String maxVarLabel = maxVar.toString() + MText.expandText("#times#E[X]");
		varianceSlider.fixMinMaxText(minVarLabel, maxVarLabel);
		varianceSlider.setForeground(kDarkRed);
		
		thePanel.add(varianceSlider);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == varianceSlider || target == meanSlider) {
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