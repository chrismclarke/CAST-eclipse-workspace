package ssq;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;


public class R2Slider extends XNoValueSlider {
	static final private int kStepMax = 100;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	private String adjustedVarKey, responseVarKey;
	private double maxR2;
	
	public R2Slider(XApplet applet, DataSet data, String adjustedVarKey,
											String responseVarKey, SummaryDataSet summaryData, String title,
											double initialR2) {
		this(applet, data, adjustedVarKey, responseVarKey, summaryData, title,
												applet.translate("All unexplained"), applet.translate("All explained"), initialR2);
	}
	
	public R2Slider(XApplet applet, DataSet data, String adjustedVarKey,
													String responseVarKey, SummaryDataSet summaryData,
													String title, String lowText, String highText, double initialR2) {
		this(applet, data, adjustedVarKey, responseVarKey, summaryData, title, lowText,
																																		highText, initialR2, 1.0);
	}
	
	public R2Slider(XApplet applet, DataSet data, String adjustedVarKey,
											String responseVarKey, SummaryDataSet summaryData, String title,
											String lowText, String highText, double initialR2, double maxR2) {
		super(lowText, highText, title, 0, kStepMax,
										(int)Math.round(kStepMax * initialR2), applet);
		this.data = data;
		this.summaryData = summaryData;
		this.adjustedVarKey = adjustedVarKey;
		this.responseVarKey = responseVarKey;
		this.maxR2 = maxR2;
	}
	
	protected double getR2() {
		return getR2(getValue());
	}
	
	protected double getR2(int val) {
		return (val * 0.01 * maxR2);
	}
	
	public void updateForNewR2() {
		AdjustedSsqVariable yVariable = (AdjustedSsqVariable)data.getVariable(adjustedVarKey);
		yVariable.setR2(getR2());
		CoreModelVariable lsFit = (CoreModelVariable)data.getVariable("ls");
		lsFit.updateLSParams(responseVarKey);
//		data.variableChanged(adjustedVarKey);
		data.valueChanged(-1);		// Don't change selection
		
		if (summaryData != null)
			summaryData.setSingleSummaryFromData();
	}
	
	private boolean localAction(Object target) {
		updateForNewR2();
		return false;			// to allow applet to do further processing of event
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}