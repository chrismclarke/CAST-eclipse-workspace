package responseSurfaceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import graphics3D.*;

import multivarProg.*;
import responseSurface.*;


public class QuadSurfaceSliceApplet extends RotateApplet {
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String SHADE_CUTOFF_PARAM = "shadeCutoff";
	static final private String CONTOUR_AXIS_PARAM = "contourAxis";
	static final private String FIXED_CONTOURS_PARAM = "fixedContours";
	static final protected String W_VAR_NAME_PARAM = "wVarName";
	static final private String W_VALUES_PARAM = "wValues";
	static final private String W_LIMITS_PARAM = "wLimits";
	
	static final protected String kExplanKey[] = {"x", "z", "w"};
	static final protected String kSliceExplanKey[] = {"x", "z"};
	
	static final private Color kKeyColor[] = {Color.green, Color.red, Color.blue};
	
	private String xName, zName, wName, yName;
	protected DataSet data;
	
	private ColourMap colourMap;
	
	private XChoice displayTypeChoice;
	private int currentDisplayType = 0;
	
	private NumValue minW, maxW, startW;
	
	private ParameterSlider wSliceSlider;
	
	protected Color[] getKeyColors(double[] keyValues) {
		return kKeyColor;
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
			yName = getParameter(Y_VAR_NAME_PARAM);
			String yValues = getParameter(Y_VALUES_PARAM);
		data.addNumVariable("y", yName, yValues);
		
			xName = getParameter(X_VAR_NAME_PARAM);
			String xValues = getParameter(X_VALUES_PARAM);
		data.addNumVariable("x", xName, xValues);
		
			zName = getParameter(Z_VAR_NAME_PARAM);
			String zValues = getParameter(Z_VALUES_PARAM);
		data.addNumVariable("z", zName, zValues);
		
			wName = getParameter(W_VAR_NAME_PARAM);
			String wValues = getParameter(W_VALUES_PARAM);
		data.addNumVariable("w", wName, wValues);
		
			ResponseSurface3Model fullModel = new ResponseSurface3Model("Model", data, kExplanKey);
			int bDecs[] = new int[10];
			StringTokenizer st = new StringTokenizer(getParameter(PARAM_DECIMALS_PARAM));
			for (int i=0 ;i<10 ; i++)
				bDecs[i] = Integer.parseInt(st.nextToken());
			fullModel.setLSParams("y", bDecs, 9);
		data.addVariable("fullModel", fullModel);
		
			double[] keyValues = getKeyValues();
			colourMap = new ColourMap(getKeyColors(keyValues), keyValues);
			
			st = new StringTokenizer(getParameter(W_LIMITS_PARAM));
			minW = new NumValue(st.nextToken());
			maxW = new NumValue(st.nextToken());
			startW = new NumValue(st.nextToken());
		
			ResponseSurfaceSliceModel sliceModel = new ResponseSurfaceSliceModel("Slice", data,
																										kSliceExplanKey, fullModel, startW.toDouble());
		data.addVariable("sliceModel", sliceModel);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
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
		
			ContourControlView scaleView = new ContourControlView(data, this,
															scaleAxis, "sliceModel", colourMap, Double.NaN);
		
		thePanel.add("LeftMargin", scaleView);
		
			D3Axis xAxis = new D3Axis(xName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(zName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			ResponseSurfaceView localView = new ResponseSurfaceView(data, this,
												xAxis, yAxis, zAxis, "sliceModel", kSliceExplanKey, "y");
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
		XPanel thePanel = new InsetPanel(30, 0);
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER));
			
				wSliceSlider = new ParameterSlider(minW, maxW, startW,
																		translate("Slice for") + " " + getParameter(W_VAR_NAME_PARAM), this);
			leftPanel.add(wSliceSlider);
			
		thePanel.add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER));
			
				XLabel displayLabel = new XLabel(translate("Display type") + ":", XLabel.CENTER, this);
				displayLabel.setFont(getStandardBoldFont());
			rightPanel.add(displayLabel);
				
				displayTypeChoice = new XChoice(this);
				displayTypeChoice.addItem(translate("Shaded surface"));
				displayTypeChoice.addItem(translate("Selected contours"));
			rightPanel.add(displayTypeChoice);
			
		thePanel.add("East", rightPanel);
		
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
		else if (target == wSliceSlider) {
			double w = wSliceSlider.getParameter().toDouble();
			ResponseSurfaceSliceModel sliceModel = (ResponseSurfaceSliceModel)data.getVariable("sliceModel");
			sliceModel.setSliceValue(w);
			data.variableChanged("sliceModel");
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