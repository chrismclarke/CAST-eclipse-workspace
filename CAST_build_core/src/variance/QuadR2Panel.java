package variance;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;


public class QuadR2Panel extends XPanel {
	static final private int kStepMax = 100;
	
	static final private Color kDarkBlue = new Color(0x000099);
	static final private Color kDarkRed = new Color(0x990000);
	
	private CoreModelDataSet data;
	private SummaryDataSet summaryData;
	private String varKey;
	
	private XNoValueSlider overallR2Slider, quadR2Slider;
	
	
	public QuadR2Panel(XApplet applet, CoreModelDataSet data, String varKey, SummaryDataSet summaryData,
																			String overallR2Labels, double initialOverallR2,
																			String quadR2Labels, double initialQuadR2) {
		this.data = data;
		this.summaryData = summaryData;
		this.varKey = varKey;
		
		overallR2Slider = createSlider(overallR2Labels, applet, initialOverallR2);
		overallR2Slider.setForeground(kDarkBlue);
		
		quadR2Slider = createSlider(quadR2Labels, applet, initialQuadR2);
		quadR2Slider.setForeground(kDarkRed);
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
		add(overallR2Slider);
		add(quadR2Slider);
	}
	
	private XNoValueSlider createSlider(String labels, XApplet applet, double initialR2) {
		StringTokenizer st = new StringTokenizer(labels, "#");
		String title = st.nextToken();
		String lowText = st.nextToken();
		String highText = st.nextToken();
		return new XNoValueSlider(lowText, highText, title, 0, kStepMax,
										(int)Math.round(kStepMax * initialR2), applet);
	}
	
	private double getOverallR2() {
		return getOverallR2(overallR2Slider.getValue());
	}
	
	private double getOverallR2(int val) {
		return (val * 0.01);
	}
	
	private double getQuadR2() {
		return getQuadR2(quadR2Slider.getValue());
	}
	
	private double getQuadR2(int val) {
		return (val * 0.01);
	}

	
	private boolean localAction(Object target) {
		CoreVariable yCore = data.getVariable(varKey);
		if (yCore instanceof AdjustedQuadVariable) {
			AdjustedQuadVariable yVariable = (AdjustedQuadVariable)data.getVariable(varKey);
			yVariable.setR2(getOverallR2(), getQuadR2());
		}
		else {
			AdjustedPureVariable yVariable = (AdjustedPureVariable)data.getVariable(varKey);
			yVariable.setR2(getOverallR2(), getQuadR2());
		}
		
		data.updateForNewSample();
		data.variableChanged(varKey);
		
		if (summaryData != null)
			summaryData.setSingleSummaryFromData();
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}