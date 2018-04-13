package randomisationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import coreSummaries.*;

import randomisation.*;


public class RandomisedGroupApplet extends CoreRandomisationApplet {
	static final private String MAX_DIFF_PARAM = "maxDiff";
	static final private String DIFF_NAME_PARAM = "diffName";
	
	private NumValue maxDiff;
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
		data.addCatVariable("trueRand", getParameter(CAT_NAME_PARAM),
												getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
		
		RandomisedCatVariable groupVar = new RandomisedCatVariable(getParameter(CAT_NAME_PARAM));
		groupVar.readLabels(getParameter(CAT_LABELS_PARAM));
		groupVar.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("randRand", groupVar);
			
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		summaryData = super.getSummaryData(data);
		
			maxDiff = new NumValue(getParameter(MAX_DIFF_PARAM));
			DiffSummaryVariable diff = new DiffSummaryVariable(getParameter(DIFF_NAME_PARAM),
																											"y", "randRand", maxDiff.decimals);
		summaryData.addVariable("stat", diff);
		
		return summaryData;
	}
	
	protected double getActualSimPropn() {
		return 0.5;
	}
	
	protected double getMaxAbsDiff() {
		double mean[] = ((TwoGroupDotView)actualDataView).getMeans();
		return Math.abs(mean[1] - mean[0]);
	}
	
	protected RandomisationInterface createAndAddView(XPanel targetPanel, DataSet data,
																								int actualOrRandomised, VertAxis numAxis) {
		String catKey = (actualOrRandomised == ACTUAL) ? "trueRand" : "randRand";
		
			HorizAxis catAxis = new HorizAxis(this);
			CatVariable groupVariable = (CatVariable)data.getVariable(catKey);
			catAxis.setCatLabels(groupVariable);
		targetPanel.add("Bottom", catAxis);
		
			TwoGroupDotView dataView = new TwoGroupDotView(data, this, numAxis, catAxis, catKey, "trueRand");
			dataView.setActiveNumVariable("y");
			dataView.lockBackground(Color.white);
		targetPanel.add("Center", dataView);
		
		return dataView;
	}
	
	protected XPanel createStatisticPanel(DataSet data, DataView dataView,
																																		int actualOrRandomised) {
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		valuePanel.add(new MeanDiffValueView(data, this, (TwoGroupDotView)dataView, maxDiff));
		return valuePanel;
	}
	
	protected boolean randomiseNotSimulate() {
		return true;
	}
}