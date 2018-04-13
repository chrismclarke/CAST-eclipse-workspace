package controlProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.OneValueView;


public class FixedChartApplet extends ControlChartApplet {
	static final private String DATA_SETS_PARAM = "dataSets";
	
	protected XChoice dataSetChoice;
	private Vector dataValues = new Vector();
	protected XTextArea dataDescriptions = null;
	
	protected void doDataSelection() {
//		data.clearSelection();
	}
	
	protected XPanel createProblemView(DataSet data) {
		XPanel thePanel = super.createProblemView(data);
		theView.setFrame(data.getNumVariable().noOfValues());
		doDataSelection();
		return thePanel;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 3));
		
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
		
		OneValueView valueView = new OneValueView(data, "y", this);
		valuePanel.add(valueView);
		controlPanel.add(valuePanel);
		
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		buttonPanel.add(dataSetChoice);
		
		controlPanel.add(buttonPanel);
		
		return controlPanel;
	}
	
	protected int noOfValuesToDisplay() {		//		this must only be called once
		dataSetChoice = new XChoice(this);
		String dataNames = getParameter(DATA_SETS_PARAM);
		LabelEnumeration names = new LabelEnumeration(dataNames);
		int maxValues = 0;
		while (names.hasMoreElements()) {
			String choiceLabel = (String)names.nextElement();
			if (!names.hasMoreElements())
				break;
			String dataName = (String)names.nextElement();
			dataSetChoice.addItem(choiceLabel);
			String valueString = getParameter(dataName);
			dataValues.addElement(valueString);
			StringTokenizer valueTokens = new StringTokenizer(valueString);
			maxValues = Math.max(maxValues, valueTokens.countTokens());
		}
		
		return maxValues;
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int dataIndex = dataSetChoice.getSelectedIndex();
			String dataVals = (String)dataValues.elementAt(dataIndex);
			if (dataVals != null)
				synchronized (data) {
					NumVariable variable = data.getNumVariable();
					variable.readValues(dataVals);
					data.variableChanged("y");
					theView.setFrame(data.getNumVariable().noOfValues());
					doDataSelection();
				}
			if (dataDescriptions != null)
				dataDescriptions.setText(dataIndex);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}