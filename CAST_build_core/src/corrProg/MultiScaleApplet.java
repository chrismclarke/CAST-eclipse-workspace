package corrProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.OneValueView;
import coreVariables.*;
import coreGraphics.*;

import scatter.*;


public class MultiScaleApplet extends ScatterApplet {
	static final protected String X_AXIS2_INFO_PARAM = "horiz2Axis";
	static final protected String Y_AXIS2_INFO_PARAM = "vert2Axis";
	static final protected String X_AXIS3_INFO_PARAM = "horiz3Axis";
	static final protected String Y_AXIS3_INFO_PARAM = "vert3Axis";
	
	static final protected String X_SCALE_NAMES_PARAM = "xScaleNames";
	static final protected String Y_SCALE_NAMES_PARAM = "yScaleNames";
	
	static final protected String X_TRANSFORM_PARAM = "xTransform";
	static final protected String X2_TRANSFORM_PARAM = "x2Transform";
	static final protected String X3_TRANSFORM_PARAM = "x3Transform";
	static final protected String Y_TRANSFORM_PARAM = "yTransform";
	static final protected String Y2_TRANSFORM_PARAM = "y2Transform";
	static final protected String Y3_TRANSFORM_PARAM = "y3Transform";
	
	private XChoice xScaleChoice, yScaleChoice;
	private MultiHorizAxis localHorizAxis;
	private MultiVertAxis localVertAxis;
	
	private String xTransformString[];
	private String yTransformString[];
	
	private ScaledVariable xScaleVariable, yScaleVariable;
	private int noOfXScales, noOfYScales;
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
		if (getParameter(X3_TRANSFORM_PARAM) != null)
			noOfXScales = 3;
		else if (getParameter(X2_TRANSFORM_PARAM) != null)
			noOfXScales = 2;
		else
			noOfXScales = 1;
		
		xTransformString = new String[noOfXScales];
		xTransformString[0] = getParameter(X_TRANSFORM_PARAM);
		if (noOfXScales > 1)
			xTransformString[1] = getParameter(X2_TRANSFORM_PARAM);
		if (noOfXScales > 2)
			xTransformString[2] = getParameter(X3_TRANSFORM_PARAM);
			
		if (getParameter(Y3_TRANSFORM_PARAM) != null)
			noOfYScales = 3;
		else if (getParameter(Y2_TRANSFORM_PARAM) != null)
			noOfYScales = 2;
		else
			noOfYScales = 1;
		
		yTransformString = new String[noOfYScales];
		yTransformString[0] = getParameter(Y_TRANSFORM_PARAM);
		if (noOfYScales > 1)
			yTransformString[1] = getParameter(Y2_TRANSFORM_PARAM);
		if (noOfYScales > 2)
			yTransformString[2] = getParameter(Y3_TRANSFORM_PARAM);
		
		NumVariable xVariable = (NumVariable)data.getVariable("x");
		xScaleVariable = new ScaledVariable("", xVariable, "x", xTransformString[0]);
		data.addVariable("x2", xScaleVariable);
		
		NumVariable yVariable = (NumVariable)data.getVariable("y");
		yScaleVariable = new ScaledVariable("", yVariable, "y", yTransformString[0]);
		data.addVariable("y2", yScaleVariable);
		return data;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		localHorizAxis = new MultiHorizAxis(this, noOfXScales);
		
		String labelInfo = getParameter(X_AXIS_INFO_PARAM);
		localHorizAxis.readNumLabels(labelInfo);
		if (noOfXScales > 1) {
			labelInfo = getParameter(X_AXIS2_INFO_PARAM);
			localHorizAxis.readExtraNumLabels(labelInfo);
		}
		if (noOfXScales > 2) {
			labelInfo = getParameter(X_AXIS3_INFO_PARAM);
			localHorizAxis.readExtraNumLabels(labelInfo);
		}
		if (labelAxes)
			localHorizAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
		return localHorizAxis;
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		localVertAxis = new MultiVertAxis(this, noOfYScales);
		String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
		localVertAxis.readNumLabels(labelInfo);
		if (noOfYScales > 1) {
			labelInfo = getParameter(Y_AXIS2_INFO_PARAM);
			localVertAxis.readExtraNumLabels(labelInfo);
		}
		if (noOfYScales > 2) {
			labelInfo = getParameter(Y_AXIS3_INFO_PARAM);
			localVertAxis.readExtraNumLabels(labelInfo);
		}
		return localVertAxis;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		ScatterArrowView theView = new ScatterArrowView(data, this, theHorizAxis, theVertAxis, "x", "y");
//		DataView theView = super.createDataView(data, theHorizAxis, theVertAxis);
		theView.setRetainLastSelection(true);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 3));
		
		yScaleChoice = createChoice(getParameter(Y_SCALE_NAMES_PARAM));
		XPanel choicePanel = createChoicePanel(data, "y", yScaleChoice);
		controlPanel.add(choicePanel);
		
		xScaleChoice = createChoice(getParameter(X_SCALE_NAMES_PARAM));
		choicePanel = createChoicePanel(data, "x", xScaleChoice);
		controlPanel.add(choicePanel);
		
		return controlPanel;
	}
	
	private XChoice createChoice(String optionNames) {
		XChoice scaleChoice = new XChoice(this);
		LabelEnumeration theOptions = new LabelEnumeration(optionNames);
		while (theOptions.hasMoreElements())
			scaleChoice.addItem((String)theOptions.nextElement());
		scaleChoice.select(0);
		
		return scaleChoice;
	}
	
	private XPanel createChoicePanel(DataSet data, String variableKey, XChoice scaleChoice) {
		XPanel choicePanel = new XPanel();
		choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER));
		CoreVariable variable = data.getVariable(variableKey);
		
		XLabel choiceLabel = new XLabel(translate("Units for") + " " + variable.name, XLabel.CENTER, this);
		choiceLabel.setFont(getStandardBoldFont());
		
		OneValueView value = new OneValueView(data, variableKey + 2, this);
		
		choicePanel.add(choiceLabel);
		choicePanel.add(scaleChoice);
		choicePanel.add(value);
		
		return choicePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == xScaleChoice) {
			int newScale = xScaleChoice.getSelectedIndex();
			if (localHorizAxis.setAlternateLabels(newScale)) {
				localHorizAxis.repaint();
				xScaleVariable.setScale(xTransformString[newScale]);
				int currentSelection = data.getSelection().findSingleSetFlag();
				data.variableChanged("x2", currentSelection);
			}
			return true;
		}
		else if (target == yScaleChoice) {
			int newScale = yScaleChoice.getSelectedIndex();
			if (localVertAxis.setAlternateLabels(newScale)) {
				yScaleVariable.setScale(yTransformString[newScale]);
				int currentSelection = data.getSelection().findSingleSetFlag();
				data.variableChanged("y2", currentSelection);
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