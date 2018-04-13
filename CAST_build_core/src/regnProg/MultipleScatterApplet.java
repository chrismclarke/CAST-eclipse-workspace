package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import valueList.OneValueView;
import utils.*;

import loess.*;


abstract public class MultipleScatterApplet extends XApplet {
	static final protected String X_AXIS_INFO_PARAM = "horizAxis";
	static final protected String Y_AXIS_INFO_PARAM = "vertAxis";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String Y_VALUES_PARAM = "yValues";
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	abstract protected XPanel displayPanel(DataSet data);
	abstract protected XPanel controlPanel(DataSet data);
	abstract protected DataView createDataView(DataSet data, HorizAxis horizAxis, VertAxis vertAxis,
																													int plotIndex);
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		return null;
	}
	
	protected VertAxis createVertAxis(DataSet data, String yAxisInfo, int plotIndex) {
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(yAxisInfo);
		return vertAxis;
	}
	
	protected HorizAxis createHorizAxis(DataSet data, String xAxisInfo, int plotIndex) {
		HorizAxis horizAxis = new HorizAxis(this);
		horizAxis.readNumLabels(xAxisInfo);
		return horizAxis;
	}
	
	protected XPanel createPlotPanel(DataSet data, boolean axisLabelValues, String xKey,
										String yKey, String labelKey, String xAxisInfo, String yAxisInfo,
										int plotIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
			HorizAxis horizAxis = createHorizAxis(data, xAxisInfo, plotIndex);
			NumVariable xVar = (NumVariable)data.getVariable(xKey);
			if (axisLabelValues) {
				XPanel axisLabelPanel = new XPanel();
				axisLabelPanel.setLayout(new BorderLayout());
				
				OneValueView xView = new OneValueView(data, xKey, this);
				xView.setFont(horizAxis.getFont());
				axisLabelPanel.add("East", xView);
				if (labelKey != null) {
					OneValueView labelView = new OneValueView(data, labelKey, this);
					axisLabelPanel.add("West", labelView);
				}
				
				thePanel.add("South", axisLabelPanel);
			}
			else
				horizAxis.setAxisName(xVar.name);
			plotPanel.add("Bottom", horizAxis);
			
			VertAxis vertAxis = createVertAxis(data, yAxisInfo, plotIndex);
			plotPanel.add("Left", vertAxis);
			
			DataView theView = createDataView(data, horizAxis, vertAxis, plotIndex);
			theView.lockBackground(Color.white);
			plotPanel.add("Center", theView);
		
		thePanel.add("Center", plotPanel);
		
		if (!(vertAxis instanceof TransformVertAxis || vertAxis instanceof DualTransVertAxis)) {
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			NumVariable yVar = (NumVariable)data.getVariable(yKey);
			if (axisLabelValues) {
				OneValueView yView = new OneValueView(data, "y", this);
				yView.setFont(vertAxis.getFont());
				topPanel.add(yView);
			}
			else {
				XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
				yVariateName.setFont(vertAxis.getFont());
				topPanel.add(yVariateName);
			}
			thePanel.add("North", topPanel);
		}
		
		return thePanel;
	}
}