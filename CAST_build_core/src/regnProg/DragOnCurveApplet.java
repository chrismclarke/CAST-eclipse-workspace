package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

import regn.*;
import regnView.*;


public class DragOnCurveApplet extends ScatterApplet {
	static final protected String MODEL_PARAM = "model";
	static final protected String DECIMALS_PARAM = "decimals";
	
//	private SmoothQuadModel modelVariable;
	
	private DragOnCurveView theView;
	
	private XCheckbox finishedSetupCheck;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		SmoothQuadModel modelVariable = new SmoothQuadModel("model", data, "x",
																															getParameter(MODEL_PARAM));
		data.addVariable("model", modelVariable);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		if (labelAxes) {
			XPanel yNamePanel = new XPanel();
			yNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
			
				yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
				yVariateName.setFont(theVertAxis.getFont());
				yNamePanel.add(yVariateName);
			thePanel.add("South", yNamePanel);
		}
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				finishedSetupCheck = new XCheckbox(translate("Finished sketching curve"), this);
			checkPanel.add(finishedSetupCheck);
			
		thePanel.add("Center", checkPanel);
		
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		DragExplanAxis xAxis = ((DragExplanAxis)theHorizAxis);
	
		PredictionAxis yAxis = (PredictionAxis)theVertAxis;
		yAxis.setModel(data, "y", "model", xAxis, decimals);
		
		return thePanel;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		HorizAxis axis = new DragExplanAxis(data.getVariable("x").name, this);
		
		String labelInfo = getParameter(X_AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		if (labelAxes)
			axis.setAxisName(getParameter(X_VAR_NAME_PARAM));
		return axis;
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		PredictionAxis axis = new PredictionAxis(this);
		String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		return axis;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		DragExplanAxis xAxis = ((DragExplanAxis)theHorizAxis);
		theView = new DragOnCurveView(data, this, xAxis, theVertAxis, "x", "y", "model");
		theView.setFinishedSetup(false);
		return theView;
	}

	
	private boolean localAction(Object target) {
		if (target == finishedSetupCheck) {
			theView.setFinishedSetup(finishedSetupCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}