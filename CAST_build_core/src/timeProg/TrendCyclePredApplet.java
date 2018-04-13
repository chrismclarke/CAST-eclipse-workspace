package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import time.*;


public class TrendCyclePredApplet extends CoreActualResidApplet {
	
	private ExpSmoothSlider expSmoothSlider;
	
	public void setupApplet() {
		super.setupApplet();
		
		setLayout(new BorderLayout());
		
			XPanel seriesPanel = new XPanel();
			seriesPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																																		ProportionLayout.TOTAL));
			seriesPanel.add("Top", timeSeriesPanel(data, "y", "fit", Y_AXIS_INFO_PARAM));
				seriesView[0].addSmoothedVariable("predict");
			seriesPanel.add("Bottom", timeSeriesPanel(data, "resid", "smooth", RESID_AXIS_INFO_PARAM));
			
		add("Center", seriesPanel);
		
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		ExpSmoothVariable expSmoothVar = new ExpSmoothVariable(translate("Smoothed"), data, "resid");
		expSmoothVar.setExtraDecimals(2);
		data.addVariable("smooth", expSmoothVar);
		
		data.addVariable("predict", new PredictionVariable("Prediction", data, "y", "time",
																																			"model", "smooth"));
		
		return data;
	}
	
	protected TimeView getTimeView(DataSet data, IndexTimeAxis horizAxis, VertAxis vertAxis) {
		return new TimeView(data, this, horizAxis, vertAxis);
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("West", modelChoicePanel(true));		//	vertical
		
			expSmoothSlider = new ExpSmoothSlider(1.0, this);
		
		thePanel.add("Center", expSmoothSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == expSmoothSlider) {
			ExpSmoothVariable expSmoothVar = (ExpSmoothVariable)data.getVariable("smooth");
			expSmoothVar.setSmoothConst(expSmoothSlider.getExpSmoothConst());
															//		which calls data.variableChanged()
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