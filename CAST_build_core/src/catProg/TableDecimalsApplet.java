package catProg;

import java.awt.*;

import dataView.*;
import utils.*;
import cat.*;


public class TableDecimalsApplet extends XApplet {
	static final private String PROPN_DECIMALS_PARAM = "propnDecimals";
	
	static final private String[] kDisplayKeys = {"displayY", "displayPropn"};
	static final private int[] kMinShift = {0, 0};
	static final private int[] kMaxShift = {4, 4};
	
	private DataSet data;
	private TableValuesView theTable;
	private XCheckbox percentageCheck;
	
	private boolean showPropn;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("West", tablePanel(data));
		add("Center", controlPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			LabelVariable labelVar = new LabelVariable(getParameter(LABEL_NAME_PARAM));
			labelVar.readValues(getParameter(LABELS_PARAM));
			labelVar.addValue(new LabelValue("Total"));
		data.addVariable("label", labelVar);
		
			NumVariable baseY = new NumVariable("Raw Y");
			baseY.readValues(getParameter(VALUES_PARAM));
			double total = 0.0;
			ValueEnumeration ye = baseY.values();
			while (ye.hasMoreValues())
				total += ye.nextDouble();
			baseY.addValue(new NumValue(total, baseY.getMaxDecimals()));
		data.addVariable("baseY", baseY);
		
			ShiftedVariable displayY = new ShiftedVariable(getParameter(VAR_NAME_PARAM), baseY,
																																				"baseY", this);
		data.addVariable("displayY", displayY);
		
		String propnDecimalsString = getParameter(PROPN_DECIMALS_PARAM);
		showPropn = propnDecimalsString != null;
		if (showPropn) {
				NumVariable basePropn = new NumVariable("Raw P");
				basePropn.readValues(getParameter(VALUES_PARAM));
				
				ye = basePropn.values();
				while (ye.hasMoreValues()) {
					NumValue yVal = (NumValue)ye.nextValue();
					yVal.setValue(yVal.toDouble() / total);
				}
					
				int maxPropnDecimals = Integer.parseInt(propnDecimalsString);
				basePropn.setDecimals(maxPropnDecimals);
				basePropn.addValue(new NumValue(1.0, maxPropnDecimals));
			data.addVariable("basePropn", basePropn);
			
				ShiftedVariable displayPropn = new ShiftedVariable(translate("Proportion"), basePropn,
																																					"basePropn", this);
			data.addVariable("displayPropn", displayPropn);
		}
		
		return data;
	}

	private XPanel tablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		if (showPropn)
			theTable = new TableValuesView(data, this, "label", kDisplayKeys, kMinShift, kMaxShift);
		else
			theTable = new TableValuesView(data, this, "label", kDisplayKeys[0], kMinShift[0], kMaxShift[0]);
		theTable.setFont(getBigFont());
		thePanel.add(theTable);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		if (showPropn) {
			percentageCheck = new XCheckbox(translate("Percentage"), this);
			thePanel.add(percentageCheck);
		}
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == percentageCheck) {
			ShiftedVariable displayPropn = (ShiftedVariable)data.getVariable("displayPropn");
			displayPropn.setShowPercentage(percentageCheck.getState());
			displayPropn.name = percentageCheck.getState() ? translate("Percentage") : translate("Proportion");
			data.variableChanged("displayPropn");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
}