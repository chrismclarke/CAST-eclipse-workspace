package randomisationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import coreSummaries.*;
import imageGroups.*;

import corr.*;
import randomisation.*;


public class TwoYearCorrApplet extends CoreRandomisationApplet {
	static final private String MAX_CORR_PARAM = "maxCorr";
	static final private String CORR_NAME_PARAM = "corrName";
	static final private String X_NAME_PARAM = "xVarName";
	static final private String Y_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X2_VALUES_PARAM = "xValues2";
	static final private String Y2_VALUES_PARAM = "yValues2";
	static final private String LABELS2_PARAM = "labels2";
	static final private String INIT_FIXED_PARAM = "initFixed";
	
	private NumValue maxCorr;
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			
		data.addLabelVariable("team", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		data.addLabelVariable("team2", getParameter(LABEL_NAME_PARAM), getParameter(LABELS2_PARAM));
		
		data.addNumVariable("x", getParameter(X_NAME_PARAM), getParameter(X_VALUES_PARAM));
			RandomisedNumVariable yFixed = new RandomisedNumVariable(getParameter(Y_NAME_PARAM));
			yFixed.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yFixed);
		
		data.addNumVariable("x2", getParameter(X_NAME_PARAM), getParameter(X2_VALUES_PARAM));
		data.addNumVariable("y2", getParameter(Y_NAME_PARAM), getParameter(Y2_VALUES_PARAM));
			
			RandomisedNumVariable yRand = new RandomisedNumVariable(getParameter(Y_NAME_PARAM));
			yRand.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("yRand", yRand);
			
		return data;
	}

	
	protected SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "yRand");
		
			maxCorr = new NumValue(getParameter(MAX_CORR_PARAM));
			
			CorrVariable corrVar = new CorrVariable(getParameter(CORR_NAME_PARAM), "x", "yRand",
																																	maxCorr.decimals);
			
		summaryData.addVariable("stat", corrVar);
		
		return summaryData;
	}
	
	protected double getActualSimPropn() {
		return 0.5;
	}
	
	protected double getMaxAbsDiff() {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		NumVariable yVar = (NumVariable)data.getVariable("y");
		return CorrVariable.correlation(xVar, yVar);
	}
	
	
	protected XPanel dataPlotPanel(DataSet data, int actualOrRandomised) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			String pointsKey = (actualOrRandomised == ACTUAL) ? "y" : "yRand";
			int initFixed = Integer.parseInt(getParameter(INIT_FIXED_PARAM));
			LeagueYearsView tempView = new LeagueYearsView(data, this,
																"x", pointsKey, "x2", "y2", "team", "team2", initFixed);
		thePanel.add("Center", tempView);
					
		if (actualDataView == null)
			actualDataView = tempView;
		else
			randDataView = tempView;
		
		thePanel.add("South", createStatisticPanel(data, null, actualOrRandomised));
		
		return thePanel;
	}
	
	protected RandomisationInterface createAndAddView(XPanel targetPanel, DataSet data,
																								int actualOrRandomised, VertAxis numAxis) {
		return null;			//		Not used
	}
	
	protected XPanel createStatisticPanel(DataSet data, DataView dataView,
																																		int actualOrRandomised) {
		MeanSDImages.loadMeanSD(this);
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			String yKey = (actualOrRandomised == ACTUAL) ? "y" : "yRand";
		valuePanel.add(new CorrelationView(data, "x", yKey, CorrelationView.NO_FORMULA, this));
		return valuePanel;
	}
	
	protected boolean randomiseNotSimulate() {
		return true;
	}
}