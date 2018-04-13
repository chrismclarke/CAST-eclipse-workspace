package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.RandomNormal;
import distn.*;
import coreVariables.*;
import coreSummaries.*;

//import transform.*;
//import time.*;
import twoGroup.*;


public class SampleHeight2Applet extends CoreSumDiffApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String MEANS_PARAM = "means";
	static final private String SDS_PARAM = "sds";
	static final private String X1_NAME_PARAM = "x1Name";
	static final private String X2_NAME_PARAM = "x2Name";
	static final private String DIFF_NAME_PARAM = "diffName";
	static final private String FRAME_HEIGHT_PARAM = "frameHeight";
//	static final private String DIFF_DISTN_PARAM = "diffDistn";
	static final private String DIFF_AXIS_INFO_PARAM = "diffAxis";
	
	static final protected Color kX1Color = new Color(0x000099);				//	dark blue
	static final protected Color kX1DensityColor = new Color(0x99CCFF);	//	light blue
	static final private Color kX2Color = new Color(0x006600);					//	dark green
	static final private Color kX2DensityColor = new Color(0x66FF99);		//	light green
	static final private Color kDiffColor = new Color(0x990000);				//	dark red
	static final private Color kDiffDensityColor = new Color(0xFFCCCC);	//	light red
	
	private NumValue mean1, mean2, sd1, sd2;
	protected RandomNormal y1Generator, y2Generator;
	
	public void setupApplet() {
		startSetup();
		data = getData();
		summaryData = getSummaryData(data);
		
		setTheoryParameters(summaryData);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(0, 15));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																																ProportionLayout.TOTAL));
			mainPanel.add(ProportionLayout.LEFT, picturePanel(data));
			mainPanel.add(ProportionLayout.RIGHT, dataPanel(data, summaryData, noOfSubPanels()));
		
		add("Center", mainPanel);
		add("South", controlPanel());
	}
	
	protected void startSetup() {
		StringTokenizer st = new StringTokenizer(getParameter(MEANS_PARAM));
		mean1 = new NumValue(st.nextToken());
		mean2 = new NumValue(st.nextToken());
		
		st = new StringTokenizer(getParameter(SDS_PARAM));
		sd1 = new NumValue(st.nextToken());
		sd2 = new NumValue(st.nextToken());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		y1Generator = new RandomNormal(1, 0.0, 1.0, 3.0);
		NumVariable y1 = new NumSampleVariable("Y1", y1Generator, 10);
		data.addVariable("y1", y1);
		
		y2Generator = new RandomNormal(1, 0.0, 1.0, 3.0);
		NumVariable y2 = new NumSampleVariable("Y2", y2Generator, 10);
		data.addVariable("y2", y2);
		
		data.addVariable("yy", new BiSampleVariable(data, "y1", "y2"));
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "yy");
		
			MeanVariable x1 = new MeanVariable(getParameter(X1_NAME_PARAM), "y1", 10);
		summaryData.addVariable("x1", x1);
		
			MeanVariable x2 = new MeanVariable(getParameter(X2_NAME_PARAM), "y2", 10);
		summaryData.addVariable("x2", x2);
		
			SumDiffVariable diff = new SumDiffVariable(getParameter(DIFF_NAME_PARAM), summaryData, "x2", "x1",
																																	SumDiffVariable.DIFF);
		summaryData.addVariable("diff", diff);
		
			NormalDistnVariable x1Distn = new NormalDistnVariable("x1 model");
		summaryData.addVariable("x1Distn", x1Distn);
		
			NormalDistnVariable x2Distn = new NormalDistnVariable("x2 model");
		summaryData.addVariable("x2Distn", x2Distn);
		
			NormalDistnVariable diffDistn = new NormalDistnVariable("diff theory");
		summaryData.addVariable("diffTheory", diffDistn);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData) {
		double m1 = mean1.toDouble();
		double s1 = sd1.toDouble();
		double m2 = mean2.toDouble();
		double s2 = sd2.toDouble();
		
		y1Generator.setParameters(m1, s1);
		y2Generator.setParameters(m2, s2);
		
		NormalDistnVariable x1Distn = (NormalDistnVariable)summaryData.getVariable("x1Distn");
		x1Distn.setMean(m1);
		x1Distn.setSD(s1);
		
		NormalDistnVariable x2Distn = (NormalDistnVariable)summaryData.getVariable("x2Distn");
		x2Distn.setMean(m2);
		x2Distn.setSD(s2);
		
		NormalDistnVariable diffDistn = (NormalDistnVariable)summaryData.getVariable("diffTheory");
		int meanDecimals = Math.max(mean1.decimals, mean2.decimals);
		int sdDecimals = Math.max(sd1.decimals, sd2.decimals) + 2;
		diffDistn.setDecimals(meanDecimals, sdDecimals);
		diffDistn.setMean(m2 - m1);
		diffDistn.setSD(Math.sqrt(s1 * s1 + s2 * s2));
	}
	
	private XPanel picturePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		Heights2View theView = new Heights2View(data, this, "y", "x", new NumValue(getParameter(FRAME_HEIGHT_PARAM)));
		theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private String getDistnString(NumValue mean, NumValue sd) {
		return "N(" + mean.toString() + ", " + sd.toString() + ")";
	}
	
	protected XPanel subDataPanel(DataSet data, SummaryDataSet summaryData, int index) {
		String axisInfo = getParameter(AXIS_INFO_PARAM);
		switch (index) {
			case 0:
				return oneDataPanel(summaryData, "x1", "x1Distn", axisInfo, kX1Color, kX1DensityColor,
																					getDistnString(mean1, sd1));
			case 1:
				return oneDataPanel(summaryData, "x2", "x2Distn", axisInfo, kX2Color, kX2DensityColor,
																					getDistnString(mean2, sd2));
			default:
				NormalDistnVariable diffDistn = (NormalDistnVariable)summaryData.getVariable("diffTheory");
				return oneDataPanel(summaryData, "diff", "diffTheory",
														getParameter(DIFF_AXIS_INFO_PARAM), kDiffColor, kDiffDensityColor,
														getDistnString(diffDistn.getMean(), diffDistn.getSD()));
		}
	}
	
	protected XPanel subSummaryPanel(DataSet data, SummaryDataSet summaryData, int index) {
		return null;
	}
	
	protected XPanel controlPanel() {
		return samplingPanel();
	}
}