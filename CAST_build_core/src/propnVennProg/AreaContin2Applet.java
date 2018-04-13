package propnVennProg;

import java.awt.*;

import axis.*;
import dataView.*;

import propnVenn.*;
import contin.*;


public class AreaContin2Applet extends XApplet {
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	
	static final protected String Y_LABELS_PARAM = "yLabels";
	static final protected String X_LABELS_PARAM = "xLabels";
	
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String Y_VALUES_PARAM = "yValues";
	
	static final protected String X_MARGIN_PARAM = "xMargin";
	static final protected String Y_CONDIT_PARAM = "yCondit";
	
	static final private String kProbAxisInfo = "0.0 1.0 0.0 0.2";
	
	protected DataSet data;
	protected AreaContinCoreView theView;
	
	protected MarginConditPanel yAxisLabel, xAxisLabel;
	
	public void setupApplet() {
		ContinImages.loadLabels(this);
		
		data = readData();
		
		setLayout(new BorderLayout(0, 30));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String xValueString = getParameter(X_VALUES_PARAM);
		if (xValueString == null) {
			CatDistnVariable xVariable = new CatDistnVariable(getParameter(X_VAR_NAME_PARAM));
			String xLabelString = getParameter(X_LABELS_PARAM);
			xVariable.readLabels(xLabelString);
			xVariable.setParams(getParameter(X_MARGIN_PARAM));
			data.addVariable("x", xVariable);
			
			ContinResponseVariable yVariable = new ContinResponseVariable(getParameter(Y_VAR_NAME_PARAM), data, "x");
			String yLabelString = getParameter(Y_LABELS_PARAM);
			yVariable.readLabels(yLabelString);
			yVariable.setProbs(getParameter(Y_CONDIT_PARAM), ContinResponseVariable.CONDITIONAL);
			data.addVariable("y", yVariable);
		}
		else {
			data.addCatVariable("x", getParameter(X_VAR_NAME_PARAM), xValueString,
															getParameter(X_LABELS_PARAM));
			data.addCatVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM),
															getParameter(Y_LABELS_PARAM));
		}
		
		return data;
	}
	
	protected MarginConditPanel yAxisLabelPanel(DataSet data, boolean marginNotCondit) {
		CoreVariable yVar = data.getVariable("y");
		boolean isFinite = (yVar instanceof CatVariable);
		
		yAxisLabel = new MarginConditPanel(MarginConditPanel.VERT_AXIS, yVar.name, marginNotCondit, isFinite, this);
		return yAxisLabel;
	}
	
	protected MarginConditPanel xAxisLabelPanel(DataSet data, boolean marginNotCondit) {
		CoreVariable xVar = data.getVariable("x");
		boolean isFinite = (xVar instanceof CatVariable);
		
		xAxisLabel = new MarginConditPanel(MarginConditPanel.HORIZ_AXIS, xVar.name, marginNotCondit, isFinite, this);
		return xAxisLabel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		return propnVennPanel(data, true);
	}
	
	protected XPanel propnVennPanel(DataSet data, boolean yMarginal) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			yAxisLabel = yAxisLabelPanel(data, yMarginal ? MarginConditPanel.MARGINAL : MarginConditPanel.CONDITIONAL);
		if (yAxisLabel != null)
			thePanel.add("West", yAxisLabel);
			
			xAxisLabel = xAxisLabelPanel(data, yMarginal ? MarginConditPanel.CONDITIONAL : MarginConditPanel.MARGINAL);
		if (xAxisLabel != null)
			thePanel.add("South", xAxisLabel);
		
		thePanel.add("Center", tablePanel(data, yMarginal));
		
		return thePanel;
	}
	
	protected AreaContinCoreView getPropnVenn(DataSet data, VertAxis vertAxis,
												HorizAxis horizAxis, boolean yMarginal) {
		return new AreaContin2View(data, this, vertAxis, horizAxis,
						"y", "x", AreaContinCoreView.CAN_SELECT, AreaContinCoreView.Y_MARGIN);
	}
	
	protected void setupAxis(NumCatAxis theAxis) {
		theAxis.readNumLabels(kProbAxisInfo);
	}
	
	private XPanel tablePanel(DataSet data, boolean yMarginal) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		setupAxis(theHorizAxis);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theVertAxis = new VertAxis(this);
		setupAxis(theVertAxis);
		thePanel.add("Left", theVertAxis);
		
		AreaContinCoreView localView = getPropnVenn(data, theVertAxis, theHorizAxis, yMarginal);
		theView = localView;
		thePanel.add("Center", localView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		JointProbChoice theChoice = new JointProbChoice(data, AreaContinCoreView.Y_MARGIN,this,
																											(AreaContin2View)theView, "y", "x");
		
		XPanel thePanel = new PickMarginPanel(xAxisLabel, yAxisLabel, theView, theChoice);
		return thePanel;
	}
}