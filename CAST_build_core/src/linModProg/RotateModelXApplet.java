package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;
import graphics3D.*;

import multivarProg.*;
import linMod.*;
import regn.*;


public class RotateModelXApplet extends RotateApplet {
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String SLOPE_LIMITS_PARAM = "slopeLimits";
	static final private String MAX_INTERCEPT_PARAM = "maxIntercept";
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final private String FIXED_VALUE_PARAM = "fixedValue";
	
	static final protected String kDefaultZAxis = "0.0 1.0 2.0 1.0";
	
	protected DataSet data;
	
	private XValueSlider xSlider;
	private ParameterSlider slopeSlider;
	
	private NumValue minX, maxX, xStep, startX;
	private String minSlopeString, maxSlopeString;
	private int noOfSlopeSteps;
	private double startSlope;
	
	private double fixedX, fixedY;
	
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(X_LIMITS_PARAM));
		minX = new NumValue(theParams.nextToken());
		maxX = new NumValue(theParams.nextToken());
		xStep = new NumValue(theParams.nextToken());
		startX = new NumValue(theParams.nextToken());
		
		theParams = new StringTokenizer(getParameter(SLOPE_LIMITS_PARAM));
		minSlopeString = theParams.nextToken();
		maxSlopeString = theParams.nextToken();
		noOfSlopeSteps = Integer.parseInt(theParams.nextToken());
		startSlope = Double.parseDouble(theParams.nextToken());
		
		RegnImages.loadRegn(this);
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
			LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
			yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
			
		data.addVariable("model", yDistn);
		
			String fixedString = getParameter(FIXED_VALUE_PARAM);
			if (fixedString == null) {
				fixedX = 0.0;
				fixedY = yDistn.getIntercept().toDouble();
			}
			else {
				StringTokenizer st = new StringTokenizer(fixedString);
				fixedX = Double.parseDouble(st.nextToken());
				fixedY = Double.parseDouble(st.nextToken());
			}
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		D3Axis zAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(kDefaultZAxis);
		
		theView = getView(data, xAxis, yAxis, zAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateDragXView theView = new RotateDragXView(data, this, xAxis, yAxis, densityAxis, "model", null, null, startX);
		theView.setModelDrawType(RotateDragXView.DRAW_SIMPLE_BAND_PDF);
		return theView;
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
	
	protected XPanel rotationPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
		thePanel.add(RotateButton.create2DYRotationPanel(theView, this));
		
			XPanel spinPanel = new XPanel();
			spinPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				rotateButton = new XButton(translate("Spin"), this);
			spinPanel.add(rotateButton);
			
		thePanel.add(spinPanel);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("North", rotationPanel());
		
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
		
				xSlider = new XValueSlider(minX, maxX, xStep, startX, this);
				xSlider.setTitle(getParameter(X_VAR_NAME_PARAM) + " = ", this);
			mainPanel.add(xSlider);
			
				String beta1 = MText.expandText("#beta##sub1#");
				slopeSlider = new ParameterSlider(new NumValue(minSlopeString), new NumValue(maxSlopeString),
										new NumValue(startSlope), noOfSlopeSteps, beta1, this);
			mainPanel.add(slopeSlider);
			
				XPanel eqnPanel = new InsetPanel(0, 20, 0, 0);
				eqnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					NumValue maxIntercept = new NumValue(getParameter(MAX_INTERCEPT_PARAM));
					LinearEquationView lsEquation = new LinearEquationView(data, this, "model", "y", "x",
											maxIntercept, maxIntercept, new NumValue(minSlopeString), new NumValue(maxSlopeString));
					lsEquation.setFont(getBigFont());
					
				eqnPanel.add(lsEquation);
			
			mainPanel.add(eqnPanel);
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == xSlider) {
			NumValue newX = xSlider.getNumValue();
			((RotateDragXView)theView).setPDFDrawX(newX);
			theView.repaint();
			return true;
		}
		else if (target == slopeSlider) {
			double newSlope = slopeSlider.getParameter().toDouble();
			LinearModel theModel = (LinearModel)data.getVariable("model");
			theModel.setSlope(newSlope);
			theModel.setIntercept(fixedY - fixedX * newSlope);
			data.variableChanged("model");
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