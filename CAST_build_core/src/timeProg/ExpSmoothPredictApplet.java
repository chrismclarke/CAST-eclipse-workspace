package timeProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;

import time.*;


public class ExpSmoothPredictApplet extends BasicTimeApplet {
	static final private String TIME_SEQUENCE_PARAM = "timeSequence";
	static final private String SERIES_LIMITS_PARAM = "seriesLimits";
	
	private FitPredictValueView smooth;
	
	private ExpSmoothSlider expSmoothSlider;
	private ParameterSlider lastYearSlider;
	
	private int minEndYear, maxEndYear, startEndYear;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		NumVariable yRaw = new NumVariable(getParameter(VAR_NAME_PARAM));
		yRaw.readValues(getParameter(VALUES_PARAM));
		data.addVariable("yRaw", yRaw);
		
		StringTokenizer st = new StringTokenizer(getParameter(SERIES_LIMITS_PARAM));
		minEndYear = Integer.parseInt(st.nextToken());
		maxEndYear = Integer.parseInt(st.nextToken());
		startEndYear = Integer.parseInt(st.nextToken());
		data.addVariable("y", new TruncatedSeriesVariable(getParameter(VAR_NAME_PARAM),
																													yRaw, startEndYear - minEndYear));
		
		NumVariable timeVar = new NumVariable(getParameter(TIME_NAME_PARAM));
		timeVar.readSequence(getParameter(TIME_SEQUENCE_PARAM));
		data.addVariable("time", timeVar);
		
		ExpSmoothVariable expSmoothVar = new ExpSmoothVariable(translate("Smoothed"), data, "y");
		expSmoothVar.setExtraDecimals(2);
		data.addVariable("smooth", expSmoothVar);
		
		return data;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
		OneValueView actual = new OneValueView(data, "y", this);
		actual.setForeground(kActualColor);
		actual.setLabel("Actual =");
		thePanel.add(actual);
		
		smooth = new FitPredictValueView(data, "smooth", "y", this, "Smoothed value =", "Prediction =");
		smooth.setForeground(kSmoothedColor);
		thePanel.add(smooth);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																													ProportionLayout.TOTAL));
			
			String timeName = getParameter(TIME_NAME_PARAM);
			if (timeName == null)
				timeName = "Index";
			lastYearSlider = new ParameterSlider(new NumValue(minEndYear, 0),
													new NumValue(maxEndYear, 0), new NumValue(startEndYear, 0),
													"Last " + timeName, ParameterSlider.SHOW_MIN_MAX, this);
		thePanel.add("Left", lastYearSlider);
			
			expSmoothSlider = new ExpSmoothSlider(1.0, this);
			
		thePanel.add("Right", expSmoothSlider);
		return thePanel;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"smooth"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return true;
	}
	
	protected boolean showSmoothedValue() {
		return true;
	}

	
	private boolean localAction(Object target) {
		if (target == expSmoothSlider) {
			ExpSmoothVariable expSmoothVar = (ExpSmoothVariable)getData().getVariable("smooth");
			expSmoothVar.setSmoothConst(expSmoothSlider.getExpSmoothConst());
															//		which calls data.variableChanged()
			return true;
		}
		else if (target == lastYearSlider) {
			TruncatedSeriesVariable yVar = (TruncatedSeriesVariable)getData().getVariable("y");
			int lastYearIndex = lastYearSlider.getValue();
			yVar.setLastIndex(lastYearIndex);
			getData().variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}