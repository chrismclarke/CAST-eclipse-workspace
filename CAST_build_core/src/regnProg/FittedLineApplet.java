package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import models.*;
import coreGraphics.*;

import regn.*;
import regnView.*;


public class FittedLineApplet extends ScatterApplet {
	static final protected String MODEL_PARAM = "model";
	static final protected String DECIMALS_PARAM = "decimals";
	
	private FittedRangeView theView;
	private LinearModel modelVariable;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		modelVariable = new LinearModel("model", data, "x", getParameter(MODEL_PARAM));
		data.addVariable("model", modelVariable);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		DragExplanAxis xAxis = ((DragExplanAxis)theHorizAxis);
		PredictionEView prediction = new PredictionEView(data, this, "x", "y", "model", xAxis,
																																			theVertAxis, decimals);
		
		prediction.setFont(theVertAxis.getFont());
		prediction.setForeground(Color.blue);
		thePanel.add(prediction);
		
		theView.setLinkedPrediction(prediction, null);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
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
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		DragExplanAxis xAxis = ((DragExplanAxis)theHorizAxis);
		theView = new FittedRangeView(data, this, xAxis,theVertAxis, "x", "y", "model");
		return theView;
	}
}