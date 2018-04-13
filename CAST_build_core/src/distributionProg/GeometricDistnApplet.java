package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;


public class GeometricDistnApplet extends CoreDiscreteDistnApplet {
	static final private String P_LIMITS_PARAM = "pLimits";
	
	private String pSuccessString;
	
	private ParameterSlider pSlider;
	
	protected DiscreteDistnVariable getDistn() {
		pSuccessString = "P(" + translate("success") + "), " + MText.expandText("#pi#");
		GeometricDistnVariable geomVar = new GeometricDistnVariable("x");
		return geomVar;
	}
	
	protected void initialiseParams() {
		GeometricDistnVariable geomVar = (GeometricDistnVariable)data.getVariable("distn");
		NumValue pSuccess_geom = pSlider.getParameter();
		geomVar.setPSuccess(pSuccess_geom);
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		StringTokenizer st_geom = new StringTokenizer(getParameter(P_LIMITS_PARAM));
		NumValue sliderMin = new NumValue(st_geom.nextToken());
		NumValue sliderMax = new NumValue(st_geom.nextToken());
		NumValue sliderStart = new NumValue(st_geom.nextToken());
		
		pSlider = new ParameterSlider(sliderMin, sliderMax, sliderStart, pSuccessString, this);
		thePanel.add("Center", pSlider);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == pSlider) {
			GeometricDistnVariable geomVar = (GeometricDistnVariable)data.getVariable("distn");
			NumValue pSuccess = pSlider.getParameter();
			geomVar.setPSuccess(pSuccess);
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