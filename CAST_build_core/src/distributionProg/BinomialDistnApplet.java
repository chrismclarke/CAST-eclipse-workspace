package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;


public class BinomialDistnApplet extends CoreDiscreteDistnApplet {
//	static final private String P_LIMITS_PARAM = "pLimits";
	static final private String N_LIMITS_PARAM = "nLimits";
	
	private String pSuccessString;
	private ParameterSlider nSlider, pSlider;
	
	protected DiscreteDistnVariable getDistn() {
		pSuccessString = "P(" + translate("success") + "), " + MText.expandText("#pi#");
		BinomialDistnVariable binomVar = new BinomialDistnVariable("x");
		return binomVar;
	}
	
	protected void initialiseParams() {
		BinomialDistnVariable binomVar = (BinomialDistnVariable)data.getVariable("distn");
		NumValue pSuccess_binom = pSlider.getParameter();
		binomVar.setProb(pSuccess_binom.toDouble());
		NumValue n_binom = nSlider.getParameter();
		binomVar.setCount((int)Math.round(n_binom.toDouble()));
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new ProportionLayout(0.5, 5));
		
		StringTokenizer st_binom = new StringTokenizer(getParameter(N_LIMITS_PARAM));
		int maxN = Integer.parseInt(st_binom.nextToken());
		int startN = Integer.parseInt(st_binom.nextToken());
		String nLabel = st_binom.nextToken().replace("_", " ");
		nSlider = new ParameterSlider(new NumValue(1, 0), new NumValue(maxN, 0),
																	new NumValue(startN, 0), nLabel + ", n", this);
		thePanel.add("Left", nSlider);
		
		pSlider = new ParameterSlider(new NumValue(0, 2), new NumValue(1, 2),
																	new NumValue(0.5, 2), pSuccessString, this);
		pSlider.setForeground(Color.blue);
		
		thePanel.add("Right", pSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == pSlider) {
			BinomialDistnVariable binomVar = (BinomialDistnVariable)data.getVariable("distn");
			double newP = pSlider.getParameter().toDouble();
			binomVar.setProb(newP);
			data.variableChanged("distn");
			return true;
		}
		else if (target == nSlider) {
			BinomialDistnVariable binomVar = (BinomialDistnVariable)data.getVariable("distn");
			int newN = (int)Math.round(nSlider.getParameter().toDouble());
			binomVar.setCount(newN);
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