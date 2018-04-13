package residProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import models.*;

import regnProg.*;
import resid.*;


public class DeleteInfluenceApplet extends MultipleScatterApplet {
	static final private String Y2_VALUES_PARAM = "y2Values";
	
	private LabelValue kHighLeverage, kHighInfluence, kLowInfluence;
	
	private XCheckbox deleteCheck;
	private double actualY1, actualY2;
	
	public void setupApplet() {
		kHighLeverage = new LabelValue(translate("high leverage"));
		kHighInfluence = new LabelValue(translate("high influence"));
		kLowInfluence = new LabelValue(translate("low influence"));
		super.setupApplet();
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			NumVariable y1Var = (NumVariable)data.getVariable("y");
			actualY1 = y1Var.doubleValueAt(y1Var.noOfValues() - 1);
		
			LinearModel lsLine = new LinearModel("LS line", data, "x");
			lsLine.updateLSParams("y");
		data.addVariable("lsLine", lsLine);
		
			NumVariable y2Var = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
			y2Var.readValues(getParameter(Y2_VALUES_PARAM));
			actualY2 = y2Var.doubleValueAt(y2Var.noOfValues() - 1);
		data.addVariable("y2", y2Var);
		
			LinearModel lsLine2 = new LinearModel("LS line 2", data, "x");
			lsLine2.updateLSParams("y2");
		data.addVariable("lsLine2", lsLine2);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
			thePanel.setLayout(new ProportionLayout(0.5, 5));
				
			thePanel.add(ProportionLayout.LEFT, createPlotPanel(data, false, "x", "y", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
			
			thePanel.add(ProportionLayout.RIGHT, createPlotPanel(data, false, "x", "y2", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 1));
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis xAxis, VertAxis yAxis,
																									int plotIndex) {
		String yKey = (plotIndex == 0) ? "y" : "y2";
		String lsKey = (plotIndex == 0) ? "lsLine" : "lsLine2";
		LabelValue influenceLabel = (plotIndex == 0) ? kLowInfluence : kHighInfluence;
		NumVariable xVar = (NumVariable)data.getVariable("x");
		return new LSLabelScatterView(data, this, xAxis, yAxis, "x", yKey, lsKey,
																xVar.noOfValues() - 1, kHighLeverage, influenceLabel);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			deleteCheck = new XCheckbox(translate("Delete high leverage points"), this);
		thePanel.add(deleteCheck);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == deleteCheck) {
			NumVariable y1Var = (NumVariable)data.getVariable("y");
			NumValue lastY1 = (NumValue)y1Var.valueAt(y1Var.noOfValues() - 1);
			lastY1.setValue(deleteCheck.getState() ? Double.NaN : actualY1);
			LinearModel lsLine1 = (LinearModel)data.getVariable("lsLine");
			lsLine1.updateLSParams("y");
			
			NumVariable y2Var = (NumVariable)data.getVariable("y2");
			NumValue lastY2 = (NumValue)y2Var.valueAt(y1Var.noOfValues() - 1);
			lastY2.setValue(deleteCheck.getState() ? Double.NaN : actualY2);
			LinearModel lsLine2 = (LinearModel)data.getVariable("lsLine2");
			lsLine2.updateLSParams("y2");
			
			data.variableChanged("x");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}