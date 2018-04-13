package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreVariables.*;
import formula.*;


import percentile.*;


public class DragPercentileApplet extends DragFindPercentApplet {
	static final private String UNITS_PARAM = "units";
	
	static final private String kPercentageAxis = "0 100 0 20";
	static final private String kSingleValueAxis = "0 100 50 100";
																						//	only shows a single value, initialised later
	static final private String kZeroOneAxis = "0 1 3 1";						//		no values are shown
	
	static final private Color kFormulaBackground = new Color(0xDDDDFF);
	
	protected PercentileFormulaPanel formula;
	protected Percentile2FormulaPanel formula2;
	
	protected DataSet getReferenceData(DataSet data) {
		DataSet referenceData = super.getReferenceData(data);
		
			ScaledVariable propnVar = new ScaledVariable(translate("Proportion"),
											(NumVariable)referenceData.getVariable("ref"), "ref", 0.0, 0.01, 2);
		referenceData.addVariable("propn", propnVar);
		return referenceData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			XLabel cumLabel = new XLabel(translate("Cumulative percentage"), XLabel.LEFT, this);
			cumLabel.setForeground(Color.red);
		thePanel.add(cumLabel);
		return thePanel;
	}
	
	protected XPanel dataDisplayPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			String longVarName = getParameter(LONG_VAR_NAME_PARAM);
			horizAxis.setAxisName((longVarName == null) ? data.getVariable("y").name : longVarName);
			horizAxis.setForeground(CumFunctInverseView.kPercentArrowColor);
		thePanel.add("Bottom", horizAxis);
		
			DragMultiVertAxis vertAxis = new DragMultiVertAxis(this, 3);
			vertAxis.readNumLabels(kZeroOneAxis);
			vertAxis.readExtraNumLabels(kPercentageAxis);
			vertAxis.readExtraNumLabels(kSingleValueAxis);
			vertAxis.setStartAlternate(1);
			vertAxis.setForeground(Color.red);
		thePanel.add("Left", vertAxis);
		
			cumView = new CumFunctInverseView(data, this, horizAxis, referenceData, "ref", vertAxis);
		thePanel.add("Center", cumView);
		return thePanel;
	}
	
	protected XPanel propnCalcPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new InsetPanel(6, 6);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 10));
			
			NumValue maxY = new NumValue(getParameter(MAX_VALUE_PARAM));
			FormulaContext boldContext = new FormulaContext(Color.black, getStandardBoldFont(), this);
			formula2 = new Percentile2FormulaPanel(data, "y", "ref", "propn", referenceData,
																																		maxY, boldContext);
		thePanel.add(formula2);
			
			XPanel bottomPanel = new InsetPanel(6, 2);
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				formula = new PercentileFormulaPanel(data, "y", "ref", "propn", referenceData,
																						maxY, getParameter(UNITS_PARAM), boldContext);
			bottomPanel.add(formula);
			
			bottomPanel.lockBackground(Color.white);
		thePanel.add(bottomPanel);
			
		thePanel.lockBackground(kFormulaBackground);
		return thePanel;
	}
}