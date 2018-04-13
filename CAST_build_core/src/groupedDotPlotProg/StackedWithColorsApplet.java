package groupedDotPlotProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import valueList.*;
import utils.*;

import dotPlot.*;


public class StackedWithColorsApplet extends XApplet {
	static final private String CHANGE_VALUE_PARAM = "changeValue";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	private DataSet data;
	
	private double originalValue, newValue;
	
	private XCheckbox changeLastValueCheck;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 5));
		add("Center", dotPlotPanel(data));
		add("North", valuePanel(data));
		add("South", controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addLabelVariable("label", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		
		return data;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			NumVariable yVar = (NumVariable)data.getVariable("y");
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			theHorizAxis.setAxisName(yVar.name);
			
		thePanel.add("Bottom", theHorizAxis);
		
			StackedWithColorsView theDotPlot = new StackedWithColorsView(data, this, theHorizAxis, 0, yVar.noOfValues() - 1);
			if (yVar.noOfValues() < 20)
				theDotPlot.setCrossSize(DataView.LARGE_CROSS);
			theDotPlot.lockBackground(Color.white);
			
		thePanel.add("Center", theDotPlot);
		
		return thePanel;
	}
	
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		thePanel.add(new OneValueView(data, "label", this));
		thePanel.add(new OneValueView(data, "y", this));
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		String changeValueString = getParameter(CHANGE_VALUE_PARAM);
		if (changeValueString != null) {
			StringTokenizer st = new StringTokenizer(changeValueString, "#");
			originalValue = Double.parseDouble(st.nextToken());
			newValue = Double.parseDouble(st.nextToken());
			changeLastValueCheck = new XCheckbox(st.nextToken(), this);
			thePanel.add(changeLastValueCheck);
		}
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == changeLastValueCheck) {
			NumVariable yVar = (NumVariable)data.getVariable("y");
			NumValue lastValue = (NumValue)yVar.valueAt(yVar.noOfValues() - 1);
			lastValue.setValue(changeLastValueCheck.getState() ? newValue : originalValue);
			
			data.variableChanged("y");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}