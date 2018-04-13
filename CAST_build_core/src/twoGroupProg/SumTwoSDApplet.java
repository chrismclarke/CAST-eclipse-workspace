package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import distn.*;

import twoGroup.*;


public class SumTwoSDApplet extends SumTwoMeanApplet {
	static final private String X1_AXIS_INFO_PARAM = "x1Axis";
	static final private String X2_AXIS_INFO_PARAM = "x2Axis";
	static final private String SUM_AXIS_INFO_PARAM = "sumAxis";
	static final private String MEANS_PARAM = "means";
	static final private String SD_PARAM = "sd";
	static final private String MAX_SD_PARAM = "maxSD";
	
	private ParameterSlider sd1Slider, sd2Slider;
	protected double y1Mean, y2Mean;
	
	protected void startSetup() {
		sd1Slider = paramSlider("#sigma#", 1, getParameter(SD_PARAM));
		sd2Slider = paramSlider("#sigma#", 2, getParameter(SD_PARAM));
		
		maxSD = new NumValue(getParameter(MAX_SD_PARAM));
		
		StringTokenizer st = new StringTokenizer(getParameter(MEANS_PARAM));
		y1Mean = Double.parseDouble(st.nextToken());
		y2Mean = Double.parseDouble(st.nextToken());
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData) {
		double y1SD = sd1Slider.getParameter().toDouble();
		double y2SD = sd2Slider.getParameter().toDouble();
		
		y1Generator.setParameters(y1Mean, y1SD);
		y2Generator.setParameters(y2Mean, y2SD);
		
		NormalDistnVariable x1Distn = (NormalDistnVariable)summaryData.getVariable("x1Distn");
		x1Distn.setMean(y1Mean);
		x1Distn.setSD(y1SD);
		
		NormalDistnVariable x2Distn = (NormalDistnVariable)summaryData.getVariable("x2Distn");
		x2Distn.setMean(y2Mean);
		x2Distn.setSD(y2SD);
		
		NormalDistnVariable sumDistn = (NormalDistnVariable)summaryData.getVariable("sumTheory");
		sumDistn.setMean(y1Mean + y2Mean);
		sumDistn.setSD(Math.sqrt(y1SD * y1SD + y2SD * y2SD));
	}
	
	protected XPanel subDataPanel(DataSet data, SummaryDataSet summaryData, int index) {
		switch (index) {
			case 0:
				{
				String axisInfo = getParameter(X1_AXIS_INFO_PARAM);
				return oneDataPanel(summaryData, "x1", "x1Distn", axisInfo, Color.black, kDataDensityColor);
				}
			case 1:
				{
				String axisInfo = getParameter(X2_AXIS_INFO_PARAM);
				return oneDataPanel(summaryData, "x2", "x2Distn", axisInfo, Color.black, kDataDensityColor);
				}
			default:
				{
				String axisInfo = getParameter(SUM_AXIS_INFO_PARAM);
				return oneDataPanel(summaryData, "sum", "sumTheory", axisInfo, kSumColor, kSumDensityColor);
				}
		}
	}
	
	protected XPanel subSummaryPanel(DataSet data, SummaryDataSet summaryData, int index) {
		switch (index) {
			case 0:
				XPanel panel1 = new XPanel();
				panel1.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 8));
				panel1.add(sd1Slider);
				return panel1;
			case 1:
				{
				XPanel panel2 = new XPanel();
				panel2.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 8));
				panel2.add(sd2Slider);
				return panel2;
				}
			default:
				{
				XPanel panel3 = new XPanel();
				panel3.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 8));
				
					SDImageView sdView = new SDImageView(summaryData, this, "diff/sigma1Plus2.gif", 20, "sumTheory", maxSD);
					sdView.setForeground(kSumColor);
					sdView.setFont(getBigBoldFont());
				panel3.add(sdView);
				
				return panel3;
				}
		}
	}
	
	private void adjustSampleValue(ParameterSlider slider, String dataKey, String distnKey) {
		double newSD = slider.getParameter().toDouble();
		NormalDistnVariable oldDistn = (NormalDistnVariable)summaryData.getVariable(distnKey);
		double mean = oldDistn.getMean().toDouble();
		double factor = newSD / oldDistn.getSD().toDouble();
		NumVariable xVar = (NumVariable)data.getVariable(dataKey);
		((NumValue)xVar.valueAt(0)).setValue(mean + (xVar.doubleValueAt(0) - mean) * factor);
	}

	
	private boolean localAction(Object target) {
		if (target == sd1Slider || target == sd2Slider) {
			if (target == sd1Slider)
				adjustSampleValue(sd1Slider, "y1", "x1Distn");
			else
				adjustSampleValue(sd2Slider, "y2", "x2Distn");
			setTheoryParameters(summaryData);
			summaryData.setSingleSummaryFromData();
//			summaryData.variableChanged(evt.target == mean1Slider ? "x1Distn" : "x2Distn");
//			summaryData.variableChanged("sumTheory");
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