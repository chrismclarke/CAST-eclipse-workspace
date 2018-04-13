package varianceProg;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.*;
import distn.*;
import utils.*;
import coreVariables.*;
import models.*;

import twoGroup.*;
import ssq.*;
import variance.*;


public class FandT2Applet extends FandTApplet {
	
	static private String kTwoGroupComponentNames[] = {"Total", "Between groups", "Within groups"};
	
	
	protected void setupComponentNames() {
		componentNames = kTwoGroupComponentNames;
	}
	
	protected double getFDisplayProportion() {
		return 0.30;
	}
	
	protected DataSet getData() {
		GroupsDataSet data = new GroupsDataSet(this);
		
		data.addBasicComponents();
		
		return data;
	}
	
	
	protected AnovaSummaryData getSummaryData(DataSet sourceData) {
		AnovaSummaryData summaryData = new AnovaSummaryData(sourceData, "y",
								BasicComponentVariable.kComponentKey, maxSsq.decimals, kMaxRSquared.decimals);
		
			String meanKey = BasicComponentVariable.kComponentKey[1];
			String residKey = BasicComponentVariable.kComponentKey[2];
			SsqRatioVariable f = new SsqRatioVariable("F ratio", meanKey,
																		residKey, maxF.decimals, SsqRatioVariable.MEAN_SSQ);
		summaryData.addVariable("F", f);
		
			PowerVariable tValue = new PowerVariable(translate("t statistic"), f, 0.5, maxT.decimals);
		summaryData.addVariable("t", tValue);
		
			int nValues = ((NumVariable)sourceData.getVariable("y")).noOfValues();
			FDistnVariable fDistn = new FDistnVariable("F distn", 1, nValues - 2);
		summaryData.addVariable("fDistn", fDistn);
		
//			GroupsDataSet groupsData = (GroupsDataSet)sourceData;
			TwoGroupSummary diffMeans = new TwoGroupSummary("Diff means", 10,
																																TwoGroupSummary.DIFFERENCE);
		summaryData.addVariable("diffMeans", diffMeans);
		
			TDistnVariable tDistn = new TDistnVariable(translate("t distn"), nValues - 2);
		summaryData.addVariable("tDistn", tDistn);
		
		summaryData.setSingleSummaryFromData();
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis yAxis = new HorizAxis(this);
			GroupsDataSet groupsData = (GroupsDataSet)data;
			yAxis.readNumLabels(groupsData.getYAxisInfo());
		thePanel.add("Bottom", yAxis);
		
			VertAxis xAxis = new VertAxis(this);
			CatVariable xVar = (CatVariable)data.getVariable("x");
			xAxis.setCatLabels(xVar);
		thePanel.add("Left", xAxis);
		
			TwoGroupSpreadDotView dataView = new TwoGroupSpreadDotView(data, this, yAxis, xAxis, "y", "x");
			dataView.lockBackground(Color.white);
		
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	protected AnovaTableView anovaTable(AnovaSummaryData summaryData) {
		AnovaTableView table = new AnovaTableView(summaryData, this,
									BasicComponentVariable.kComponentKey, maxSsq, maxMeanSsq, maxF,
									AnovaTableView.SSQ_F_PVALUE);
		table.setComponentNames(componentNames);
		return table;
	}
	
	protected XPanel summaryValuePanel(AnovaSummaryData summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(new OneValueView(summaryData, "t", this, maxT));
		thePanel.add(new OneValueView(summaryData, "diffMeans", this));
		
		return thePanel;
	}
}