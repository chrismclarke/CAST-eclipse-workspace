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


public class ConditNormalApplet extends RotateApplet {
	static final private String NORMAL_PARAMS_PARAM = "normalParams";
	static final private String DENSITY_NAME_PARAM = "densityName";
	static final private String DENSITY_AXIS_INFO_PARAM = "densityAxis";
	static final private String SHADE_CUTOFF_PARAM = "shadeCutoff";
	static final private String AXIS_STEPS_PARAM = "axisSteps";
	static final private String Y_SLIDER_LIMITS_PARAM = "ySliderLimits";
	static final private String X_SLIDER_LIMITS_PARAM = "xSliderLimits";
	static final private String DISTN_DECIMALS_PARAM = "distnDecimals";
	
	static final private Color kKeyColor[] = {Color.green, Color.red, Color.blue};
	static final private double kDimPropn = 0.5;
	static final private Color kDimKeyColor[] = new Color[kKeyColor.length];
	static {
		for (int i=0 ; i<kKeyColor.length ; i++)
			kDimKeyColor[i] = DataView.dimColor(kKeyColor[i], kDimPropn);
	}
	
	protected DataSet data;
	
	protected ColourMap colourMap, dimColourMap;
	
	private boolean showConditional = false;
	private int meanDecimals, varDecimals;
	
	private ParameterSlider corrSlider;
	private XChoice sliceChoice;
	
	private XPanel sliderPanel;
	private CardLayout sliderLayout;
	private ParameterSlider xSliceSlider, ySliceSlider;
	
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
		dimColourMap = new ColourMap(kDimKeyColor, keyValues);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		String xVarName = getParameter(X_VAR_NAME_PARAM);
		String yVarName = getParameter(Y_VAR_NAME_PARAM);
		BiNormalDistnVariable normalModel = new BiNormalDistnVariable(xVarName, yVarName);
		normalModel.setParams(getParameter(NORMAL_PARAMS_PARAM));
		data.addVariable("model", normalModel);
		
		setupColorMap();
		String decimalsString = getParameter(DISTN_DECIMALS_PARAM);
		if (decimalsString != null) {
			StringTokenizer st = new StringTokenizer(decimalsString);
			meanDecimals = Integer.parseInt(st.nextToken());
			varDecimals = Integer.parseInt(st.nextToken());
			showConditional = true;
		}
		
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
			ConditSurfaceView localView = new ConditSurfaceView(data, this, yAxis, densityAxis, xAxis, "model", axisSteps);
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
		thePanel.setLayout(new ProportionLayout(0.4, 10));
		
			XPanel corrPanel = new XPanel();
				corrPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
				corrSlider = new ParameterSlider(new NumValue(-0.8, 2), new NumValue(0.8, 2),
																	new NumValue(0.0, 2), MText.expandText("#rho#"), this);
			corrPanel.add(corrSlider);
			
		thePanel.add(ProportionLayout.LEFT, corrPanel);
			
			XPanel slicePanel = new XPanel();
			slicePanel.setLayout(new BorderLayout(0, 0));
				sliceChoice = new XChoice(translate("Condition") + ":", XChoice.VERTICAL_LEFT, this);
				sliceChoice.addItem(translate("None"));
				sliceChoice.addItem("Y");
				sliceChoice.addItem("X");
			slicePanel.add("West", sliceChoice);
			
				sliderPanel = new XPanel();
				sliderLayout = new CardLayout();
				sliderPanel.setLayout(sliderLayout);
			
				sliderPanel.add("noSlice", new XPanel());
				
					XPanel xSlicePanel = new XPanel();
					xSlicePanel.setLayout(new BorderLayout(0, 0));
					StringTokenizer st = new StringTokenizer(getParameter(X_SLIDER_LIMITS_PARAM));
					xSliceSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
											new NumValue(st.nextToken()), "x", this);
					xSlicePanel.add("Center", xSliceSlider);
				sliderPanel.add("xSlice", xSlicePanel);
				
					XPanel ySlicePanel = new XPanel();
					ySlicePanel.setLayout(new BorderLayout(0, 0));
					st = new StringTokenizer(getParameter(Y_SLIDER_LIMITS_PARAM));
					ySliceSlider = new ParameterSlider(new NumValue(st.nextToken()), new NumValue(st.nextToken()),
											new NumValue(st.nextToken()), "y", this);
					ySlicePanel.add("Center", ySliceSlider);
				sliderPanel.add("ySlice", ySlicePanel);
				
			slicePanel.add("Center", sliderPanel);
		
		thePanel.add(ProportionLayout.RIGHT, slicePanel);
			
		return thePanel;
	}
	
	private void setDistnLabel() {
		if (!showConditional)
			return;
		ConditSurfaceView view = (ConditSurfaceView)theView;
		BiNormalDistnVariable distn = (BiNormalDistnVariable)data.getVariable("model");
		double xMean = distn.xMean.toDouble();
		double yMean = distn.yMean.toDouble();
		double xSd = distn.xSd.toDouble();
		double ySd = distn.ySd.toDouble();
		double r = distn.xyCorr.toDouble();
		String distnString = null;
		switch (sliceChoice.getSelectedIndex()) {
			case ConditSurfaceView.Z_SLICE:
				double x = xSliceSlider.getParameter().toDouble();
				NumValue mean = new NumValue(yMean + ySd / xSd * r * (x - xMean), meanDecimals);
				NumValue var = new NumValue(ySd * ySd * (1 - r * r), varDecimals);
				distnString = translate("Normal") + "(" + mean.toString() + ", " + var.toString() + ")";
				break;
			case ConditSurfaceView.X_SLICE:
				double y = ySliceSlider.getParameter().toDouble();
				mean = new NumValue(xMean + xSd / ySd * r * (y - yMean), meanDecimals);
				var = new NumValue(xSd * xSd * (1 - r * r), varDecimals);
				distnString = translate("Normal") + "(" + mean.toString() + ", " + var.toString() + ")";
				break;
		}
		
		view.setTextLabel(distnString);
	}
	
	private boolean localAction(Object target) {
		if (target == corrSlider) {
			BiNormalDistnVariable distn = (BiNormalDistnVariable)data.getVariable("model");
			distn.setCorr(corrSlider.getParameter());
			setDistnLabel();
			data.variableChanged("model");
			return true;
		}
		else {
			ConditSurfaceView view = (ConditSurfaceView)theView;
			if (target == sliceChoice) {
				switch (sliceChoice.getSelectedIndex()) {
					case ConditSurfaceView.NO_SLICE:
						sliderLayout.show(sliderPanel, "noSlice");
						view.setSliceType(ConditSurfaceView.NO_SLICE);
						setDistnLabel();
						break;
					case ConditSurfaceView.Z_SLICE:
						sliderLayout.show(sliderPanel, "xSlice");
						view.setSliceType(ConditSurfaceView.Z_SLICE);
						view.setSliceValue(xSliceSlider.getParameter().toDouble());
						setDistnLabel();
						break;
					case ConditSurfaceView.X_SLICE:
						sliderLayout.show(sliderPanel, "ySlice");
						view.setSliceType(ConditSurfaceView.X_SLICE);
						view.setSliceValue(ySliceSlider.getParameter().toDouble());
						setDistnLabel();
						break;
					default:
						break;
				}
				return true;
			}
			else if (target == xSliceSlider) {
				view.setSliceValue(xSliceSlider.getParameter().toDouble());
				setDistnLabel();
				return true;
			}
			else if (target == ySliceSlider) {
				view.setSliceValue(ySliceSlider.getParameter().toDouble());
				setDistnLabel();
				return true;
			}
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