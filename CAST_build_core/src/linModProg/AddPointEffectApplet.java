package linModProg;

import java.awt.*;
import java.util.*;

import axis.*;
import utils.*;
import dataView.*;
import models.*;
import coreGraphics.*;

import linMod.*;


public class AddPointEffectApplet extends ScatterApplet {
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String REGN_MODEL_PARAM = "regnModel";
	
	private XCheckbox addPtCheck;
	private XValueSlider xSlider;
//	private NumValue maxPrediction;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
		yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
		data.addVariable("model", yDistn);
		
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				addPtCheck = new XCheckbox(translate("Extra point at") + ":", this);
			checkPanel.add(addPtCheck);
		thePanel.add("North", checkPanel);
		
				StringTokenizer theParams = new StringTokenizer(getParameter(X_LIMITS_PARAM));
				NumValue minX = new NumValue(theParams.nextToken());
				NumValue maxX = new NumValue(theParams.nextToken());
				NumValue xStep = new NumValue(theParams.nextToken());
				NumValue startX = new NumValue(theParams.nextToken());
			xSlider = new XValueSlider(minX, maxX, xStep, startX, this);
			xSlider.setForeground(Color.red);
			xSlider.setFont(getStandardBoldFont());
			xSlider.show(false);
		thePanel.add("Center", xSlider);
		
			XPanel spacerPanel = new XPanel();
			spacerPanel.setLayout(new FixedSizeLayout(50, 10));
			spacerPanel.add(new XPanel());
		thePanel.add("West", spacerPanel);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		return new LineBoundsView(data, this, theHorizAxis, theVertAxis, "x", "model");
	}
	
	private boolean localAction(Object target) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		int n = xVar.noOfValues();
		
		if (target == addPtCheck) {
			boolean addPt = addPtCheck.getState();
			xSlider.show(addPt);
			if (addPt) {
				NumValue newX = xSlider.getNumValue();
				xVar.setValueAt(newX, n - 1);
			}
			else {
				NumValue oldX = (NumValue)xVar.valueAt(n - 1);
				oldX.setValue(Double.NaN);
			}
			data.variableChanged("x");
		}
		else if (target == xSlider) {
			NumValue newX = xSlider.getNumValue();
			xVar.setValueAt(newX, n - 1);
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