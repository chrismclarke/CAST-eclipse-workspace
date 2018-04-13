package responseSurfaceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class QuadSurfaceApplet extends RotateApplet {
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String SHADE_CUTOFF_PARAM = "shadeCutoff";
	static final private String CONTOUR_PARAM = "contour";
	static final private String CONTOUR_AXIS_PARAM = "contourAxis";
	static final private String FIXED_CONTOURS_PARAM = "fixedContours";
	static final private String MAX_COEFFS_PARAM = "maxCoeffs";
	static final private String SHORT_NAMES_PARAM = "shortNames";
	
	static final protected String explanKey[] = {"x", "z"};
	
	static final private Color kKeyColor[] = {Color.green, Color.red, Color.blue};
	
	protected String xName, zName, yName;
	protected DataSet data;
	
	protected ColourMap colourMap;
	
	private XChoice displayTypeChoice;
	private int currentDisplayType = 0;
	
	protected MultiLinearEqnView theEqn;
	
	protected Color[] getKeyColors(double[] keyValues) {
		return kKeyColor;
	}
	
	protected void addModel(DataSet data, String[] xKey, String yKey, String modelKey) {
		ResponseSurfaceModel model = new ResponseSurfaceModel("Model", data, xKey);
		int bDecs[] = new int[6];
		StringTokenizer st = new StringTokenizer(getParameter(PARAM_DECIMALS_PARAM));
		for (int i=0 ;i<6 ; i++)
			bDecs[i] = Integer.parseInt(st.nextToken());
		model.setLSParams(yKey, bDecs, 9);
		
		data.addVariable(modelKey, model);
	}
	
	protected void addDataValues(DataSet data) {
			yName = getParameter(Y_VAR_NAME_PARAM);
			String yValues = getParameter(Y_VALUES_PARAM);
		data.addNumVariable("y", yName, yValues);
		
			xName = getParameter(X_VAR_NAME_PARAM);
			String xValues = getParameter(X_VALUES_PARAM);
		data.addNumVariable("x", xName, xValues);
		
			zName = getParameter(Z_VAR_NAME_PARAM);
			String zValues = getParameter(Z_VALUES_PARAM);
		data.addNumVariable("z", zName, zValues);
	}
	
	protected void setupColorMap() {
		double[] keyValues = getKeyValues();
		colourMap = new ColourMap(getKeyColors(keyValues), keyValues);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		addDataValues(data);
		addModel(data, explanKey, "y", "model");
		
		setupColorMap();
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(SHORT_NAMES_PARAM));
			String shortYName = st.nextToken();
			String shortXName = st.nextToken();
			String shortZName = st.nextToken();
			String shortNames[] = {shortXName, shortZName, shortXName, shortZName, shortXName + shortZName};
			NumValue maxParam[] = new NumValue[6];
			st = new StringTokenizer(getParameter(MAX_COEFFS_PARAM));
			for (int i=0 ; i<6 ; i++)
				maxParam[i] = new NumValue(st.nextToken());
			
			theEqn = new MultiLinearEqnView(data, this, "model", shortYName, shortNames, maxParam, maxParam);
			theEqn.setSquaredExplan(2, true);
			theEqn.setSquaredExplan(3, true);
		thePanel.add(theEqn);
		return thePanel;
	}
	
	private double[] getKeyValues() {
		double keyValue[] = new double[kKeyColor.length];
		StringTokenizer st = new StringTokenizer(getParameter(SHADE_CUTOFF_PARAM));
		for (int i=0 ; i<keyValue.length ; i++)
			keyValue[i] = Double.parseDouble(st.nextToken());
		return keyValue;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			VertAxis scaleAxis = new VertAxis(this);
			scaleAxis.readNumLabels(getParameter(CONTOUR_AXIS_PARAM));
		thePanel.add("Left", scaleAxis);
		
			double contourValue = Double.parseDouble(getParameter(CONTOUR_PARAM));
			ContourControlView scaleView = new ContourControlView(data, this, scaleAxis, "model", colourMap, contourValue);
		
		thePanel.add("LeftMargin", scaleView);
		
			D3Axis xAxis = new D3Axis(xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			ResponseSurfaceView localView = new ResponseSurfaceView(data, this,
																				xAxis, yAxis, zAxis, "model", explanKey, "y");
			localView.lockBackground(Color.white);
			localView.setColourMap(colourMap);
			localView.setContourControl(scaleView);
			localView.setCrossSize(DataView.LARGE_CROSS);
			theView = localView;
			
			StringTokenizer st = new StringTokenizer(getParameter(FIXED_CONTOURS_PARAM));
			double fixedContours[] = new double[st.countTokens()];
			for (int i=0 ; i<fixedContours.length ; i++)
				fixedContours[i] = Double.parseDouble(st.nextToken());
			localView.setFixedContours(fixedContours);
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER));
		
			XLabel displayLabel = new XLabel(translate("Display type") + ":", XLabel.CENTER, this);
			displayLabel.setFont(getStandardBoldFont());
		thePanel.add(displayLabel);
			
			displayTypeChoice = new XChoice(this);
			displayTypeChoice.addItem(translate("Shaded surface"));
			displayTypeChoice.addItem(translate("Selected contours"));
		thePanel.add(displayTypeChoice);
			
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == displayTypeChoice) {
			int newChoice = displayTypeChoice.getSelectedIndex();
			if (newChoice != currentDisplayType) {
				currentDisplayType = newChoice;
				
				int displayType = (newChoice == 0) ? ResponseSurfaceView.SURFACE : ResponseSurfaceView.CONTOURS;
				((ResponseSurfaceView)theView).setDrawType(displayType);
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