package continProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import contin.*;
import multivarProg.RotateApplet;


public class RotateBarApplet extends RotateApplet {
	static final protected String PROB_AXIS_INFO_PARAM = "probAxis";
	static final protected String COUNT_AXIS_INFO_PARAM = "countAxis";
	static final protected String Y_LABELS_PARAM = "yLabels";
	static final protected String X_LABELS_PARAM = "xLabels";
	
	static final protected String X_MARGIN_PARAM = "xMargin";
	static final protected String Y_CONDIT_PARAM = "yCondit";
	
	static final protected String TYPE_PARAM = "transitions";
	
	private CoreVariable xVariable, yVariable;
	
	private D3ProbCountAxis probCountAxis = null;
	
	private TransGraphicChoice displayChoice;
	private int currentType = RotateContinView.JOINT;
	private XChoice probCountChoice;
	private int currentProbCount = 0;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String xValues = getParameter(X_VALUES_PARAM);
		if (xValues == null) {
			CatDistnVariable localX = new CatDistnVariable(getParameter(X_VAR_NAME_PARAM));
			localX.readLabels(getParameter(X_LABELS_PARAM));
			localX.setParams(getParameter(X_MARGIN_PARAM));
			xVariable = localX;
		}
		else {
			CatVariable localX = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			localX.readLabels(getParameter(X_LABELS_PARAM));
			localX.readValues(xValues);
			xVariable = localX;
		}
		data.addVariable("x", xVariable);
		
		String yValues = getParameter(Y_VALUES_PARAM);
		if (yValues == null) {
			ContinResponseVariable localY = new ContinResponseVariable(getParameter(Y_VAR_NAME_PARAM), data, "x");
			localY.readLabels(getParameter(Y_LABELS_PARAM));
			localY.setProbs(getParameter(Y_CONDIT_PARAM), ContinResponseVariable.CONDITIONAL);
			yVariable = localY;
		}
		else {
			CatVariable localY = new CatVariable(getParameter(Y_VAR_NAME_PARAM));
			localY.readLabels(getParameter(Y_LABELS_PARAM));
			localY.readValues(yValues);
			yVariable = localY;
		}
		data.addVariable("y", yVariable);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(yVariable.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setCatScale((CatVariableInterface)yVariable);
		D3Axis zAxis = new D3Axis(xVariable.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setCatScale((CatVariableInterface)xVariable);
		
		D3Axis probAxis;
		String countAxisInfo = getParameter(COUNT_AXIS_INFO_PARAM);
		if (countAxisInfo != null) {
			probCountAxis = new D3ProbCountAxis(D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
			probCountAxis.setNumScale(getParameter(PROB_AXIS_INFO_PARAM), countAxisInfo);
			probAxis = probCountAxis;
		}
		else {
			probAxis = new D3Axis("Prob", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
			probAxis.setNumScale(getParameter(PROB_AXIS_INFO_PARAM));
		}
		
		theView = new RotateContinView(data, this, xAxis, probAxis, zAxis,"y", "x");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new ProportionLayout(0.4, 0, ProportionLayout.VERTICAL));
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
		
//		thePanel.add(ProportionLayout.TOP, rotationPanel());
		thePanel.add(rotationPanel());
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
				displayChoice = createChoice();
			if (displayChoice != null)
				choicePanel.add(displayChoice);
		
			if (probCountAxis != null) {
				XPanel vertAxisPanel = new XPanel();
				vertAxisPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				vertAxisPanel.add(new XLabel(translate("Vertical axis") + ":", XLabel.LEFT, this));
					probCountChoice = new XChoice(this);
					probCountChoice.addItem(translate("Frequency"));
					probCountChoice.addItem(translate("Proportion"));
				vertAxisPanel.add(probCountChoice);
				choicePanel.add(vertAxisPanel);
			}
		
//		thePanel.add(ProportionLayout.BOTTOM, choicePanel);
		thePanel.add(choicePanel);
		
		return thePanel;
	}
	
	private XPanel rotationPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		String type = getParameter(TYPE_PARAM);
		boolean basicGraph = type == null || type.equals("none");
			XPanel buttonPanel = RotateButton.createXYDRotationPanel(theView, this,
																basicGraph ? RotateButton.VERTICAL : RotateButton.HORIZONTAL);
		
		thePanel.add(buttonPanel);
			rotateButton = new XButton(translate("Spin"), this);
		
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected TransGraphicChoice createChoice() {
		String type = getParameter(TYPE_PARAM);
		boolean marginal = (type != null) && type.indexOf("margin") >= 0;
		boolean conditional = (type != null) && type.indexOf("condit") >= 0;
		
		if (marginal || conditional)
			return new TransGraphicChoice(marginal ? TransGraphicChoice.MARGINAL
																	: TransGraphicChoice.CONDITIONAL, this);
		else
			return null;
	}
	
	protected String getYAxisName() {
		return "Prob";
	}
	
	private boolean localAction(Object target) {
		if (target == displayChoice) {
			if (displayChoice.getCurrentType() != currentType) {
				currentType = displayChoice.getCurrentType();
				((RotateContinView)theView).animateChange(currentType);
			}
			return true;
		}
		else if (target == probCountChoice) {
			if (probCountChoice.getSelectedIndex() != currentProbCount) {
				currentProbCount = probCountChoice.getSelectedIndex();
				probCountAxis.setDisplayType(currentProbCount);
				theView.repaint();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}