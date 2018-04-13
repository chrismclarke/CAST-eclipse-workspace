package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreGraphics.*;

import regn.*;
import regnView.*;


public class FittedSpreadApplet extends ScatterApplet {
	static final protected String MODEL_PARAM = "model";
	static final protected String DECIMALS_PARAM = "decimals";
	static final protected String DATA_NAME_PARAM = "dataNames";
	
	private FittedRangeView theView;
	private QuadraticModel modelVariable;
	private PlusMinusView predictionSD;
	private XCheckbox errorsCheck;
	private XChoice dataChoice;
	private int selectedData = 0;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		modelVariable = new QuadraticModel("model", data, "x", getParameter(MODEL_PARAM));
		data.addVariable("model", modelVariable);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
		
		if (labelAxes) {
			yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yVariateName.setFont(theVertAxis.getFont());
			thePanel.add(yVariateName);
		}
		
		int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		DragExplanAxis xAxis = ((DragExplanAxis)theHorizAxis);

		predictionSD = new PlusMinusView(data, this, "y", "model", xAxis, theVertAxis, decimals);
		predictionSD.setFont(theVertAxis.getFont());
		predictionSD.show(false);
		thePanel.add(predictionSD);
		
		theView.setLinkedPrediction(null, predictionSD);
		PredictionAxis yAxis = (PredictionAxis)theVertAxis;
		yAxis.setModel(data, "y", "model", xAxis, decimals);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		errorsCheck = new XCheckbox(translate("Likely errors"), this);
		thePanel.add(errorsCheck);
		
		dataChoice = new XChoice(this);
		LabelEnumeration e = new LabelEnumeration(getParameter(DATA_NAME_PARAM));
		while (e.hasMoreElements())
			dataChoice.addItem((String)e.nextElement());
		dataChoice.select(0);
		thePanel.add(dataChoice);
		
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
		theView = new FittedRangeView(data, this, xAxis, theVertAxis, "x", "y", "model");
		return theView;
	}

	
	private boolean localAction(Object target) {
		if (target == errorsCheck) {
			theView.setErrorDisplay(errorsCheck.getState());
			theView.repaint();
			
			predictionSD.show(errorsCheck.getState());
			return true;
		}
		else if (target == dataChoice) {
			if (dataChoice.getSelectedIndex() != selectedData) {
				selectedData = dataChoice.getSelectedIndex();
				String xDataValues = X_VALUES_PARAM;
				String yDataValues = Y_VALUES_PARAM;
				String modelParams = MODEL_PARAM;
				if (selectedData > 0) {
					xDataValues += (selectedData + 1);
					yDataValues += (selectedData + 1);
					modelParams += (selectedData + 1);
				}
				NumVariable x = (NumVariable)data.getVariable("x");
				NumVariable y = (NumVariable)data.getVariable("y");
				synchronized (data) {
					x.readValues(getParameter(xDataValues));
					y.readValues(getParameter(yDataValues));
					modelVariable.setParameters(getParameter(modelParams));
					data.variableChanged("x");
					data.variableChanged("y");
					data.variableChanged("model");
					theVertAxis.repaint();
					theView.repaint();				//		repaint() should not be needed, but seems
															//		to be (sometimes) on the Mac at least.
				}
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