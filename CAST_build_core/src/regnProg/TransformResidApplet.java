package regnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import loess.*;
import regn.*;
import regnView.*;


public class TransformResidApplet extends MultipleScatterApplet {
	static final private String BETA_EXTREMES_PARAM = "betaExtremes";
	
	static final private String kResidAxisInfo = "-4 4 0 10";
	
	private TransformVertAxis theTransformAxis;
	private HorizAxis mainXAxis;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addVariable("dummyResid", new NumVariable(translate("Residual")));		//	only so right residual plot shows y-axis name
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new BorderLayout(0, 0));
			dataPanel.add("Center", createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
					NumVariable yVar = (NumVariable)data.getVariable("y");
					XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
					yVariateName.setFont(theTransformAxis.getFont());
				topPanel.add(yVariateName);
			dataPanel.add("North", topPanel);
			
		thePanel.add("Left", dataPanel);
		thePanel.add("Right", createPlotPanel(data, false, "x", "dummyResid", null,
							getParameter(X_AXIS_INFO_PARAM), kResidAxisInfo, 1));
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		if (plotIndex == 0) {
			TransResidView theView = new TransResidView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
			theView.setRetainLastSelection(true);
			return theView;
		}
		else {
			ScaledResidPlotView theView = new ScaledResidPlotView(data, this, theHorizAxis, theVertAxis,
																														theTransformAxis, "x", "y", "model");
			theView.setRetainLastSelection(true);
			return theView;
		}
	}
	
	protected VertAxis createVertAxis(DataSet data, String yAxisInfo, int plotIndex) {
		if (plotIndex == 0) {
			theTransformAxis = new TransformVertAxis(this);
			theTransformAxis.readNumLabels(yAxisInfo);
			theTransformAxis.setLinkedData(data, true);
			theTransformAxis.setAxisName(data.getVariable("y").name);
			PowerLinearModel modelVariable = new PowerLinearModel("model", data, "x", theTransformAxis);
			modelVariable.setLSParams("y", 3, 3, 3, 3, 3, 3);
			data.addVariable("model", modelVariable);		//	transformed model cannot be created until
																		// axis has been created
			return theTransformAxis;
		}
		else
			return super.createVertAxis(data, yAxisInfo, plotIndex);
	}
	
	protected HorizAxis createHorizAxis(DataSet data, String xAxisInfo, int plotIndex) {
		if (plotIndex == 0) {
			mainXAxis = super.createHorizAxis(data, xAxisInfo, plotIndex);
			return mainXAxis;
		}
		else
			return super.createHorizAxis(data, xAxisInfo, plotIndex);
	}
	
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		StringTokenizer st = new StringTokenizer(getParameter(BETA_EXTREMES_PARAM));
		NumValue bigIntercept = new NumValue(st.nextToken());
		NumValue bigSlope = new NumValue(st.nextToken());
		
		LinearPowerEquationView theEqn = new LinearPowerEquationView(data, this,
							"model", data.getVariable("y").name, data.getVariable("x").name, 
							bigIntercept, bigIntercept, bigSlope, bigSlope, mainXAxis, theTransformAxis);
		thePanel.add(theEqn);
		
		return thePanel;
	}
}