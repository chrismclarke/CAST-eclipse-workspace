package loessProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;
import models.*;

import regnProg.*;
import loess.*;


public class FitResid2Applet extends MultipleScatterApplet {
	static final public String DATA_NAME_PARAM = "dataName";
	static final private String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String FIT_RESID_DECIMALS_PARAM = "decimals";
	
	private String xValues[];
	private String yValues[];
	
	private XChoice dataSetChoice;
	private int currentDataSet = 0;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout());
		add("North", choicePanel());
		add("Center", dataPanel(data));
	}
	
	protected XPanel displayPanel(DataSet data) {return null;}		//	not used
	protected XPanel controlPanel(DataSet data) {return null;}		//	not used
	
	private void countDataSets() {
		int n = 0;
		while (getParameter(Y_VALUES_PARAM + n) != null)
			n++;
		
		xValues = new String[n];
		yValues = new String[n];
		for (int i=0 ; i<n ; i++) {
			xValues[i] = getParameter(X_VALUES_PARAM + i);
			if (xValues[i] == null)
				xValues[i] = xValues[0];
			yValues[i] = getParameter(Y_VALUES_PARAM + i);
		}
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		countDataSets();
		
			NumVariable y0 = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
			y0.readValues(yValues[0]);
		data.addVariable("y", y0);
		
			NumVariable x0 = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			x0.readValues(xValues[0]);
		data.addVariable("x", x0);
		
			LinearModel modelVariable = new LinearModel("model", data, "x");
			modelVariable.setLSParams("y", 10, 10, 0);
		data.addVariable("model", modelVariable);
		
			int fitResidDecimals = Integer.parseInt(getParameter(FIT_RESID_DECIMALS_PARAM));
		data.addVariable("resid", new ResidValueVariable(translate("Residual"), data, "x", "y", "model",
																							fitResidDecimals));
		data.addVariable("fit", new FittedValueVariable(translate("Fitted y"), data, "x", "model",
																							fitResidDecimals));
		return data;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XPanel graphPanel = new XPanel();
			graphPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
			
			graphPanel.add("Left", createPlotPanel(data, false, "x", "y", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
			graphPanel.add("Right", createPlotPanel(data, false, "x", "resid", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		
		thePanel.add("Center", graphPanel);
		thePanel.add("South", valuePanel(data));
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		if (plotIndex == 0)
			return new HiliteResidualView(data, this, theHorizAxis, theVertAxis, "x", "y", "model", null);
		else
			return new HiliteResidualView(data, this, theHorizAxis, theVertAxis, "x", "resid", null, null);
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			OneValueView yView = new OneValueView(data, "y", this);
			yView.setForeground(Color.blue);
			yView.setLabel(translate("Actual y"));
		thePanel.add(yView);
		
			OneValueView fitView = new OneValueView(data, "fit", this);
			fitView.setForeground(HiliteResidualView.darkGreen);
		thePanel.add(fitView);
		
			OneValueView residView = new OneValueView(data, "resid", this);
			residView.setForeground(Color.red);
		thePanel.add(residView);
		
		return thePanel;
	}
	
	private XPanel choicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			dataSetChoice = new XChoice(this);
			for (int i=0 ; i<yValues.length ; i++)
				dataSetChoice.addItem(getParameter(DATA_NAME_PARAM + i));
		thePanel.add(dataSetChoice);
		
		return thePanel;
	}
	
	private void changeDataSet(DataSet data, int newChoice) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.readValues(yValues[newChoice]);
		NumVariable xVar = (NumVariable)data.getVariable("x");
		xVar.readValues(xValues[newChoice]);
		
		LinearModel modelVariable =(LinearModel)data.getVariable("model");
		modelVariable.setLSParams("y", 10, 10, 0);
		
		data.variableChanged("y");
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (currentDataSet != newChoice) {
				currentDataSet = newChoice;
				changeDataSet(data, newChoice);
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