package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import inference.*;


public class AdjustZIntervalApplet extends SampleIntervalApplet {
	static final protected String K_LIMITS_PARAM = "kLimits";
	
	private ParameterSlider kSlider;
	
	private NumValue kMin, kStart, kMax;
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		MeanCIVariable ci = (MeanCIVariable)summaryData.getVariable("ci");
		
		StringTokenizer st = new StringTokenizer(getParameter(K_LIMITS_PARAM));
		kMin = new NumValue(st.nextToken());
		kMax = new NumValue(st.nextToken());
		kStart = new NumValue(st.nextToken());
		
		ci.setT(kStart.toDouble());
		
		return summaryData;
	}
	
	protected boolean onlyShowSummaryScale() {
		return true;
	}
	
	protected XPanel sampleControlPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 40));
		
		thePanel.add("Center", super.sampleControlPanel(summaryData));
		
			kSlider = new ParameterSlider(kMin, kMax, kStart, translate("Constant") + ", k",  this);
			kSlider.setFont(getStandardBoldFont());
		thePanel.add("South", kSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == kSlider) {
			NumValue kVal = kSlider.getParameter();
			MeanCIVariable ci = (MeanCIVariable)summaryData.getVariable("ci");
			ci.setT(kVal.toDouble());
			summaryData.valueChanged(ci.noOfValues() - 1);
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