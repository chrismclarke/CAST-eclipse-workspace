package mixtureProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;
import mixture.*;


public class MixtureSurfaceApplet extends RotateApplet {
	static final private String R_VAR_NAME_PARAM = "rVarName";
	static final private String R_VALUES_PARAM = "rValues";
	static final private String S_VAR_NAME_PARAM = "sVarName";
	static final private String S_VALUES_PARAM = "sValues";
	static final private String T_VAR_NAME_PARAM = "tVarName";
	static final private String T_VALUES_PARAM = "tValues";
	
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String SHADE_CUTOFF_PARAM = "shadeCutoff";
	static final private String CONTOUR_PARAM = "contour";
	static final private String CONTOUR_AXIS_PARAM = "contourAxis";
	static final private String FIXED_CONTOURS_PARAM = "fixedContours";
	static final private String MAX_COEFFS_PARAM = "maxCoeffs";
	static final private String SHORT_NAMES_PARAM = "shortNames";
	
	static final private String explanKey[] = {"r", "s", "t"};
	static final private String kAxisInfo = "0 1.2 0 0.2";
	
	static final private Color kKeyColor[] = {Color.green, Color.red, Color.blue};
	
	static final private double kLinearConstraints[] = {Double.NaN, Double.NaN, Double.NaN, 0.0, 0.0, 0.0};
	static final private double kQuadraticConstraints[] = {Double.NaN, Double.NaN, Double.NaN,
																															Double.NaN, Double.NaN, Double.NaN};
	
	static final private boolean kLinearParamVis[] = {true, true, true, false, false, false};
	static final private boolean kQuadraticParamVis[] = {true, true, true, true, true, true};
	
	protected String rName, sName, tName, yName;
	protected DataSet data;
	
	protected ColourMap colourMap;
	
	private XChoice displayTypeChoice;
	private int currentDisplayType = 0;
	
	private XCheckbox quadraticCheck;
	
	protected MultiLinearEqnView theEqn;
	
	protected Color[] getKeyColors(double[] keyValues) {
		return kKeyColor;
	}
	
	protected void addModel(DataSet data, String[] xKey, String yKey, String modelKey) {
		MixtureModel model = new MixtureModel("Model", data, xKey);
		int bDecs[] = new int[6];
		StringTokenizer st = new StringTokenizer(getParameter(PARAM_DECIMALS_PARAM));
		for (int i=0 ;i<6 ; i++)
			bDecs[i] = Integer.parseInt(st.nextToken());
		model.setLSParams(yKey, kLinearConstraints, bDecs, 9);
		
		data.addVariable(modelKey, model);
	}
	
	protected void addDataValues(DataSet data) {
			yName = getParameter(Y_VAR_NAME_PARAM);
			String yValues = getParameter(Y_VALUES_PARAM);
		data.addNumVariable("y", yName, yValues);
		
			rName = getParameter(R_VAR_NAME_PARAM);
			String rValues = getParameter(R_VALUES_PARAM);
		data.addNumVariable("r", rName, rValues);
		
			sName = getParameter(S_VAR_NAME_PARAM);
			String sValues = getParameter(S_VALUES_PARAM);
		data.addNumVariable("s", sName, sValues);
		
			tName = getParameter(T_VAR_NAME_PARAM);
			String tValues = getParameter(T_VALUES_PARAM);
		data.addNumVariable("t", tName, tValues);
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
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(SHORT_NAMES_PARAM));
			String shortYName = st.nextToken();
			String shortRName = st.nextToken();
			String shortSName = st.nextToken();
			String shortTName = st.nextToken();
			String shortNames[] = {shortRName, shortSName, shortTName, shortRName+shortSName,
																									shortRName+shortTName, shortSName+shortTName};
			NumValue maxParam[] = new NumValue[6];
			st = new StringTokenizer(getParameter(MAX_COEFFS_PARAM));
			for (int i=0 ; i<6 ; i++)
				maxParam[i] = new NumValue(st.nextToken());
			
			theEqn = new MultiLinearEqnView(data, this, "model", shortYName, shortNames, maxParam, maxParam);
			theEqn.setDrawParameters(kLinearParamVis);
			theEqn.setHasIntercept(false);
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
		
			D3Axis xAxis = new D3Axis("", D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(kAxisInfo);
			xAxis.setShow(false);
			D3Axis yAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			yAxis.setLabelName(getParameter(Y_VAR_NAME_PARAM));
			D3Axis zAxis = new D3Axis("", D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(kAxisInfo);
			zAxis.setShow(false);
			
			MixtureSurfaceView localView = new MixtureSurfaceView(data, this, xAxis, yAxis, zAxis, "model", explanKey, "y");
			localView.lockBackground(Color.white);
			localView.setColourMap(colourMap);
			localView.setContourControl(scaleView);
			localView.setCrossSize(DataView.LARGE_CROSS);
			localView.setBigHitRadius();
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
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				quadraticCheck = new XCheckbox(translate("Quadratic terms"), this);
			leftPanel.add(quadraticCheck);
			
		thePanel.add(leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XLabel displayLabel = new XLabel(translate("Display type") + ":", XLabel.CENTER, this);
				displayLabel.setFont(getStandardBoldFont());
			rightPanel.add(displayLabel);
				
				displayTypeChoice = new XChoice(this);
				displayTypeChoice.addItem(translate("Shaded surface"));
				displayTypeChoice.addItem(translate("Selected contours"));
			rightPanel.add(displayTypeChoice);
			
		thePanel.add(rightPanel);
			
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
				
				int displayType = (newChoice == 0) ? MixtureSurfaceView.SURFACE : MixtureSurfaceView.CONTOURS;
				((MixtureSurfaceView)theView).setDrawType(displayType);
				theView.repaint();
			}
			return true;
		}
		else if (target == quadraticCheck) {
			boolean quadNotLin = quadraticCheck.getState();
			MixtureModel model = (MixtureModel)data.getVariable("model");
			model.updateLSParams("y", quadNotLin ? kQuadraticConstraints : kLinearConstraints);
			theEqn.setDrawParameters(quadNotLin ? kQuadraticParamVis : kLinearParamVis);
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