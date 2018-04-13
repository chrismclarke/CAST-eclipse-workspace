package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;


public class NegBinomDistnApplet extends CoreDiscreteDistnApplet {
	static final private String P_LIMITS_PARAM = "pLimits";
	static final private String K_LIMITS_PARAM = "kLimits";
	
	private String pSuccessString;
	
	private ParameterSlider kSlider, pSlider;
	
	protected DiscreteDistnVariable getDistn() {
		pSuccessString = "P(" + translate("success") + "), " + MText.expandText("#pi#");
		NegBinDistnVariable negBinVar = new NegBinDistnVariable("x");
		return negBinVar;
	}
	
	protected void initialiseParams() {
		NegBinDistnVariable negBinVar = (NegBinDistnVariable)data.getVariable("distn");
		NumValue pSuccess = pSlider.getParameter();
		negBinVar.setPSuccess(pSuccess);
		NumValue k = kSlider.getParameter();
		negBinVar.setK((int)Math.round(k.toDouble()));
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new ProportionLayout(0.5, 5));
		
		StringTokenizer st = new StringTokenizer(getParameter(K_LIMITS_PARAM));
		int maxK = Integer.parseInt(st.nextToken());
		int startK = Integer.parseInt(st.nextToken());
		String kLabel = st.nextToken().replace("_", " ");
		kSlider = new ParameterSlider(new NumValue(1, 0), new NumValue(maxK, 0),
																				new NumValue(startK, 0), kLabel + ", k", this);
		thePanel.add("Left", kSlider);
		
		st = new StringTokenizer(getParameter(P_LIMITS_PARAM));
		NumValue sliderMin_negBin = new NumValue(st.nextToken());
		NumValue sliderMax_negBin = new NumValue(st.nextToken());
		NumValue sliderStart_negBin = new NumValue(st.nextToken());
		
		pSlider = new ParameterSlider(sliderMin_negBin, sliderMax_negBin, sliderStart_negBin, pSuccessString, this);
		pSlider.setForeground(Color.blue);
		
		thePanel.add("Right", pSlider);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == pSlider) {
			NegBinDistnVariable negBinVar = (NegBinDistnVariable)data.getVariable("distn");
			NumValue newP = pSlider.getParameter();
			negBinVar.setPSuccess(newP);
			data.variableChanged("distn");
			return true;
		}
		else if (target == kSlider) {
			NegBinDistnVariable negBinVar = (NegBinDistnVariable)data.getVariable("distn");
			int newK = (int)Math.round(kSlider.getParameter().toDouble());
			negBinVar.setK(newK);
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