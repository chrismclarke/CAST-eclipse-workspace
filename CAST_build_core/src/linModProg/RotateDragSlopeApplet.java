package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;
import graphics3D.*;

import linMod.*;


public class RotateDragSlopeApplet extends RotatePDFApplet {
	static final private String X_LIMITS_PARAM = "xLimits";
	static final private String SLOPE_LIMITS_PARAM = "slopeLimits";
	static final private String MAX_PARAMS_PARAM = "maxParams";
	
	private XValueSlider xSlider;
	private ParameterSlider slopeSlider;
	private LSEquationView lsEquation;
	
	private NumValue minX, maxX, xStep, startX;
	private String minSlopeString, maxSlopeString;
	private int noOfSlopeSteps;
	private double startSlope;
	
	
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
		
//		RegnImages.loadRegn(this);
		
		super.setupApplet();
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateDragXView theView = new RotateDragXView(data, this, xAxis, yAxis, densityAxis, "model", "x", "y", startX);
		theView.setModelDrawType(RotateDragXView.DRAW_SIMPLE_BAND_PDF);
		theView.setDataDrawType(RotateDragXView.DRAW_LS_CROSSES);
		return theView;
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
			xSlider.setTitleFont(getStandardBoldFont());
			mainPanel.add(xSlider);
			
			String beta1 = MText.expandText("slope, #beta##sub1#");
			slopeSlider = new ParameterSlider(new NumValue(minSlopeString), new NumValue(maxSlopeString),
										new NumValue(startSlope), noOfSlopeSteps, beta1, this);
			mainPanel.add(slopeSlider);
			
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				addSampleButton(samplePanel);
			mainPanel.add(samplePanel);
			
			StringTokenizer theParams = new StringTokenizer(getParameter(MAX_PARAMS_PARAM));
			NumValue maxIntercept = new NumValue(theParams.nextToken());
			NumValue maxSlope = new NumValue(theParams.nextToken());
			lsEquation = new LSEquationView(data, this, "y", "x", maxIntercept, maxSlope, Color.blue, Color.blue);
			
			mainPanel.add(lsEquation);
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	protected void takeSample() {
		((RotateDragXView)theView).setShowData(true);
		lsEquation.setShowData(true);
		super.takeSample();
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
			((RotateDragXView)theView).setShowData(false);
			lsEquation.setShowData(false);
			theModel.setSlope(newSlope);
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