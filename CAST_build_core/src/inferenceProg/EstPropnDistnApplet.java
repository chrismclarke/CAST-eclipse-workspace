package inferenceProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import coreSummaries.*;

import randomStat.*;


public class EstPropnDistnApplet extends EstDistnApplet {
	
	private String catValues[];
	private String catLabels[];
	
	protected DataSet getData() {
		int noOfDataSets = readCoreData();
		catValues = new String[noOfDataSets];
		catLabels = new String[noOfDataSets];
		for (int i=0 ; i<noOfDataSets ; i++) {
			String suffix = (i == 0) ? "" : String.valueOf(i+1);
			catValues[i] = getParameter(CAT_VALUES_PARAM + suffix);
			catLabels[i] = getParameter(CAT_LABELS_PARAM + suffix);
		}
		
		DataSet data = new DataSet();
		
		CatVariable yVar = new CatVariable(varName[0], Variable.USES_REPEATS);
		yVar.readLabels(catLabels[0]);
		yVar.readValues(catValues[0]);
		data.addVariable("y", yVar);
		
		CatDistnVariable dataDistn = new CatDistnVariable("data model");
		data.addVariable("fit", dataDistn);
		
		setEstimatedDistn(data, "y", "fit", displayDecimals[0]);
		
		return data;
	}
	
	protected void setEstimatedDistn(DataSet data, String varKey, String estKey, int decimals) {
		CatVariable y = (CatVariable)data.getVariable(varKey);
		int catCount[] = y.getCounts();
		int count = y.noOfValues();
		
		dataMean = catCount[0] / (double)count;
		dataSD = Math.sqrt(dataMean * (1.0 - dataMean));
		
		CatDistnVariable dataDistn = (CatDistnVariable)data.getVariable(estKey);
		dataDistn.readLabels(catLabels[dataSetSelection]);
		double[] newProbs = new double[catCount.length];
		for (int i=0 ; i<newProbs.length ; i++)
			newProbs[i] = catCount[i] / (double)count;
		dataDistn.setProbs(newProbs);
		
//		data.variableChanged(estKey);		//		changed for Netscape 4.5/Mac
		if (dataMeanView != null)
			dataMeanView.paint(dataMeanView.getGraphics());
		if (dataCountView != null)
			dataCountView.paint(dataCountView.getGraphics());
//		----------------  ( replacement should not be necessary)
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		PropnVariable mean = new PropnVariable(meanName[0], "y", displayDecimals[0]);
		
		summaryData.addVariable("mean", mean);
		
		BinomialDistnVariable meanDistn = new BinomialDistnVariable("count distn");
		summaryData.addVariable("theory", meanDistn);
		
		NormalDistnVariable approxMeanDistn = new NormalDistnVariable("approx propn distn");
		summaryData.addVariable("normal", approxMeanDistn);
		
		return summaryData;
	}
	
	protected void setSummaryInfo(SummaryDataSet summaryData, String sumValueKey,
						String sumTheoryKey, DataSet sourceData, String sourceVarKey, int decimals,
						String summaryName) {
		NumVariable meanVar = (NumVariable)summaryData.getVariable(sumValueKey);
		meanVar.setDecimals(decimals);
		meanVar.name = summaryName;
		
		Variable y = (Variable)sourceData.getVariable(sourceVarKey);
		int count = y.noOfValues();
		
		NormalDistnVariable approxPropnTheory = (NormalDistnVariable)summaryData
																							.getVariable("normal");
		approxPropnTheory.setMean(dataMean);
		approxPropnTheory.setSD(dataSD / Math.sqrt(count));
		approxPropnTheory.setDecimals(decimals);
		
		BinomialDistnVariable propnTheory = (BinomialDistnVariable)summaryData
																							.getVariable(sumTheoryKey);
		propnTheory.setCount(count);
		propnTheory.setProb(dataMean);
		
		summaryData.setSingleSummaryFromData();
		summaryData.variableChanged(sumValueKey);
//		-------------------------		//		changed for Netscape 4.5/Mac
		if (summaryView != null)
			summaryView.paint(summaryView.getGraphics());
		if (estMeanView != null)
			estMeanView.paint(estMeanView.getGraphics());
		if (estSDView != null)
			estSDView.paint(estSDView.getGraphics());
//		----------------  ( addition should not be necessary)
	}
	
	protected int getMeanType() {
		return SampleMeanSDView.PROPN_MEAN;
	}
	
	protected int getSDType() {
		return SampleMeanSDView.PROPN_SD;
	}
	
	protected XPanel dataPanel(DataSet data, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		dataAxis = new HorizAxis(this);
		dataAxis.setCatLabels((CatVariable)data.getVariable(variableKey));
		dataAxis.setAxisName(varName[0]);
		thePanel.add("Bottom", dataAxis);
		
		VertAxis probAxis = new VertAxis(this);
		probAxis.readNumLabels("0 1 0 0.2");
		thePanel.add("Left", probAxis);
		
		BarPlusProbView localView = new BarPlusProbView(data, this, "y", "fit", (HorizAxis)dataAxis, probAxis);
		localView.setShowDensity(DataPlusDistnInterface.NO_DISTN);
		thePanel.add("Center", localView);
		localView.lockBackground(Color.white);
		dataView = localView;
		
		return thePanel;
	}
	
	protected DataPlusDistnInterface getSummaryView(SummaryDataSet summaryData,
																			String variableKey, String modelKey) {
		return new StackedDiscBinomialView(summaryData, this, summaryAxis, modelKey, "normal");
	}
	
	protected String checkBoxName() {
		return "Estimate distn of proportion";
	}
	
	protected void changeDataSet(int newChoice) {
		dataSetSelection = newChoice;
		
		summaryAxis.readNumLabels(axisInfo[dataSetSelection]);
		summaryAxis.setAxisName(meanName[dataSetSelection]);
//		summaryAxis.repaint();		//		changed for Netscape 4.5/Mac
		summaryAxis.paint(summaryAxis.getGraphics());
//		----------------  ( replacement should not be necessary)
		
		CatVariable y = (CatVariable)data.getVariable("y");
		y.name = varName[dataSetSelection];
		y.readLabels(catLabels[dataSetSelection]);
		y.readValues(catValues[dataSetSelection]);
		
		dataAxis.setCatLabels((CatVariable)data.getVariable("y"));
		dataAxis.setAxisName(varName[dataSetSelection]);
//		dataAxis.repaint();		//		changed for Netscape 4.5/Mac
		dataAxis.paint(dataAxis.getGraphics());
//		----------------  ( replacement should not be necessary)
		
		data.variableChanged("y");
//		---------------------		//		changed for Netscape 4.5/Mac
		dataView.paint(dataView.getGraphics());
//		----------------  ( addition should not be necessary)
		
		setEstimatedDistn(data, "y", "fit", displayDecimals[dataSetSelection]);

		setSummaryInfo(summaryData, "mean", "theory", data, "y",
											displayDecimals[dataSetSelection], meanName[dataSetSelection]);
	}
}