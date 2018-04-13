package randomisationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import coreSummaries.*;

import exerciseSD.*;


public class MeanSimApplet extends CoreStatDistanceApplet {
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String MEAN_SD_DECIMALS_PARAM = "meanSdDecimals";
	
	static final private String ACTUAL_MEAN_PARAM = "actualMean";
	
	static final private String MEAN_LABEL_PARAM = "meanLabel";
	static final private String SD_LABEL_PARAM = "sdLabel";
	static final private String N_LABEL_PARAM = "nLabel";
	
	
	protected double dataPanelPropn() {
		return 0.5;
	}
	
	protected String lowerCaseParam() {
		return "mean";
	}
	
	protected void readParameters() {
		super.readParameters();
		
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_INFO_PARAM));
		sampleSize = Integer.parseInt(st.nextToken());
		nullParam = new NumValue(st.nextToken());
		popnSd = new NumValue(st.nextToken());
		
		actualParam = new NumValue(getParameter(ACTUAL_MEAN_PARAM));
		
		paramSdDecimals = Integer.parseInt(getParameter(MEAN_SD_DECIMALS_PARAM));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		RandomNormal generator = new RandomNormal(sampleSize, nullParam.toDouble(), popnSd.toDouble(), 3.0);
		
		NumSampleVariable sv = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 9);
		data.addVariable("y", sv);
		
		return data;
	}
	
	protected void addStatistic(SummaryDataSet summaryData) {
		MeanVariable meanVar = new MeanVariable(translate("Mean"), "y", paramSdDecimals);
		summaryData.addVariable("stat", meanVar);
	}
	
	protected XPanel modelInfoPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			String meanLabel = getParameter(MEAN_LABEL_PARAM);
			XLabel meanText = new XLabel(meanLabel + " = " + nullParam.toString(), XLabel.CENTER, this);
			meanText.setFont(getStandardBoldFont());
			meanText.setForeground(kDarkRed);
		thePanel.add(meanText);
		
			String sdLabel = getParameter(SD_LABEL_PARAM);
			XLabel sdText = new XLabel(sdLabel + " = " + popnSd.toString(), XLabel.CENTER, this);
			sdText.setFont(getStandardBoldFont());
			sdText.setForeground(kDarkRed);
		thePanel.add(sdText);
		
			String nLabel = getParameter(N_LABEL_PARAM);
			XLabel sampleSizeText = new XLabel(nLabel + " " + sampleSize, XLabel.CENTER, this);
			sampleSizeText.setFont(getStandardBoldFont());
			sampleSizeText.setForeground(kDarkRed);
		
		thePanel.add(sampleSizeText);
		
		return thePanel;
	}
	
	protected XPanel sampleViewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis yAxis = new HorizAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			yAxis.setAxisName(data.getVariable("y").name);
		
		thePanel.add("Bottom", yAxis);
		
			StackMeanSdView dataView = new StackMeanSdView(data, this, yAxis, "y", paramSdDecimals);
			dataView.setDrawSd(false);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	protected String summaryName(DataSet data) {
		return translate("Mean") + " " + data.getVariable("y").name;
	}
}