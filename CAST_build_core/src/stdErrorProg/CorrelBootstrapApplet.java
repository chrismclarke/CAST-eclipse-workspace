package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreSummaries.*;
import coreVariables.*;

import stdError.*;


public class CorrelBootstrapApplet extends ErrorSampleApplet {
	static final protected String X_AXIS_INFO_PARAM = "xAxis";
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String Y_VALUES_PARAM = "yValues";
	
	protected double topProportion() {
		return 0.7;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		NumVariable xPopn = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		xPopn.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("xPopn", xPopn);
		
		BootstrapNumVariable xBootstrap = new BootstrapNumVariable(xPopn.name);
		xBootstrap.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xBootstrap);
		
		NumVariable yPopn = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
		yPopn.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("yPopn", yPopn);
		
		Bootstrap2NumVariable yBootstrap = new Bootstrap2NumVariable(yPopn.name, xBootstrap);
		yBootstrap.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yBootstrap);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			target = getTarget(data, 0.0, decimals);
			
			CorrVariable estimator = new CorrVariable("Estimate", "y", "x", decimals);
			
		summaryData.addVariable("est", estimator);
		
			ScaledVariable error = new ScaledVariable(getParameter(ERROR_NAME_PARAM), estimator,
																									"est", -target.toDouble(), 1.0, decimals);
		
		summaryData.addVariable("error", error);
		
		return summaryData;
	}
	
	protected NumValue getTarget(DataSet data, double unused, int decimals) {
		NumVariable xVar = (NumVariable)data.getVariable("xPopn");
		NumVariable yVar = (NumVariable)data.getVariable("yPopn");
		
		double corr = CorrVariable.correlation(xVar, yVar);
		
		return new NumValue(corr, decimals);
	}
	
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis theHorizAxis = new HorizAxis(this);
				theHorizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
				theHorizAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
			mainPanel.add("Bottom", theHorizAxis);
			
				VertAxis theVertAxis = new VertAxis(this);
				theVertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
			mainPanel.add("Left", theVertAxis);
			
				DataView theView = new BootstrapScatterView(data, this, theHorizAxis, theVertAxis,
																				"xPopn", "yPopn", "y", summaryData, "est", target);
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
			mainPanel.add("Center", theView);
		
		thePanel.add("Center", mainPanel);
		
			XLabel yVarLabel = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yVarLabel.setFont(theVertAxis.getFont());
		thePanel.add("North", yVarLabel);
		
		return thePanel;
	}

}