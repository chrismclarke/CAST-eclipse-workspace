package distributionProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import distn.*;
import utils.*;
import formula.*;

import distribution.*;


public class NormalDistnApplet extends CoreContinDistnApplet {
	static final private String MEAN_LIMITS_PARAM = "meanLimits";
	static final private String SD_LIMITS_PARAM = "sdLimits";
	
	private ParameterSlider meanSlider, sdSlider;
	
	protected ContinDistnVariable getDistn() {
		NormalDistnVariable theDistn = new NormalDistnVariable("x");
		return theDistn;
	}
	
	protected void setParamsFromSliders() {
		NormalDistnVariable normalVar = (NormalDistnVariable)data.getVariable("distn");
		NumValue mean = meanSlider.getParameter();
		normalVar.setMean(mean);
		
		NumValue sd = sdSlider.getParameter();
		normalVar.setSD(sd);
		
		pdfView.setSupport(mean.toDouble() - 4 * sd.toDouble(), mean.toDouble() + 4 * sd.toDouble());		//	to make it easier to draw
	}
	
	protected void setDistnSupport(ContinuousProbView pdfView) {	//	leave at +/- infinity
	}
	
	protected XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(40, 0);
		thePanel.setLayout(new ProportionLayout(0.5, 5));
			
				String meanString = translate("Mean") + ", " + MText.expandText("#mu#");
				
				StringTokenizer st = new StringTokenizer(getParameter(MEAN_LIMITS_PARAM));
				NumValue meanMin = new NumValue(st.nextToken());
				NumValue meanMax = new NumValue(st.nextToken());
				NumValue meanStart = new NumValue(st.nextToken());
				
				meanSlider = new ParameterSlider(meanMin, meanMax, meanStart, meanString, this);
			thePanel.add(ProportionLayout.LEFT, meanSlider);
			
				String sdString = translate("Standard deviation") + ", " + MText.expandText("#sigma#");
				
				st = new StringTokenizer(getParameter(SD_LIMITS_PARAM));
				NumValue sdMin = new NumValue(st.nextToken());
				NumValue sdMax = new NumValue(st.nextToken());
				NumValue sdStart = new NumValue(st.nextToken());
				
				sdSlider = new ParameterSlider(sdMin, sdMax, sdStart, sdString, this);
			thePanel.add(ProportionLayout.RIGHT, sdSlider);
		
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