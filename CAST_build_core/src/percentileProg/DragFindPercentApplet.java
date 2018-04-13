package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import formula.*;


import percentile.*;


public class DragFindPercentApplet extends XApplet {
	static final private String FIXED_VALUE_PARAM = "fixedValue";
	static final protected String MAX_VALUE_PARAM = "maxValue";
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final protected String LONG_VAR_NAME_PARAM = "longVarName";
	
	protected DataSet data;
	protected DataSet refData;
	
	protected SimplePropnFormulaPanel formula;
	protected CumDotBoxPlotView cumView;
	protected HorizAxis horizAxis;
	
	private XChoice inequalityChoice;
	private int currentInequality = 0;
	
	public void setupApplet() {
		data = getData();
		refData = getReferenceData(data);
		
		setLayout(new BorderLayout(0, 0));
		add("North", topPanel(data));
		add("Center", dataDisplayPanel(data, refData));
		add("South", propnCalcPanel(data, refData));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected DataSet getReferenceData(DataSet data) {
		DataSet referenceData = new DataSet();
		referenceData.addNumVariable("ref", "Reference", getParameter(FIXED_VALUE_PARAM));
		referenceData.setSelection(0);
		return referenceData;
	}
	
	protected XPanel propnSidePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			XLabel label = new XLabel(translate("Proportion of") + " ", XLabel.LEFT, this);
			label.setFont(getStandardBoldFont());
		thePanel.add(label);
		
			inequalityChoice = new XChoice(this);
			inequalityChoice.addItem(translate("smaller values"));
			inequalityChoice.addItem(translate("larger values"));
		thePanel.add(inequalityChoice);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		return propnSidePanel(data);
	}
	
	protected XPanel dataDisplayPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			String longVarName = getParameter(LONG_VAR_NAME_PARAM);
			horizAxis.setAxisName((longVarName == null) ? data.getVariable("y").name : longVarName);
		thePanel.add("Bottom", horizAxis);
		
			cumView = new CumDotBoxPlotView(data, this, horizAxis, referenceData, "ref", PropnRangeView.LESS_EQUAL);
		thePanel.add("Center", cumView);
		return thePanel;
	}
	
	protected XPanel propnCalcPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			NumValue maxY = new NumValue(getParameter(MAX_VALUE_PARAM));
			FormulaContext boldContext = new FormulaContext(Color.black, getStandardBoldFont(), this);
			formula = new PropnFormulaPanel(data, "y", "ref", referenceData, maxY,
																					PropnRangeView.LESS_EQUAL, boldContext);
		thePanel.add(formula);
		return thePanel;
	}
	
	protected void changeInequality(int inequality) {
		formula.setInequality(inequality);
		cumView.changeInequality(inequality);
	}
	
	
	private boolean localAction(Object target) {
		if (target == inequalityChoice) {
			int newChoice = inequalityChoice.getSelectedIndex();
			if (newChoice != currentInequality) {
				currentInequality = newChoice;
				int inequality = newChoice == 0 ? PropnRangeView.LESS_EQUAL
																											: PropnRangeView.GREATER_THAN;
				changeInequality(inequality);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}