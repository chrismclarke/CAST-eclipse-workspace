package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import exper.*;


public class RotateTwoFactorApplet extends CoreMultiFactorApplet {
	static final protected String RESPONSE_AXIS_INFO_PARAM = "responseAxis";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String INITIAL_ROTATION_PARAM = "initialRotation";
	static final protected String TREAT1_EFFECT_AXIS_PARAM = "treat1EffectAxis";
	static final protected String TREAT2_EFFECT_AXIS_PARAM = "treat2EffectAxis";
	
	static final private String kTwoTreatKey[] = {"treat1", "treat2"};
	
	protected RotateTwoFactorView theView;
	protected ConstantSliderPanel constantSliderPanel;
	
	public void setupApplet() {
		readEffects();
		
		data = readData();
		
		setLayout(new ProportionLayout(0.65, 10, ProportionLayout.HORIZONTAL,
																																ProportionLayout.TOTAL));
		add(ProportionLayout.LEFT, rotatePanel(data));
		add(ProportionLayout.RIGHT, controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
			
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			FactorsModel model = new FactorsModel(getParameter(RESPONSE_NAME_PARAM), data,
																							getTreatKeys(), constant, effects, decimals);
			
		data.addVariable("model", model);
		
		return data;
	}
	
	protected String[] getTreatKeys() {
		return kTwoTreatKey;
	}
	
	protected XPanel rotatePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(2, 2));
		thePanel.add("Center", threeDPanel(data));
			
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
			
			buttonPanel.add(RotateButton.createRotationPanel(theView, this));
			buttonPanel.add(namePanel(data));
		thePanel.add("South", buttonPanel);
		
		String initialRotationString = getParameter(INITIAL_ROTATION_PARAM);
		if (initialRotationString != null) {
			StringTokenizer theAngles = new StringTokenizer(initialRotationString);
			int roundDens = Integer.parseInt(theAngles.nextToken());
			int ofDens = Integer.parseInt(theAngles.nextToken());
			theView.rotateTo(roundDens, ofDens);
		}
		return thePanel;
	}
	
	protected RotateTwoFactorView create3DView(DataSet data, D3Axis xAxis, D3Axis yAxis,
																													D3Axis zAxis, String yVarKey) {
		RotateTwoFactorView theView = new RotateTwoFactorView(data, this, xAxis, yAxis, zAxis,
																					kTwoTreatKey[0], yVarKey, kTwoTreatKey[1], "model");
		theView.setDrawData(false);
		return theView;
	}
	
	protected XPanel threeDPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			CatVariable xVar = (CatVariable)data.getVariable(kTwoTreatKey[0]);
			D3Axis xAxis = new D3Axis(xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setCatScale(xVar);
			D3Axis yAxis = new D3Axis(getVarName("model"), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(RESPONSE_AXIS_INFO_PARAM));
			CatVariable zVar = (CatVariable)data.getVariable(kTwoTreatKey[1]);
			D3Axis zAxis = new D3Axis(zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setCatScale(zVar);
			
			String yVarKey = "response";											//	for RotateTwoFactorLSApplet
			if (data.getVariable(yVarKey) == null)
				yVarKey = null;
			
			theView = create3DView(data, xAxis, yAxis, zAxis, yVarKey);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			constantSliderPanel = new ConstantSliderPanel(data, "model", this);
		thePanel.add("North", constantSliderPanel);
			
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																																ProportionLayout.TOTAL));
			mainPanel.add(ProportionLayout.TOP, new EffectSlidersPanel(data, kTwoTreatKey[0], "model", 0,
																	getParameter(TREAT1_EFFECT_AXIS_PARAM), false, false, this));
			mainPanel.add(ProportionLayout.BOTTOM, new EffectSlidersPanel(data, kTwoTreatKey[1], "model", 1,
																	getParameter(TREAT2_EFFECT_AXIS_PARAM), false, false, this));
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	protected Color getFactorColor(int factorIndex) {
		int axisInt = factorIndex == 0 ? D3Axis.X_AXIS : D3Axis.Z_AXIS;
		return D3Axis.axisColor[axisInt][D3Axis.FOREGROUND];
	}
	
	protected String getVarName(String key) {
		return data.getVariable(key).name;
	}
	
	protected XPanel namePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		XLabel yLabel = new XLabel("y:" + getVarName("model"), XLabel.LEFT, this);
		yLabel.setForeground(D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(yLabel);
		
		XLabel xLabel = new XLabel("x:" + getVarName(kTwoTreatKey[0]), XLabel.LEFT, this);
		xLabel.setForeground(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(xLabel);
		
		XLabel zLabel = new XLabel("z:" + getVarName(kTwoTreatKey[1]), XLabel.LEFT, this);
		zLabel.setForeground(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(zLabel);
		return thePanel;
	}
	
	public void finishAnimation() {
	}
}