package inferenceProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import coreSummaries.*;

import randomStat.*;


public class EstMeanDistnApplet extends EstDistnApplet {
	
	private String varValues[];
	
	protected DataSet getData() {
		int noOfDataSets = readCoreData();
		varValues = new String[noOfDataSets];
		for (int i=0 ; i<noOfDataSets ; i++) {
			String suffix = (i == 0) ? "" : String.valueOf(i+1);
			varValues[i] = getParameter(VALUES_PARAM + suffix);
		}
		
		DataSet data = new DataSet();
		
		data.addNumVariable("y", varName[0], varValues[0]);
		
		NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
		data.addVariable("fit", dataDistn);
		
		setEstimatedDistn(data, "y", "fit", displayDecimals[0]);
		
		return data;
	}
	
	protected void setEstimatedDistn(DataSet data, String varKey, String estKey, int decimals) {
		NumVariable y = (NumVariable)data.getVariable(varKey);
		double sum = 0.0;
		double sum2 = 0.0;
		ValueEnumeration ye = y.values();
		while (ye.hasMoreValues()) {
			double val = ye.nextDouble();
			sum += val;
			sum2 += val * val;
		}
		int count = y.noOfValues();
		
		dataMean = sum / count;
		dataSD = Math.sqrt((sum2 - sum * sum / count) / (count - 1));
		
		NormalDistnVariable dataDistn = (NormalDistnVariable)data.getVariable(estKey);
		dataDistn.setMean(dataMean);
		dataDistn.setSD(dataSD);
		dataDistn.setDecimals(decimals);
//		data.variableChanged(estKey);		//		changed for Netscape 4.5/Mac
		if (dataMeanView != null)
			dataMeanView.paint(dataMeanView.getGraphics());
		if (dataSDView != null)
			dataSDView.paint(dataSDView.getGraphics());
//		----------------  ( replacement should not be necessary)
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		MeanVariable mean = new MeanVariable(meanName[0], "y", displayDecimals[0]);
		
		summaryData.addVariable("mean", mean);
		
		NormalDistnVariable meanDistn = new NormalDistnVariable("mean distn");
		summaryData.addVariable("theory", meanDistn);
		
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
		
		NormalDistnVariable meanTheory = (NormalDistnVariable)summaryData
																							.getVariable(sumTheoryKey);
		meanTheory.setMean(dataMean);
		meanTheory.setSD(dataSD / Math.sqrt(count));
		meanTheory.setDecimals(decimals);
		
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
		return SampleMeanSDView.MEAN;
	}
	
	protected int getSDType() {
		return SampleMeanSDView.SD;
	}
	
	protected XPanel dataPanel(DataSet data, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		dataAxis = getAxis(data, variableKey);
		thePanel.add("Bottom", dataAxis);
		
		dataView = new StackedDotPlotView(data, this, dataAxis);
		thePanel.add("Center", dataView);
		dataView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected DataPlusDistnInterface getSummaryView(SummaryDataSet summaryData,
																			String variableKey, String modelKey) {
		return new JitterPlusNormalView(summaryData, this, summaryAxis, modelKey, 0.0);
	}
	
	protected String checkBoxName() {
		return "Estimate distn of mean";
	}
	
	protected void changeDataSet(int newChoice) {
		dataSetSelection = newChoice;
		
		dataAxis.readNumLabels(axisInfo[dataSetSelection]);
		dataAxis.setAxisName(varName[dataSetSelection]);
//		dataAxis.repaint();		//		changed for Netscape 4.5/Mac
		dataAxis.paint(dataAxis.getGraphics());
//		----------------  ( replacement should not be necessary)
		
		summaryAxis.readNumLabels(axisInfo[dataSetSelection]);
		summaryAxis.setAxisName(meanName[dataSetSelection]);
//		summaryAxis.repaint();		//		changed for Netscape 4.5/Mac
		summaryAxis.paint(summaryAxis.getGraphics());
//		----------------  ( replacement should not be necessary)
		
		NumVariable y = (NumVariable)data.getVariable("y");
		y.name = varName[dataSetSelection];
		y.readValues(varValues[dataSetSelection]);
		data.variableChanged("y");
//		---------------------		//		changed for Netscape 4.5/Mac
		dataView.paint(dataView.getGraphics());
//		----------------  ( addition should not be necessary)
		
		setEstimatedDistn(data, "y", "fit", displayDecimals[dataSetSelection]);

		setSummaryInfo(summaryData, "mean", "theory", data, "y",
											displayDecimals[dataSetSelection], meanName[dataSetSelection]);
	}
}