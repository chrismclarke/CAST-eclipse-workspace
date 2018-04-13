package curveInteractProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multiRegn.*;


public class ChooseTermsQuadApplet extends DragQuadXZApplet {
	static final private String LONG_Y_NAME_PARAM = "longYVarName";
	static final private String LONG_X_NAME_PARAM = "longXVarName";
	static final private String LONG_Z_NAME_PARAM = "longZVarName";
	static final protected String INITIAL_PARAM_PARAM = "initialParams";
	
	static final private String MAX_RSS_PARAM = "maxRss";
	
	static final protected String kXZKeys[] = {"x", "z"};
	
	private XCheckbox linearXCheck, linearZCheck, quadXCheck, quadZCheck;
	
	protected DataSet readData() {
		data = new DataSet();
		
		yVarName = getParameter(Y_VAR_NAME_PARAM);
		xVarName = getParameter(X_VAR_NAME_PARAM);
		zVarName = getParameter(Z_VAR_NAME_PARAM);
		
		data.addNumVariable("y", yVarName, getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", xVarName, getParameter(X_VALUES_PARAM));
		data.addNumVariable("z", zVarName, getParameter(Z_VALUES_PARAM));
		
		ResponseSurfaceModel model = new ResponseSurfaceModel("model", data, kXZKeys,
																								getParameter(INITIAL_PARAM_PARAM));
		data.addVariable("model", model);
		
		readMinMaxParams();
		
		setupColorMap();
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(getParameter(LONG_X_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(getParameter(LONG_Y_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(getParameter(LONG_Z_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			ResponseSurfaceView localView = new ResponseSurfaceView(data, this, xAxis, yAxis, zAxis,
																																						"model", kXZKeys, "y");
			localView.setColourMap(colourMap);
			localView.setDrawResids(true);
			localView.setSquaredResids(true);
			localView.lockBackground(Color.white);
			theView = localView;
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private void setParamsToLS() {
		boolean termsInModel[] = new boolean[6];
		termsInModel[0] = true;
		termsInModel[1] = linearXCheck.getState();
		termsInModel[2] = linearZCheck.getState();
		termsInModel[3] = quadXCheck.getState();
		termsInModel[4] = quadZCheck.getState();
		termsInModel[5] = false;
		
		double constraints[] = new double[6];
		for (int i=0 ; i<6 ; i++)
			constraints[i] = termsInModel[i] ? Double.NaN : 0.0;
		
		ResponseSurfaceModel model = (ResponseSurfaceModel)data.getVariable("model");
		model.updateLSParams("y", constraints);
		
		equationView.setDrawParameters(termsInModel);
		
		data.variableChanged("model");
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			XPanel termsPanel = new XPanel();
			termsPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 5));
			
				XLabel residLabel = new XLabel(translate("Terms in model") + ":", XLabel.LEFT, this);
				residLabel.setFont(getStandardBoldFont());
			
			termsPanel.add(residLabel);
				
				XPanel xCheckPanel = new XPanel();
				xCheckPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
					String xName = data.getVariable("x").name;
					linearXCheck = new XCheckbox(translate("Linear") + " " + xName, this);
				xCheckPanel.add(linearXCheck);
				
					XPanel quadXPanel = new InsetPanel(20, 0);
					quadXPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
						quadXCheck = new XCheckbox(translate("Quadratic") + " " + xName, this);
						quadXCheck.disable();
					quadXPanel.add(quadXCheck);
				xCheckPanel.add(quadXPanel);
				
			termsPanel.add(xCheckPanel);

				XPanel zCheckPanel = new XPanel();
				zCheckPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
					String zName = data.getVariable("z").name;
					linearZCheck = new XCheckbox(translate("Linear") + " " + zName, this);
				zCheckPanel.add(linearZCheck);
				
					XPanel quadZPanel = new InsetPanel(20, 0);
					quadZPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
						quadZCheck = new XCheckbox(translate("Quadratic") + " " + zName, this);
						quadZCheck.disable();
					quadZPanel.add(quadZCheck);
				zCheckPanel.add(quadZPanel);
				
			termsPanel.add(zCheckPanel);
				
//				residDisplayChoice = new XChoice(this);
//				residDisplayChoice.addItem("Don't show");
//				residDisplayChoice.addItem("Show as lines");
//				residDisplayChoice.addItem("Show as squares");
//			termsPanel.add(residDisplayChoice);
		
		thePanel.add(termsPanel);
		
		setParamsToLS();
		
		thePanel.add(new MultiResidSsqView(data, "y", "model", new NumValue(getParameter(MAX_RSS_PARAM)), this));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.HORIZONTAL);
			rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == linearXCheck) {
			if (quadXCheck.getState())
				quadXCheck.setState(false);
			if (linearXCheck.getState())
				quadXCheck.enable();
			else
				quadXCheck.disable();
			setParamsToLS();
			return true;
		}
		else if (target == linearZCheck) {
			if (quadZCheck.getState())
				quadZCheck.setState(false);
			if (linearZCheck.getState())
				quadZCheck.enable();
			else
				quadZCheck.disable();
			setParamsToLS();
			return true;
		}
		else if (target == quadXCheck || target == quadZCheck) {
			setParamsToLS();
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