package bivarDistnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import coreGraphics.*;
import graphics3D.*;
import formula.*;

import multivarProg.*;
import bivarDistn.*;


public class BivarNormalParamApplet extends RotateApplet {
//	static final private String NORMAL_PARAMS_PARAM = "normalParams";
	static final private String DENSITY_NAME_PARAM = "densityName";
	static final private String DENSITY_AXIS_INFO_PARAM = "densityAxis";
	static final private String SHADE_CUTOFF_PARAM = "shadeCutoff";
	static final private String AXIS_STEPS_PARAM = "axisSteps";
	
	static final private String CORR_LIMITS_PARAM = "corrLimits";
	static final private String X_MEAN_LIMITS_PARAM = "xMeanLimits";
	static final private String X_SD_LIMITS_PARAM = "xSdLimits";
	static final private String Y_MEAN_LIMITS_PARAM = "yMeanLimits";
	static final private String Y_SD_LIMITS_PARAM = "ySdLimits";
	
	static final private Color kKeyColor[] = {Color.green, Color.red, Color.blue};
	
	protected DataSet data;
	
	protected ColourMap colourMap;
	
	private ParameterSlider corrSlider, xMeanSlider, xSdSlider, yMeanSlider, ySdSlider;
	
	private double[] getKeyValues() {
		double keyValue[] = new double[kKeyColor.length];
		StringTokenizer st = new StringTokenizer(getParameter(SHADE_CUTOFF_PARAM));
		for (int i=0 ; i<keyValue.length ; i++)
			keyValue[i] = Double.parseDouble(st.nextToken());
		return keyValue;
	}
	
	protected void setupColorMap() {
		double[] keyValues = getKeyValues();
		colourMap = new ColourMap(kKeyColor, keyValues);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		String xVarName = getParameter(X_VAR_NAME_PARAM);
		String yVarName = getParameter(Y_VAR_NAME_PARAM);
		BiNormalDistnVariable normalModel = new BiNormalDistnVariable(xVarName, yVarName);
		data.addVariable("model", normalModel);
		
		setupColorMap();
		
		return data;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
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
			theView = localView;
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createXYDRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.3333, 10));
		
			XPanel meanPanel = new XPanel();
			meanPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
		
				StringTokenizer st = new StringTokenizer(getParameter(X_MEAN_LIMITS_PARAM));
				xMeanSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
																new NumValue(st.nextToken()), MText.expandText("#mu#_X"), this);
			meanPanel.add(xMeanSlider);
				st = new StringTokenizer(getParameter(Y_MEAN_LIMITS_PARAM));
				yMeanSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
																new NumValue(st.nextToken()), MText.expandText("#mu#_Y"), this);
			meanPanel.add(yMeanSlider);
		
		thePanel.add(ProportionLayout.LEFT, meanPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.5, 10));
			
				XPanel sdPanel = new XPanel();
				sdPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
			
					st = new StringTokenizer(getParameter(X_SD_LIMITS_PARAM));
					xSdSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
																	new NumValue(st.nextToken()), MText.expandText("#sigma#_X"), this);
				sdPanel.add(xSdSlider);
					st = new StringTokenizer(getParameter(Y_SD_LIMITS_PARAM));
					ySdSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
																	new NumValue(st.nextToken()), MText.expandText("#sigma#_Y"), this);
				sdPanel.add(ySdSlider);
			
			rightPanel.add(ProportionLayout.LEFT, sdPanel);
				
				XPanel corrPanel = new XPanel();
				corrPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
					st = new StringTokenizer(getParameter(CORR_LIMITS_PARAM));
					corrSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
																		new NumValue(st.nextToken()), MText.expandText("#rho#"), this);
				corrPanel.add(corrSlider);
			rightPanel.add(ProportionLayout.RIGHT, corrPanel);
		
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
			
		
		BiNormalDistnVariable distn = (BiNormalDistnVariable)data.getVariable("model");
		
			distn.setCorr(corrSlider.getParameter());
			distn.setXMean(xMeanSlider.getParameter());
			distn.setXSd(xSdSlider.getParameter());
			distn.setYMean(yMeanSlider.getParameter());
			distn.setYSd(ySdSlider.getParameter());
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		BiNormalDistnVariable distn = (BiNormalDistnVariable)data.getVariable("model");
		if (target == corrSlider) {
			distn.setCorr(corrSlider.getParameter());
			data.variableChanged("model");
			return true;
		}
		else if (target == xMeanSlider) {
			distn.setXMean(xMeanSlider.getParameter());
			data.variableChanged("model");
			return true;
		}
		else if (target == xSdSlider) {
			distn.setXSd(xSdSlider.getParameter());
			data.variableChanged("model");
			return true;
		}
		else if (target == yMeanSlider) {
			distn.setYMean(yMeanSlider.getParameter());
			data.variableChanged("model");
			return true;
		}
		else if (target == ySdSlider) {
			distn.setYSd(ySdSlider.getParameter());
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