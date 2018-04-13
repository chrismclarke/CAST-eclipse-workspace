package responseSurfaceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import coreVariables.*;
import models.*;
import random.*;
import coreGraphics.*;
import graphics3D.*;

import multivarProg.*;
//import multiRegn.*;
import responseSurface.*;


public class CorrelSurfaceApplet extends RotateApplet {
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	static final private String NVALUES_PARAM = "nValues";
	
	static final private String explanKey[] = {"x", "y"};
	
	static final private Color kKeyColor[] = {Color.red, new Color(0xFFFFCC), Color.blue};
//	static final private Color kKeyColor[] = {Color.red, new Color(0xEEEEEE), Color.blue};
	static final private double kKeyValues[] = {-6.25, 0.0, 6.25};
	
	static final private String kSurfaceParams = "0.0 0.0 0.0 0.0 0.0 1.0 0.0";
	static final private String kStdAxisString = "-2.5 2.5 -2 1";
	static final private String kProductAxisString = "-6.2 6.2 -6 2";
	
	static final private int kDecimals = 3;
	static final private double kStart_a1 = Math.sqrt(0.5);
	
//	private String xName, zName;
	private DataSet data;
	
	private ColourMap colourMap;
	
	private ParameterSlider correlSlider;
	
	private void addDataValues(DataSet data) {
			int nValues = Integer.parseInt(getParameter(NVALUES_PARAM));
			
			RandomNormal generator = new RandomNormal(nValues, 0.0, 1.0, 2.5);
			generator.setSeed(Long.parseLong(getParameter(RANDOM_SEED_PARAM)));
			NumSampleVariable u1Var = new NumSampleVariable("u1", generator, kDecimals);
			u1Var.generateNextSample();
		data.addVariable("u1", u1Var);
		
			NumSampleVariable u2Var = new NumSampleVariable("u2", generator, kDecimals);
			u2Var.generateNextSample();
		data.addVariable("u2", u2Var);
		
			LinearModel uuModel = new LinearModel("uuModel", data, "u1");
			uuModel.setLSParams("u2", 3, 3, 3);
		data.addVariable("uuModel", uuModel);
		
			ResidValueVariable orthog_u2 = new ResidValueVariable("orthog_u2", data, "u1", "u2",
																			"uuModel", kDecimals);
		data.addVariable("orthog_u2", orthog_u2);
		
		data.addVariable("z1", standardisedVar("u1", "z1", data));
		data.addVariable("z2", standardisedVar("orthog_u2", "z2", data));
		
		LinCombinationVariable xVar = new LinCombinationVariable("x", data, "z1", "z2",
																			kStart_a1, Math.sqrt(1.0 - kStart_a1 * kStart_a1));
		data.addVariable("x", xVar);
		
		LinCombinationVariable yVar = new LinCombinationVariable("y", data, "z1", "z2",
																			kStart_a1, -Math.sqrt(1.0 - kStart_a1 * kStart_a1));
		data.addVariable("y", yVar);
		
		data.addVariable("xy", new ProductVariable("xy", data, "x", "y"));
	}
	
	private void addModel(DataSet data) {
		ResponseSurfaceModel surface = new ResponseSurfaceModel("Model", data, explanKey, kSurfaceParams);
		data.addVariable("surface", surface);
	}
	
	private ScaledVariable standardisedVar(String rawKey, String newName, DataSet data) {
		NumVariable yVar = (NumVariable)data.getVariable(rawKey);
		double sy = 0.0;
		double syy = 0.0;
		int n = 0;
		ValueEnumeration ye = yVar.values();
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			syy += y * y;
			n ++;
		}
		double mean = sy / n;
		double sd = Math.sqrt((syy - sy * mean) / (n - 1));
		
		return new ScaledVariable(newName, yVar, rawKey, -mean / sd, 1.0 / sd, kDecimals);
	}
	
	
	private void setupColorMap() {
		colourMap = new ColourMap(kKeyColor, kKeyValues);
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		addDataValues(data);
		addModel(data);
		
		setupColorMap();
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}

	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			D3Axis xAxis = new D3Axis("z(y)", D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(kStdAxisString);
			D3Axis yAxis = new D3Axis("z(x)*z(y)", D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(kProductAxisString);
			D3Axis zAxis = new D3Axis("z(x)", D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(kStdAxisString);
			
			ResponseSurfaceView localView = new ResponseSurfaceView(data, this,
																			xAxis, yAxis, zAxis, "surface", explanKey, "xy");
			localView.lockBackground(Color.white);
			localView.setColourMap(colourMap);
			localView.setCrossSize(DataView.LARGE_CROSS);
			localView.setNeverDimCross(true);
			theView = localView;
		
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(50, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			correlSlider = new ParameterSlider(new NumValue(-1.0, 2), new NumValue(1.0, 2),
												new NumValue(2 * kStart_a1 * kStart_a1 - 1, 2), translate("Correlation") + ", r", this);
		
		thePanel.add("Center", correlSlider);
			
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
			thePanel.add(new RotateButton(RotateButton.YXD_ROTATE, theView, this));
			thePanel.add(new RotateButton(RotateButton.DY_ROTATE, theView, this));
			thePanel.add(new RotateButton(RotateButton.DX_ROTATE, theView, this));
			thePanel.add(new RotateButton(RotateButton.YX2_ROTATE, theView, this));
			rotateButton = new XButton(translate("Spin"), this);
			
		thePanel.add(rotateButton);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == correlSlider) {
			double r = correlSlider.getParameter().toDouble();
			double a1Sqr = (r + 1) / 2;
			double a1 = Math.sqrt(a1Sqr);
			double a2 = Math.sqrt(1 - a1Sqr);
			
			LinCombinationVariable xVar = (LinCombinationVariable)data.getVariable("x");
			xVar.setCoeffs(a1, a2);
			LinCombinationVariable yVar = (LinCombinationVariable)data.getVariable("y");
			yVar.setCoeffs(a1, -a2);
			
			data.variableChanged("x");
			data.variableChanged("y");
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