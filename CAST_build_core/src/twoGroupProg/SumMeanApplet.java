package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.RandomNormal;
import distn.*;
import coreVariables.*;
import coreSummaries.*;
import imageUtils.*;

import twoGroup.*;


public class SumMeanApplet extends CoreSumDiffApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String NORMAL_PARAM = "normal";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String MAX_SD_PARAM = "maxSD";
	
	static final private Color kMeanColor = new Color(0x000099);				//	dark blue
	static final private Color kMeanDensityColor = new Color(0x99CCFF);	//	light blue
	static final private Color kSumColor = new Color(0x006600);					//	dark green
	static final private Color kSumDensityColor = new Color(0x66FF99);	//	light green
	
	private ParameterSlider nSlider;
	private int sampleSize;
	
	private double modelMean, modelSD;
	private int sdDecimals;
	
	private void setupSampleSizes() {
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_SIZE_PARAM));
		int minCount = Integer.parseInt(st.nextToken());
		int maxCount = Integer.parseInt(st.nextToken());
		sampleSize = Integer.parseInt(st.nextToken());
		nSlider = new ParameterSlider(new NumValue(minCount, 0), new NumValue(maxCount, 0),
												new NumValue(sampleSize, 0), translate("Sample size") + ", n", this);
	}
	
	protected void startSetup() {
		setupSampleSizes();
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
		dataDistn.setParams(getParameter(NORMAL_PARAM));
		data.addVariable("model", dataDistn);
		
		modelMean = dataDistn.getMean().toDouble();
		modelSD = dataDistn.getSD().toDouble();
		sdDecimals = dataDistn.getSD().decimals;
		RandomNormal generator = new RandomNormal(sampleSize, modelMean, modelSD, 3.0);
		NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10);
		data.addVariable("y", y);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			MeanVariable mean = new MeanVariable(translate("Mean"), "y", 10);
		summaryData.addVariable("mean", mean);
		
			ScaledVariable sum = new ScaledVariable(translate("Sum"), mean, "mean", 0.0, sampleSize, 10);
		summaryData.addVariable("sum", sum);
		
			NormalDistnVariable meanDistn = new NormalDistnVariable("mean distn");
			meanDistn.setDecimals(sdDecimals);
		summaryData.addVariable("meanTheory", meanDistn);
		
			NormalDistnVariable sumDistn = new NormalDistnVariable("sum distn");
			sumDistn.setDecimals(sdDecimals);
		summaryData.addVariable("sumTheory", sumDistn);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData) {
		NormalDistnVariable meanDistn = (NormalDistnVariable)summaryData.getVariable("meanTheory");
		meanDistn.setMean(modelMean);
		meanDistn.setSD(modelSD / Math.sqrt(sampleSize));
		
		NormalDistnVariable sumDistn = (NormalDistnVariable)summaryData.getVariable("sumTheory");
		sumDistn.setMean(modelMean * sampleSize);
		sumDistn.setSD(modelSD * Math.sqrt(sampleSize));
	}
	
	protected XPanel subDataPanel(DataSet data, SummaryDataSet summaryData, int index) {
		String axisInfo = getParameter(AXIS_INFO_PARAM);
		switch (index) {
			case 0:
				return oneDataPanel(data, "y", "model", axisInfo, Color.black, kDataDensityColor);
			case 1:
				return oneDataPanel(summaryData, "mean", "meanTheory", axisInfo, kMeanColor, kMeanDensityColor);
			default:
				return oneDataPanel(summaryData, "sum", "sumTheory", axisInfo, kSumColor, kSumDensityColor);
		}
	}
	
	protected XPanel subSummaryPanel(DataSet data, SummaryDataSet summaryData, int index) {
		switch (index) {
			case 0:
				{
				XPanel panel1 = new XPanel();
				panel1.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				FixedValueImageView sdView = new FixedValueImageView("diff/sigma.gif", 8,
																	new NumValue(modelSD, sdDecimals), modelSD, this);
				sdView.unboxValue();
				sdView.setFont(getBigBoldFont());
				panel1.add(sdView);
				return panel1;
				}
			case 1:
				{
				NumValue maxSD = new NumValue(getParameter(MAX_SD_PARAM));
				XPanel panel2 = new XPanel();
				panel2.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				SDImageView sdView = new SDImageView(summaryData, this, "diff/sigmaMean.gif", 17, "meanTheory", maxSD);
				sdView.setFont(getBigBoldFont());
				sdView.setForeground(kMeanColor);
				panel2.add(sdView);
				return panel2;
				}
			default:
				{
				NumValue maxSD = new NumValue(getParameter(MAX_SD_PARAM));
				XPanel panel3 = new XPanel();
				panel3.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				SDImageView sdView = new SDImageView(summaryData, this, "diff/sigmaSum.gif", 15, "sumTheory", maxSD);
				sdView.setFont(getBigBoldFont());
				sdView.setForeground(kSumColor);
				panel3.add(sdView);
				return panel3;
				}
		}
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(30, 0));
		
		thePanel.add("West", samplingPanel());
			
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
				nSlider.setTitleFont(getStandardBoldFont());
			sliderPanel.add(nSlider);
		
		thePanel.add("Center", sliderPanel);
		
		return thePanel;
	}
	
	private void changeSampleSize() {
		ScaledVariable sum = (ScaledVariable)summaryData.getVariable("sum");
		sum.setScale(0.0, sampleSize, sum.getMaxDecimals());
		
		summaryData.changeSampleSize(sampleSize);
		setTheoryParameters(summaryData);
		summaryData.variableChanged("mean");
		summaryData.variableChanged("sum");
	}

	
	private boolean localAction(Object target) {
		if (target == nSlider) {
			sampleSize = (int)Math.round(nSlider.getParameter().toDouble());
			changeSampleSize();
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