package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.RandomNormal;
import distn.*;
import coreVariables.*;
import coreSummaries.*;
import formula.*;
import imageUtils.*;

import twoGroup.*;


public class SumTwoMeanApplet extends CoreSumDiffApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String SD_PARAM = "sd";
	static final private String MEANS_PARAM = "means";
	static final private String MAX_MEAN_SD_PARAM = "maxMeanSD";
	
	static final protected Color kSumColor = new Color(0x000099);				//	dark blue
	static final protected Color kSumDensityColor = new Color(0x99CCFF);	//	light blue
	static final private Color kSDColor = new Color(0x990000);				//	dark red
	
	private ParameterSlider mean1Slider, mean2Slider;
	
	private NumValue modelSD;
	protected RandomNormal y1Generator, y2Generator;
	protected NumValue maxMean, maxSD;
	
	protected ParameterSlider paramSlider(String paramName, int index, String paramRange) {
		StringTokenizer st = new StringTokenizer(paramRange);
		String minString = st.nextToken();
		String maxString = st.nextToken();
		int nSteps = Integer.parseInt(st.nextToken());
		NumValue startMean = new NumValue(st.nextToken());
		
		String mu = MText.expandText(paramName + "#sub" + index + "#");
		ParameterSlider theSlider = new ParameterSlider(new NumValue(minString), new NumValue(maxString),
									startMean, nSteps, mu, this);
//		theSlider.setTitleFont(getBigBoldFont());
		return theSlider;
	}
	
	protected void startSetup() {
		modelSD = new NumValue(getParameter(SD_PARAM));
		
		mean1Slider = paramSlider("#mu#", 1, getParameter(MEANS_PARAM));
		mean2Slider = paramSlider("#mu#", 2, getParameter(MEANS_PARAM));
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_MEAN_SD_PARAM));
		maxMean = new NumValue(st.nextToken());
		maxSD = new NumValue(st.nextToken());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		y1Generator = new RandomNormal(1, 0.0, 1.0, 3.0);
		NumVariable y1 = new NumSampleVariable(MText.expandText("Y#sub1#"), y1Generator, 10);
		data.addVariable("y1", y1);
		
		y2Generator = new RandomNormal(1, 0.0, 1.0, 3.0);
		NumVariable y2 = new NumSampleVariable(MText.expandText("Y#sub2#"), y2Generator, 10);
		data.addVariable("y2", y2);
		
		data.addVariable("yy", new BiSampleVariable(data, "y1", "y2"));
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "yy");
		
			MeanVariable x1 = new MeanVariable(MText.expandText("X#sub1#"), "y1", 10);
		summaryData.addVariable("x1", x1);
		
			MeanVariable x2 = new MeanVariable(MText.expandText("X#sub2#"), "y2", 10);
		summaryData.addVariable("x2", x2);
		
			String sumName = MText.expandText("Sum, X#sub1# + X#sub2#");
			SumDiffVariable sum = new SumDiffVariable(sumName, summaryData, "x1", "x2", SumDiffVariable.SUM);
		summaryData.addVariable("sum", sum);
		
			int decimals = maxSD.decimals;
			if (maxMean != null && maxMean.decimals > decimals)
				decimals = maxMean.decimals;
			NormalDistnVariable x1Distn = new NormalDistnVariable("x1 model");
			x1Distn.setDecimals(decimals);
		summaryData.addVariable("x1Distn", x1Distn);
		
			NormalDistnVariable x2Distn = new NormalDistnVariable("x2 model");
			x2Distn.setDecimals(decimals);
		summaryData.addVariable("x2Distn", x2Distn);
		
			NormalDistnVariable sumDistn = new NormalDistnVariable("sum theory");
			sumDistn.setDecimals(decimals);
		summaryData.addVariable("sumTheory", sumDistn);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData) {
		double y1Mean = mean1Slider.getParameter().toDouble();
		double y2Mean = mean2Slider.getParameter().toDouble();
		
		y1Generator.setParameters(y1Mean, modelSD.toDouble());
		y2Generator.setParameters(y2Mean, modelSD.toDouble());
		
		NormalDistnVariable x1Distn = (NormalDistnVariable)summaryData.getVariable("x1Distn");
		x1Distn.setMean(y1Mean);
		x1Distn.setSD(modelSD.toDouble());
		
		NormalDistnVariable x2Distn = (NormalDistnVariable)summaryData.getVariable("x2Distn");
		x2Distn.setMean(y2Mean);
		x2Distn.setSD(modelSD.toDouble());
		
		NormalDistnVariable sumDistn = (NormalDistnVariable)summaryData.getVariable("sumTheory");
		sumDistn.setMean(y1Mean + y2Mean);
		sumDistn.setSD(modelSD.toDouble() * Math.sqrt(2));
	}
	
	protected XPanel subDataPanel(DataSet data, SummaryDataSet summaryData, int index) {
		String axisInfo = getParameter(AXIS_INFO_PARAM);
		switch (index) {
			case 0:
				return oneDataPanel(summaryData, "x1", "x1Distn", axisInfo, Color.black, kDataDensityColor);
			case 1:
				return oneDataPanel(summaryData, "x2", "x2Distn", axisInfo, Color.black, kDataDensityColor);
			default:
				return oneDataPanel(summaryData, "sum", "sumTheory", axisInfo, kSumColor, kSumDensityColor);
		}
	}
	
	protected XPanel subSummaryPanel(DataSet data, SummaryDataSet summaryData, int index) {
		switch (index) {
			case 0:
				XPanel panel1 = new XPanel();
				panel1.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 8));
				panel1.add(mean1Slider);
				FixedValueImageView x1SDView = new FixedValueImageView("diff/sigmaRed.gif", 8, modelSD,
																															modelSD.toDouble(), this);
				x1SDView.unboxValue();
				x1SDView.setFont(getBigBoldFont());
				x1SDView.setForeground(kSDColor);
				panel1.add(x1SDView);
				return panel1;
			case 1:
				{
				XPanel panel2 = new XPanel();
				panel2.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 8));
				panel2.add(mean2Slider);
				FixedValueImageView x2SDView = new FixedValueImageView("diff/sigmaRed.gif", 8, modelSD,
																															modelSD.toDouble(), this);
				x2SDView.unboxValue();
				x2SDView.setFont(getBigBoldFont());
				x2SDView.setForeground(kSDColor);
				panel2.add(x2SDView);
				return panel2;
				}
			default:
				{
				XPanel panel3 = new XPanel();
				panel3.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 8));
				
					SDImageView meanView = new SDImageView(summaryData, this, "diff/mu1Plus2.gif", 9,
																																		"sumTheory", maxMean);
					meanView.setSDNotMean(false);
					meanView.setForeground(kSumColor);
					meanView.setFont(getBigBoldFont());
				panel3.add(meanView);
				
					NumValue sumSD = new NumValue(modelSD.toDouble() * Math.sqrt(2.0), maxSD.decimals);
					FixedValueImageView sdView = new FixedValueImageView("diff/sigma1Plus2Same.gif", 12, sumSD,
																																								sumSD.toDouble(), this);
					sdView.unboxValue();
					sdView.setForeground(kSDColor);
					sdView.setFont(getBigBoldFont());
				panel3.add(sdView);
				
				return panel3;
				}
		}
	}
	
	protected XPanel controlPanel() {
		return samplingPanel();
	}
	
	private void adjustSampleValue(ParameterSlider slider, String dataKey, String distnKey) {
		double newMean = slider.getParameter().toDouble();
		NormalDistnVariable oldDistn = (NormalDistnVariable)summaryData.getVariable(distnKey);
		double shift = newMean - oldDistn.getMean().toDouble();
		NumVariable xVar = (NumVariable)data.getVariable(dataKey);
		NumValue x0 = (NumValue)xVar.valueAt(0);
		x0.setValue(x0.toDouble() + shift);
	}

	
	private boolean localAction(Object target) {
		if (target == mean1Slider || target == mean2Slider) {
			if (target == mean1Slider)
				adjustSampleValue(mean1Slider, "y1", "x1Distn");
			else
				adjustSampleValue(mean2Slider, "y2", "x2Distn");
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