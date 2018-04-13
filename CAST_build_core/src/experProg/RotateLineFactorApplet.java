package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import exper.*;


public class RotateLineFactorApplet extends RotateTwoFactorApplet {
	static final protected String TREAT2_AXIS_PARAM = "treat2Axis";
	static final protected String TREAT2_NUM_VALUE_PARAM = "treat2NumValues";
	
	protected double zCatToNum[];
	protected double meanZ;
	
	protected DataSet readData() {
		DataSet data = super.readData();
			
//		FactorsModel model = (FactorsModel)data.getVariable("model");
		CatVariable treat2 = (CatVariable)data.getVariable("treat2");
			zCatToNum = new double[treat2.noOfCategories()];
			int counts[] = treat2.getCounts();
			StringTokenizer st = new StringTokenizer(getParameter(TREAT2_NUM_VALUE_PARAM));
			double sumZ = 0.0;
			for (int i=0 ; i<zCatToNum.length ; i++) {
				double nextZ = Double.parseDouble(st.nextToken());
				zCatToNum[i] = nextZ;
				sumZ += nextZ * counts[i];
			}
		meanZ = sumZ / treat2.noOfValues();
		
		return data;
	}
	
	protected RotateTwoFactorView create3DView(DataSet data, D3Axis xAxis, D3Axis yAxis,
																													D3Axis zAxis, String yVarKey) {
		RotateTwoFactorView theView = new RotateTwoFactorView(data, this, xAxis, yAxis, zAxis,
																							"treat1", yVarKey, "treat2", "model", zCatToNum);
		theView.setDrawData(false);
		return theView;
	}
	
	protected XPanel threeDPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			CatVariable xVar = (CatVariable)data.getVariable("treat1");
			D3Axis xAxis = new D3Axis(xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setCatScale(xVar);
			D3Axis yAxis = new D3Axis(getVarName("model"), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(RESPONSE_AXIS_INFO_PARAM));
			CatVariable zVar = (CatVariable)data.getVariable("treat2");
			D3Axis zAxis = new D3Axis(zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(TREAT2_AXIS_PARAM));
			
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
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL,
																																ProportionLayout.TOTAL));
			mainPanel.add(ProportionLayout.TOP, new EffectSlidersPanel(data, "treat1", "model", 0,
																	getParameter(TREAT1_EFFECT_AXIS_PARAM), false, true, this));
			mainPanel.add(ProportionLayout.BOTTOM, new EffectSlidersPanel(data, "treat2", "model", 1,
																	getParameter(TREAT2_EFFECT_AXIS_PARAM), true, true,
																	getParameter(TREAT2_AXIS_PARAM), zCatToNum, meanZ, this));
		
		thePanel.add("Center", mainPanel);
		
		thePanel.add("South", new LinFactorChoicePanel(data, "model", 1, 2, zCatToNum, meanZ, this));
			
		thePanel.add("North", new ConstantSliderPanel(data, "model", this));
		
		return thePanel;
	}
}