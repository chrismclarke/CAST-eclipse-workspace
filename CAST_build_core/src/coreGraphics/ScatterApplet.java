package coreGraphics;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.OneValueView;


public class ScatterApplet extends XApplet {
	static final protected String X_AXIS_INFO_PARAM = "horizAxis";
	static final protected String Y_AXIS_INFO_PARAM = "vertAxis";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_SEQUENCE_PARAM = "xSequence";
	static final protected String Y_VALUES_PARAM = "yValues";
	static final protected String LABEL_AXES_PARAM = "labelAxes";
	static final private String TRANSFORM_AXES_PARAM = "transformAxes";
	
	protected HorizAxis theHorizAxis;
	protected VertAxis theVertAxis;
	protected DataSet data;
	
	protected XLabel yVariateName = null;
	
	protected boolean labelAxes = false;
	private boolean transformAxes = false;
	
	public void setupApplet() {
		data = readData();
		
		String labelAxesParam = getParameter(LABEL_AXES_PARAM);
		labelAxes = labelAxesParam != null && labelAxesParam.equals("true");
		
		String transformAxesParam = getParameter(TRANSFORM_AXES_PARAM);
		transformAxes = transformAxesParam != null && transformAxesParam.equals("true");
		
		setLayout(new BorderLayout(10, 0));
		add("Center", displayPanel(data));
		XPanel theControlPanel = controlPanel(data);
		if (theControlPanel != null)
			add("South", theControlPanel);
		add("North", topPanel(data));
		XPanel theRightPanel = rightPanel(data);
		if (theRightPanel != null)
			add("East", theRightPanel);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		if (labelAxes) {
			yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
			yVariateName.setFont(theVertAxis.getFont());
			thePanel.add(yVariateName);
		}
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		return null;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theHorizAxis = createHorizAxis(data);
		thePanel.add("Bottom", theHorizAxis);
		
		theVertAxis = createVertAxis(data);
		thePanel.add("Left", theVertAxis);
		
		DataView theView = createDataView(data, theHorizAxis, theVertAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected DataSet readData() {
		return readCoreData();
	}
	
	protected DataSet readCoreData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
		NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
		String xValuesString = getParameter(X_VALUES_PARAM);
		if (xValuesString != null)
			xVar.readValues(xValuesString);
		else
			xVar.readSequence(getParameter(X_SEQUENCE_PARAM));
		data.addVariable("x", xVar);
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		return new ScatterView(data, this, theHorizAxis, theVertAxis, "x", "y");
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		HorizAxis axis;
		if (transformAxes) {
			axis = new TransformHorizAxis(this);
			axis.setLinkedData(data, true);
		}
		else
			axis = new HorizAxis(this);
		
		String labelInfo = getParameter(X_AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		if (labelAxes)
			axis.setAxisName(getParameter(X_VAR_NAME_PARAM));
		return axis;
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		VertAxis axis;
		if (transformAxes) {
			axis = new TransformVertAxis(this);
			axis.setLinkedData(data, true);
		}
		else
			axis = new VertAxis(this);
		String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		if (labelAxes)
			axis.setAxisName(getParameter(Y_VAR_NAME_PARAM));
										//		N.B. We also need to add label to vert axis with
										//		text above plot
		return axis;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		if (getParameter(LABEL_NAME_PARAM) != null)
			thePanel.add(new OneValueView(data, "label", this));
		return thePanel;
	}
}