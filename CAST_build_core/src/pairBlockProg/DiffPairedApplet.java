package pairBlockProg;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;
import coreVariables.*;

import pairBlock.*;


public class DiffPairedApplet extends DiffGroupApplet {
	static final private String SELECTION_VALUES_PARAM = "selectionValues";
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
			SumDiffVariable diffVar = new SumDiffVariable("Difference", data,
																												"y2", "y1", SumDiffVariable.DIFF);
		data.addVariable("diff", diffVar);
		
			RandomisedCatVariable selectionVar = new RandomisedCatVariable("Selection");
			selectionVar.readLabels("Selected #Not selected#");
			selectionVar.readValues(getParameter(SELECTION_VALUES_PARAM));
			
		data.addVariable("selection", selectionVar);
		
		data.addVariable("random", new BiSampleVariable(data, "errors", "selection"));
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "random");
		
			maxDiff = new NumValue(getParameter(MAX_DIFF_PARAM));
		summaryData.addVariable("ci", new PairedCIVariable(translate("95% CI") + " =",
																	sourceData, "diff", "selection", 0.95, maxDiff.decimals));
		summaryData.addVariable("pValue", new PairedPValueVariable(translate("p-value") + " =",
																		sourceData, "diff", "selection", kMaxPValue.decimals));
		
		return summaryData;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			joinPointsCheck = new XCheckbox(translate("Show all values"), this);
			joinPointsCheck.setState(false);
		thePanel.add(joinPointsCheck);
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT,
																											VerticalLayout.VERT_CENTER, 5));
				OneValueView indexView = new OneValueView(data, "index", this);
			dataPanel.add(indexView);
			
				LabelEnumeration groupNames = new LabelEnumeration(getParameter(GROUP_NAMES_PARAM));
				OneValueView y1View = new OneValueView(data, "y1", this);
				y1View.setLabel((String)groupNames.nextElement());
			dataPanel.add(y1View);
			
				OneValueView y2View = new OneValueView(data, "y2", this);
				y2View.setLabel((String)groupNames.nextElement());
			dataPanel.add(y2View);
			
		thePanel.add(dataPanel);
		
		return thePanel;
	}
	
	protected TwoGroupPairedView getDataView(DataSet data, VertAxis yAxis,
																															HorizAxis theGroupAxis) {
		return new PairedPairedView(data, this, "y1", "y2",  "selection", yAxis, theGroupAxis, 0.5);
	}
}