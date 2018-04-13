package randomisationProg;

import java.awt.*;

import dataView.*;
import axis.*;

import randomisation.*;


public class RandomisedPivotApplet extends CoreRandomisationApplet {
	static final private String MAX_DIFF_PARAM = "maxDiff";
	static final private String DIFF_NAME_PARAM = "diffName";
	
	static final private String kLabels = "below above";
	
	private NumValue maxDiff;
	private double median;
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		median = PivotedVariable.getMedian(data, "y");
		int initGroups[] = new int[yVar.noOfValues()];
		
		ValueEnumeration e = yVar.values();
		int index = 0;
		while (e.hasMoreValues())
			initGroups[index ++] = (e.nextDouble() < median) ? 0 : 1;
		
		CatVariable actualPivotSideVar = new CatVariable("SideOfMedian");
		actualPivotSideVar.readLabels(kLabels);
		actualPivotSideVar.setValues(initGroups);
		data.addVariable("trueRand", actualPivotSideVar);
		
		RandomisedCatVariable randPivotSideVar = new RandomisedCatVariable("SideOfMedian");
		randPivotSideVar.readLabels(kLabels);
		randPivotSideVar.setValues(initGroups);
		data.addVariable("randRand", randPivotSideVar);
		
		data.addVariable("yRand", new PivotedVariable(yVar.name, data, "y", "randRand", median));
			
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		summaryData = super.getSummaryData(data);
		
			maxDiff = new NumValue(getParameter(MAX_DIFF_PARAM));
			MeanMedDiffVariable diff = new MeanMedDiffVariable(getParameter(DIFF_NAME_PARAM),
																									"yRand", median, maxDiff.decimals);
		summaryData.addVariable("stat", diff);
		
		return summaryData;
	}
	
	protected double getActualSimPropn() {
		return 0.45;
	}
	
	protected double getMaxAbsDiff() {
		double mean = ((RoundMedianDotView)actualDataView).getMean();
		return Math.abs(mean - median);
	}
	
	protected RandomisationInterface createAndAddView(XPanel targetPanel, DataSet data,
																								int actualOrRandomised, VertAxis numAxis) {
		String yKey = (actualOrRandomised == ACTUAL) ? "y" : "yRand";
		
			RoundMedianDotView dataView = new RoundMedianDotView(data, this, numAxis, "trueRand", "trueRand");
			dataView.setActiveNumVariable(yKey);
			dataView.lockBackground(Color.white);
		targetPanel.add("Center", dataView);
		
		return dataView;
	}
	
	protected XPanel createStatisticPanel(DataSet data, DataView dataView,
																																		int actualOrRandomised) {
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		valuePanel.add(new MeanMedianValueView(data, this, (RoundMedianDotView)dataView, maxDiff));
		return valuePanel;
	}
	
	protected boolean randomiseNotSimulate() {
		return true;
	}
}