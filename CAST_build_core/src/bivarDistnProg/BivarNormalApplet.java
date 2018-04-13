package bivarDistnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import graphics3D.*;
import formula.*;

import multivarProg.*;
import bivarDistn.*;


public class BivarNormalApplet extends RotateApplet {
	static final private String NORMAL_PARAMS_PARAM = "normalParams";
	static final private String DENSITY_NAME_PARAM = "densityName";
	static final private String DENSITY_AXIS_INFO_PARAM = "densityAxis";
	static final private String SHADE_CUTOFF_PARAM = "shadeCutoff";
	static final private String CONTOUR_PARAM = "contour";
	static final private String CONTOUR_AXIS_PARAM = "contourAxis";
//	static final private String FIXED_CONTOURS_PARAM = "fixedContours";
	static final private String AXIS_STEPS_PARAM = "axisSteps";
	
	static final private Color kKeyColor[] = {Color.green, Color.red, Color.blue};
	
	protected DataSet data;
	
	protected ColourMap colourMap;
	
	private ParameterSlider corrSlider;
	
	protected Color[] getKeyColors(double[] keyValues) {
		return kKeyColor;
	}
	
	protected void setupColorMap() {
		double[] keyValues = getKeyValues();
		colourMap = new ColourMap(getKeyColors(keyValues), keyValues);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		String xVarName = getParameter(X_VAR_NAME_PARAM);
		String yVarName = getParameter(Y_VAR_NAME_PARAM);
		BiNormalDistnVariable normalModel = new BiNormalDistnVariable(xVarName, yVarName);
		normalModel.setParams(getParameter(NORMAL_PARAMS_PARAM));
		data.addVariable("model", normalModel);
		
		setupColorMap();
		
		return data;
	}
	
	private double[] getKeyValues() {
		double keyValue[] = new double[kKeyColor.length];
		StringTokenizer st = new StringTokenizer(getParameter(SHADE_CUTOFF_PARAM));
		for (int i=0 ; i<keyValue.length ; i++)
			keyValue[i] = Double.parseDouble(st.nextToken());
		return keyValue;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();						//	Y = Density, X = Y, Z = X
		thePanel.setLayout(new AxisLayout());
		
			VertAxis scaleAxis = new VertAxis(this);
			scaleAxis.readNumLabels(getParameter(CONTOUR_AXIS_PARAM));
		thePanel.add("Left", scaleAxis);
		
			double contourValue = Double.parseDouble(getParameter(CONTOUR_PARAM));
			ContourControlView scaleView = new ContourControlView(data, this, scaleAxis, "model", colourMap, contourValue);
		
		thePanel.add("LeftMargin", scaleView);
			
			BiNormalDistnVariable distn = (BiNormalDistnVariable)data.getVariable("model");
			D3Axis xAxis = new D3Axis(distn.getXName(), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis densityAxis = new D3Axis(getParameter(DENSITY_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			densityAxis.setNumScale(getParameter(DENSITY_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(distn.getYName(), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			
			int axisSteps = Integer.parseInt(getParameter(AXIS_STEPS_PARAM));
			SurfaceView localView = new SurfaceView(data, this, yAxis, densityAxis, xAxis, "model", axisSteps);
			localView.lockBackground(Color.white);
			localView.setColourMap(colourMap);
			localView.setContourControl(scaleView);
			theView = localView;
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel rotatePanel = new XPanel();
			rotatePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				XPanel buttonPanel = RotateButton.createXYDRotationPanel(theView, this, RotateButton.HORIZONTAL);
			rotatePanel.add(buttonPanel);
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
			
		thePanel.add("West", rotatePanel);
		
			XPanel corrPanel = new XPanel();
			corrPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			corrSlider = new ParameterSlider(new NumValue(-0.8, 2), new NumValue(0.8, 2),
																	new NumValue(0.0, 2), MText.expandText("#rho#"), this);
			corrPanel.add(corrSlider);
		
		thePanel.add("Center", corrPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == corrSlider) {
			BiNormalDistnVariable distn = (BiNormalDistnVariable)data.getVariable("model");
			distn.setCorr(corrSlider.getParameter());
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