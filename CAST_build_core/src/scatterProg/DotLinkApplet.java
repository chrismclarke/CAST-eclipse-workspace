package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import valueList.OneValueView;
import utils.*;
import coreGraphics.*;


public class DotLinkApplet extends XApplet {
	static final private String X_AXIS_INFO_PARAM = "xAxis";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String PLOT_TYPE_PARAM = "plotType";
	
	private boolean hasLabels;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		thePanel.add("Top", dotPlotPanel(data, getParameter(X_AXIS_INFO_PARAM), "x"));
		thePanel.add("Bottom", dotPlotPanel(data, getParameter(Y_AXIS_INFO_PARAM), "y"));
		return thePanel;
	}
	
	protected XPanel dotPlotPanel(DataSet data, String axisInfoParam, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(axisInfoParam);
		CoreVariable v = data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		thePanel.add("Bottom", theHorizAxis);
		
		String plotTypeString = getParameter(PLOT_TYPE_PARAM);
		DotPlotView theView;
		if (plotTypeString != null && plotTypeString.equals("stacked"))
			theView = new StackedDotPlotView(data, this, theHorizAxis);
		else
			theView = new DotPlotView(data, this, theHorizAxis, 1.0);
		theView.setRetainLastSelection(true);
		theView.setActiveNumVariable(variableKey);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		hasLabels = labelVarName != null;
		if (hasLabels)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 4));
		
			if (hasLabels) {
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				topPanel.add(new OneValueView(data, "label", this));
				thePanel.add(topPanel);
			}
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
			
				OneValueView xValue = new OneValueView(data, "x", this);
				xValue.addEqualsSign();
			bottomPanel.add(xValue);
				
				OneValueView yValue = new OneValueView(data, "y", this);
				yValue.addEqualsSign();
			bottomPanel.add(yValue);
			
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
}