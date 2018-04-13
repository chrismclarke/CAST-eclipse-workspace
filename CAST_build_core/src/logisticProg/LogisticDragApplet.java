package logisticProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import regn.*;
import logistic.*;


public class LogisticDragApplet extends LogisticLineApplet {
	private XButton bestButton;
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		DragLogisticView theView = new DragLogisticView(data, this, theVertAxis, theHorizAxis, "x", "y", "model");
		return theView;
	}
	
	protected LinearEquationView createEquationView(DataSet data) {
		return new LogisticEquationView(data, this, "model", successName,
												getParameter(Y_VAR_NAME_PARAM), logistic.intMin, logistic.intMax,
												logistic.slopeMin, logistic.slopeMax, null, null);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
		XPanel equationPanel = new XPanel();
		equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		equationPanel.add(createEquationView(data));
		
		thePanel.add("Center", equationPanel);
		
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		bestButton = new XButton(translate("Best fit"), this);
		buttonPanel.add(bestButton);
		
		thePanel.add("South", buttonPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == bestButton) {
			NumVariable y = (NumVariable)data.getVariable("y");
			CatVariable x = (CatVariable)data.getVariable("x");
			int jointCounts[][] = x.getCounts(y);
			int noOfYValues = jointCounts[0].length;
			double horizValue[] = new double[noOfYValues];
			ValueEnumeration e = y.values();
			for (int i=0 ; i<noOfYValues ; i++)
				horizValue[i] = ((NumValue)e.nextGroup().val).toDouble();
			
			((LogisticModel)logisticVariable).setMLParams(jointCounts, horizValue);
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}